package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "managers")
public class Manager extends SharedPrimaryKeyEntity<Long> {

    // NOTE: manager entity - don gian nhat
    // co JSON permissions de luu cac quyen rieng
    
    private static final String VN_PHONE_REGEX = "^0[1-9][0-9]{8}$";
    
    @Id
    @Column(name = "manager_id")
    private Long managerId;
    
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "manager_id")
    private User user;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @NotBlank
    @Pattern(regexp = VN_PHONE_REGEX, message = "Invalid VN phone")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Size(max = 50)
    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;
    
    @Size(max = 100)
    @Column(name = "department", length = 100)
    private String department;
    
    // JSON field - array of permission codes
    // example: ["USER_MANAGE", "TRANSPORT_APPROVE", "BOOKING_VIEW"]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "JSON")
    private List<String> permissions;
    
    // DB-managed
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void normalize() {
        // trim phone
        if (phone != null) {
            phone = phone.trim().replaceAll("\\s", "");
        }
    }
    
    // constructor
    public Manager() {}

    // getter setter
    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
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
        return managerId;
    }
}
