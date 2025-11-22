package com.homeexpress.home_express_api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homeexpress.home_express_api.dto.request.ContractRequest;
import com.homeexpress.home_express_api.dto.response.ContractResponse;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.Contract;
import com.homeexpress.home_express_api.entity.ContractStatus;
import com.homeexpress.home_express_api.entity.Quotation;
import com.homeexpress.home_express_api.entity.QuotationStatus;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.ContractRepository;
import com.homeexpress.home_express_api.repository.QuotationRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.entity.Notification;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.constants.BookingConstants;
import com.homeexpress.home_express_api.exception.ContractNotFoundException;
import com.homeexpress.home_express_api.exception.QuotationNotFoundException;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.constants.BookingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final QuotationRepository quotationRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TransportRepository transportRepository;

    public ContractService(ContractRepository contractRepository,
            QuotationRepository quotationRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService,
            UserRepository userRepository,
            TransportRepository transportRepository) {
        this.contractRepository = contractRepository;
        this.quotationRepository = quotationRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.transportRepository = transportRepository;
    }

    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Quotation quotation = quotationRepository.findById(request.getQuotationId())
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + request.getQuotationId()));

        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new IllegalStateException("Contract can only be created from ACCEPTED quotation");
        }

        if (contractRepository.findByQuotationId(quotation.getQuotationId()).isPresent()) {
            throw new IllegalStateException("Contract already exists for this quotation");
        }

        if (contractRepository.findByBookingId(quotation.getBookingId()).isPresent()) {
            throw new IllegalStateException("Contract already exists for this booking");
        }

        Contract contract = new Contract();
        contract.setQuotationId(quotation.getQuotationId());
        contract.setBookingId(quotation.getBookingId());
        contract.setTermsAndConditions(request.getTermsAndConditions());

        contract.setTotalAmount(quotation.getQuotedPrice());
        contract.setAgreedPriceVnd(quotation.getQuotedPrice().longValue());

        long depositAmount = (long) (quotation.getQuotedPrice().longValue() * BookingConstants.DEFAULT_CONTRACT_DEPOSIT_PERCENTAGE);
        contract.setDepositRequiredVnd(depositAmount);

        String contractNumber = generateContractNumber();
        contract.setContractNumber(contractNumber);

        contract.setStatus(ContractStatus.DRAFT);

        Contract saved = contractRepository.save(contract);
        return mapToResponse(saved);
    }

    private String generateContractNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseNumber = BookingConstants.CONTRACT_NUMBER_PREFIX + datePrefix + "-";

        long count = contractRepository.count();
        int sequence = (int) (count + 1);

        return baseNumber + String.format(BookingConstants.CONTRACT_SEQUENCE_FORMAT, sequence);
    }

    public ContractResponse getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));
        return mapToResponse(contract);
    }

    public ContractResponse getContractByNumber(String contractNumber) {
        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new ContractNotFoundException(contractNumber));
        return mapToResponse(contract);
    }

    public Page<ContractResponse> getContractsByCustomerId(Long customerId, Pageable pageable) {
        return contractRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ContractResponse> getContractsByTransportId(Long transportId, Pageable pageable) {
        return contractRepository.findByTransportId(transportId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ContractResponse> getAllContracts(Pageable pageable) {
        return contractRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<ContractResponse> getContractsByStatus(ContractStatus status, Pageable pageable) {
        return contractRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    public Page<ContractResponse> getContractsByTransportIdAndStatus(Long transportId, ContractStatus status, Pageable pageable) {
        return contractRepository.findByTransportIdAndStatus(transportId, status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ContractResponse updateContractStatus(Long id, ContractStatus newStatus) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));

        contract.setStatus(newStatus);
        Contract updated = contractRepository.save(contract);
        return mapToResponse(updated);
    }

    @Transactional
    public ContractResponse signContract(Long id, String role, String signatureUrl, String ipAddress) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));

        LocalDateTime now = LocalDateTime.now();

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            contract.setCustomerSigned(true);
            contract.setCustomerSignedAt(now);
            contract.setCustomerSignatureUrl(signatureUrl);
            contract.setCustomerSignedIp(ipAddress);
        } else if ("TRANSPORT".equalsIgnoreCase(role)) {
            contract.setTransportSigned(true);
            contract.setTransportSignedAt(now);
            contract.setTransportSignatureUrl(signatureUrl);
            contract.setTransportSignedIp(ipAddress);
        } else {
            throw new IllegalArgumentException("Invalid role. Must be CUSTOMER or TRANSPORT");
        }

        Contract updated = contractRepository.save(contract);
        
        // Send notification about contract signing
        sendContractSignedNotification(updated, role);
        
        return mapToResponse(updated);
    }

    /**
     * Send notification when contract is signed
     */
    private void sendContractSignedNotification(Contract contract, String role) {
        try {
            Booking booking = bookingRepository.findById(contract.getBookingId())
                .orElse(null);
            
            if (booking == null) return;
            
            // Check if both parties have signed
            boolean bothSigned = contract.getCustomerSigned() != null && contract.getCustomerSigned() 
                && contract.getTransportSigned() != null && contract.getTransportSigned();
            
            if ("CUSTOMER".equalsIgnoreCase(role)) {
                // Notify transport that customer signed
                if (booking.getTransportId() != null) {
                    Transport transport = transportRepository.findById(booking.getTransportId()).orElse(null);
                    if (transport != null && transport.getUser() != null) {
                        String message = bothSigned 
                            ? String.format("Contract #%s for booking #%d has been fully signed by both parties.", 
                                contract.getContractNumber(), booking.getBookingId())
                            : String.format("Customer has signed contract #%s for booking #%d. Please sign to complete.", 
                                contract.getContractNumber(), booking.getBookingId());
                        
                        notificationService.createNotification(
                            transport.getUser().getUserId(),
                            Notification.NotificationType.CONTRACT_UPDATE,
                            bothSigned ? "Contract Fully Signed" : "Customer Signed Contract",
                            message,
                            Notification.ReferenceType.CONTRACT,
                            contract.getContractId(),
                            bothSigned ? Notification.Priority.HIGH : Notification.Priority.MEDIUM
                        );
                    }
                }
            } else if ("TRANSPORT".equalsIgnoreCase(role)) {
                // Notify customer that transport signed
                User customerUser = userRepository.findById(booking.getCustomerId()).orElse(null);
                if (customerUser != null) {
                    String message = bothSigned 
                        ? String.format("Contract #%s for booking #%d has been fully signed by both parties.", 
                            contract.getContractNumber(), booking.getBookingId())
                        : String.format("Transport has signed contract #%s for booking #%d. Please sign to complete.", 
                            contract.getContractNumber(), booking.getBookingId());
                    
                    notificationService.createNotification(
                        customerUser.getUserId(),
                        Notification.NotificationType.CONTRACT_UPDATE,
                        bothSigned ? "Contract Fully Signed" : "Transport Signed Contract",
                        message,
                        Notification.ReferenceType.CONTRACT,
                        contract.getContractId(),
                        bothSigned ? Notification.Priority.HIGH : Notification.Priority.MEDIUM
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send contract signed notification for contract {}: {}", 
                contract.getContractId(), e.getMessage(), e);
        }
    }

    public Long getCustomerIdByContractId(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        Booking booking = bookingRepository.findById(contract.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", contract.getBookingId()));

        return booking.getCustomerId();
    }

    public Long getTransportIdByContractId(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        Booking booking = bookingRepository.findById(contract.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", contract.getBookingId()));

        return booking.getTransportId();
    }

    private ContractResponse mapToResponse(Contract contract) {
        ContractResponse response = new ContractResponse();
        response.setContractId(contract.getContractId());
        response.setBookingId(contract.getBookingId());
        response.setQuotationId(contract.getQuotationId());
        response.setContractNumber(contract.getContractNumber());
        response.setTermsAndConditions(contract.getTermsAndConditions());
        response.setTotalAmount(contract.getTotalAmount());
        response.setAgreedPriceVnd(contract.getAgreedPriceVnd());
        response.setDepositRequiredVnd(contract.getDepositRequiredVnd());
        response.setDepositDueAt(contract.getDepositDueAt());
        response.setBalanceDueAt(contract.getBalanceDueAt());
        response.setCustomerSigned(contract.getCustomerSigned());
        response.setCustomerSignedAt(contract.getCustomerSignedAt());
        response.setCustomerSignatureUrl(contract.getCustomerSignatureUrl());
        response.setTransportSigned(contract.getTransportSigned());
        response.setTransportSignedAt(contract.getTransportSignedAt());
        response.setTransportSignatureUrl(contract.getTransportSignatureUrl());
        response.setStatus(contract.getStatus());
        response.setCreatedAt(contract.getCreatedAt());
        response.setUpdatedAt(contract.getUpdatedAt());
        return response;
    }
}
