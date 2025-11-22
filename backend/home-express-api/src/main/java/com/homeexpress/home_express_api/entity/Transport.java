package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transports")
public class Transport extends SharedPrimaryKeyEntity<Long> {

    // NOTE: entity cho transport company - phuc tap nhat
    // co verification workflow, KYC, banking info

    private static final String VN_PHONE_REGEX = "^0[1-9][0-9]{8}$";
    private static final String VN_GPKD_REGEX = "^[0-9]{10}$|^[0-9]{13}$"; // 10 or 13 digits
    private static final String VN_TAX_CODE_REGEX = "^[0-9]{10}$|^[0-9]{10}-[0-9]{3}$";

    @Id
    @Column(name = "transport_id")
    private Long transportId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "transport_id")
    private User user;

    // company info
    @NotBlank
    @Size(max = 255)
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = VN_GPKD_REGEX, message = "Invalid GPKD - must be 10 or 13 digits")
    @Column(name = "business_license_number", nullable = false, unique = true, length = 50)
    private String businessLicenseNumber;

    @Size(max = 50)
    @Pattern(regexp = VN_TAX_CODE_REGEX, message = "Invalid tax code format")
    @Column(name = "tax_code", unique = true, length = 50)
    private String taxCode;

    @NotBlank
    @Pattern(regexp = VN_PHONE_REGEX, message = "Invalid VN phone")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @NotBlank
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotBlank
    @Size(max = 100)
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "district", length = 100)
    private String district;

    @Size(max = 100)
    @Column(name = "ward", length = 100)
    private String ward;

    // documents
    @Column(name = "license_photo_url", columnDefinition = "TEXT")
    private String licensePhotoUrl;

    @Column(name = "insurance_photo_url", columnDefinition = "TEXT")
    private String insurancePhotoUrl;

    // verification workflow
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY de tranh N+1
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    // statistics - cap nhat tu booking
    @NotNull
    @Column(name = "total_bookings", nullable = false)
    private Integer totalBookings = 0;

    @NotNull
    @Column(name = "completed_bookings", nullable = false)
    private Integer completedBookings = 0;

    @NotNull
    @Column(name = "cancelled_bookings", nullable = false)
    private Integer cancelledBookings = 0;

    @Column(name = "average_rating")
    private BigDecimal averageRating = BigDecimal.ZERO;

    // pricing readiness - gate job/quotation access
    @NotNull
    @Column(name = "ready_to_quote", nullable = false)
    private Boolean readyToQuote = Boolean.FALSE;

    @Column(name = "rate_card_expires_at")
    private LocalDateTime rateCardExpiresAt;

    // KYC - Vietnamese specific
    @Size(max = 12)
    @Column(name = "national_id_number", unique = true, length = 12)
    private String nationalIdNumber; // validate 9 or 12 in service layer

    @Enumerated(EnumType.STRING)
    @Column(name = "national_id_type")
    private NationalIdType nationalIdType;

    @Column(name = "national_id_issue_date")
    private LocalDate nationalIdIssueDate;

    @Size(max = 100)
    @Column(name = "national_id_issuer", length = 100)
    private String nationalIdIssuer;

    @Column(name = "national_id_photo_front_url", columnDefinition = "TEXT")
    private String nationalIdPhotoFrontUrl;

    @Column(name = "national_id_photo_back_url", columnDefinition = "TEXT")
    private String nationalIdPhotoBackUrl;

    // banking info - VN banks
    @Size(max = 100)
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Size(max = 10)
    @Column(name = "bank_code", length = 10)
    private String bankCode; // FK to vn_banks

    @Size(max = 19)
    @Column(name = "bank_account_number", length = 19)
    private String bankAccountNumber;

    @Size(max = 255)
    @Column(name = "bank_account_holder", length = 255)
    private String bankAccountHolder;

    // DB-managed
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void normalize() {
        // Normalize Vietnamese phone number: +84/84 → 0, remove spaces/dashes/dots
        if (phone != null) {
            // Inline simple normalization
            phone = phone.replaceAll("[\\s\\-\\.]", "");
            if (phone.startsWith("+84")) {
                phone = "0" + phone.substring(3);
            } else if (phone.startsWith("84") && phone.length() >= 11) {
                phone = "0" + phone.substring(2);
            }
        }
        if (taxCode != null) {
            taxCode = taxCode.trim();
        }
        if (businessLicenseNumber != null) {
            businessLicenseNumber = businessLicenseNumber.trim();
        }
        if (nationalIdNumber != null) {
            nationalIdNumber = nationalIdNumber.trim();
        }
    }

    // constructor
    public Transport() {}

    // getter setter - rat nhieu :(
    public Long getTransportId() {
        return transportId;
    }

    public void setTransportId(Long transportId) {
        this.transportId = transportId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBusinessLicenseNumber() {
        return businessLicenseNumber;
    }

    public void setBusinessLicenseNumber(String businessLicenseNumber) {
        this.businessLicenseNumber = businessLicenseNumber;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getLicensePhotoUrl() {
        return licensePhotoUrl;
    }

    public void setLicensePhotoUrl(String licensePhotoUrl) {
        this.licensePhotoUrl = licensePhotoUrl;
    }

    public String getInsurancePhotoUrl() {
        return insurancePhotoUrl;
    }

    public void setInsurancePhotoUrl(String insurancePhotoUrl) {
        this.insurancePhotoUrl = insurancePhotoUrl;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
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

    public Integer getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(Integer cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Boolean getReadyToQuote() {
        return readyToQuote;
    }

    public void setReadyToQuote(Boolean readyToQuote) {
        this.readyToQuote = readyToQuote;
    }

    public LocalDateTime getRateCardExpiresAt() {
        return rateCardExpiresAt;
    }

    public void setRateCardExpiresAt(LocalDateTime rateCardExpiresAt) {
        this.rateCardExpiresAt = rateCardExpiresAt;
    }

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public void setNationalIdNumber(String nationalIdNumber) {
        this.nationalIdNumber = nationalIdNumber;
    }

    public NationalIdType getNationalIdType() {
        return nationalIdType;
    }

    public void setNationalIdType(NationalIdType nationalIdType) {
        this.nationalIdType = nationalIdType;
    }

    public LocalDate getNationalIdIssueDate() {
        return nationalIdIssueDate;
    }

    public void setNationalIdIssueDate(LocalDate nationalIdIssueDate) {
        this.nationalIdIssueDate = nationalIdIssueDate;
    }

    public String getNationalIdIssuer() {
        return nationalIdIssuer;
    }

    public void setNationalIdIssuer(String nationalIdIssuer) {
        this.nationalIdIssuer = nationalIdIssuer;
    }

    public String getNationalIdPhotoFrontUrl() {
        return nationalIdPhotoFrontUrl;
    }

    public void setNationalIdPhotoFrontUrl(String nationalIdPhotoFrontUrl) {
        this.nationalIdPhotoFrontUrl = nationalIdPhotoFrontUrl;
    }

    public String getNationalIdPhotoBackUrl() {
        return nationalIdPhotoBackUrl;
    }

    public void setNationalIdPhotoBackUrl(String nationalIdPhotoBackUrl) {
        this.nationalIdPhotoBackUrl = nationalIdPhotoBackUrl;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder;
    }

    public void setBankAccountHolder(String bankAccountHolder) {
        this.bankAccountHolder = bankAccountHolder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getId() {
        return transportId;
    }
}
