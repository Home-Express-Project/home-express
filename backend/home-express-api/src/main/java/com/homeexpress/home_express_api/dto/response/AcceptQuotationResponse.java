package com.homeexpress.home_express_api.dto.response;

import java.math.BigDecimal;

import com.homeexpress.home_express_api.entity.BookingStatus;

public class AcceptQuotationResponse {

    private String message;
    private Long contractId;
    private String contractNumber;
    private String contractPdfUrl;
    private BookingSummary booking;
    private CustomerSummary customer;
    private TransportSummary transport;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractPdfUrl() {
        return contractPdfUrl;
    }

    public void setContractPdfUrl(String contractPdfUrl) {
        this.contractPdfUrl = contractPdfUrl;
    }

    public BookingSummary getBooking() {
        return booking;
    }

    public void setBooking(BookingSummary booking) {
        this.booking = booking;
    }

    public CustomerSummary getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerSummary customer) {
        this.customer = customer;
    }

    public TransportSummary getTransport() {
        return transport;
    }

    public void setTransport(TransportSummary transport) {
        this.transport = transport;
    }

    public static class BookingSummary {
        private Long bookingId;
        private BookingStatus status;
        private Long finalTransportId;
        private BigDecimal finalPrice;

        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public BookingStatus getStatus() {
            return status;
        }

        public void setStatus(BookingStatus status) {
            this.status = status;
        }

        public Long getFinalTransportId() {
            return finalTransportId;
        }

        public void setFinalTransportId(Long finalTransportId) {
            this.finalTransportId = finalTransportId;
        }

        public BigDecimal getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
        }
    }

    public static class CustomerSummary {
        private Long customerId;
        private String fullName;
        private String phone;
        private String email;
        private Double averageRating;

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }
    }

    public static class TransportSummary {
        private Long transportId;
        private String companyName;
        private String phone;
        private String email;
        private Double averageRating;
        private Integer totalBookings;
        private Integer completedBookings;

        public Long getTransportId() {
            return transportId;
        }

        public void setTransportId(Long transportId) {
            this.transportId = transportId;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }

        public Integer getTotalBookings() {
            return totalBookings;
        }

        public void setTotalBookings(Integer totalBookings) {
            this.totalBookings = totalBookings;
        }

        public Integer getCompletedBookings() {
            return completedBookings;
        }

        public void setCompletedBookings(Integer completedBookings) {
            this.completedBookings = completedBookings;
        }
    }
}
