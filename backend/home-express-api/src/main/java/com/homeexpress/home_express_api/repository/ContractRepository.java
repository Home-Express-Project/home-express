package com.homeexpress.home_express_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.homeexpress.home_express_api.entity.Contract;
import com.homeexpress.home_express_api.entity.ContractStatus;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractNumber(String contractNumber);

    Optional<Contract> findByQuotationId(Long quotationId);

    Optional<Contract> findByBookingId(Long bookingId);

    @Query("SELECT c FROM Contract c JOIN Booking b ON c.bookingId = b.bookingId WHERE b.customerId = :customerId")
    Page<Contract> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT c FROM Contract c JOIN Booking b ON c.bookingId = b.bookingId WHERE b.transportId = :transportId")
    Page<Contract> findByTransportId(@Param("transportId") Long transportId, Pageable pageable);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    @Query("SELECT c FROM Contract c JOIN Booking b ON c.bookingId = b.bookingId " +
           "WHERE b.customerId = :customerId AND c.status = :status")
    Page<Contract> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                              @Param("status") ContractStatus status, 
                                              Pageable pageable);

    @Query("SELECT c FROM Contract c JOIN Booking b ON c.bookingId = b.bookingId " +
           "WHERE b.transportId = :transportId AND c.status = :status")
    Page<Contract> findByTransportIdAndStatus(@Param("transportId") Long transportId, 
                                               @Param("status") ContractStatus status, 
                                               Pageable pageable);
}
