package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer extends SharedPrimaryKeyEntity<Long> {

    // NOTE: customer_id = user_id (1-to-1), ko can user_id rieng
    // customer info rieng, tach ra khoi bang users
    
    public static final String DEFAULT_LANGUAGE = "vi";
    private static final String VN_PHONE_REGEX = "^0[1-9][0-9]{8}$"; // 10 so, bat dau 0
    
    @Id
    @Column(name = "customer_id")
    private Long customerId;
    
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "customer_id")
    private User user;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @NotBlank
    @Pattern(regexp = VN_PHONE_REGEX, message = "Invalid VN phone - must be 10 digits starting with 0")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    
    @Size(max = 10)
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = DEFAULT_LANGUAGE;
    
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
    }

    @Override
    public Long getId() {
        return customerId;
    }
    
    // constructor
    public Customer() {}
    
    // getter setter
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
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
}
