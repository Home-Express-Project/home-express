package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false)
    @NotNull
    private Transport transport;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @NotBlank
    @Size(max = 100)
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @NotBlank
    @Size(max = 20)
    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "license_plate_norm", length = 20, insertable = false, updatable = false)
    private String licensePlateNorm;

    @Column(name = "license_plate_compact", length = 20, insertable = false, updatable = false)
    private String licensePlateCompact;

    @NotNull
    @Column(name = "capacity_kg", nullable = false)
    private BigDecimal capacityKg;

    @Column(name = "capacity_m3")
    private BigDecimal capacityM3;

    @Column(name = "length_cm")
    private BigDecimal lengthCm;

    @Column(name = "width_cm")
    private BigDecimal widthCm;

    @Column(name = "height_cm")
    private BigDecimal heightCm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "year")
    private Short year;

    @Size(max = 50)
    @Column(name = "color", length = 50)
    private String color;

    @NotNull
    @Column(name = "has_tail_lift", nullable = false)
    private Boolean hasTailLift = false;

    @NotNull
    @Column(name = "has_tools", nullable = false)
    private Boolean hasTools = true;

    @Size(max = 255)
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void normalize() {
        if (licensePlate != null) {
            licensePlate = licensePlate.trim().toUpperCase();
        }
    }

    public Vehicle() {
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLicensePlateNorm() {
        return licensePlateNorm;
    }

    public void setLicensePlateNorm(String licensePlateNorm) {
        this.licensePlateNorm = licensePlateNorm;
    }

    public String getLicensePlateCompact() {
        return licensePlateCompact;
    }

    public void setLicensePlateCompact(String licensePlateCompact) {
        this.licensePlateCompact = licensePlateCompact;
    }

    public BigDecimal getCapacityKg() {
        return capacityKg;
    }

    public void setCapacityKg(BigDecimal capacityKg) {
        this.capacityKg = capacityKg;
    }

    public BigDecimal getCapacityM3() {
        return capacityM3;
    }

    public void setCapacityM3(BigDecimal capacityM3) {
        this.capacityM3 = capacityM3;
    }

    public BigDecimal getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Short getYear() {
        return year;
    }

    public void setYear(Short year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getHasTailLift() {
        return hasTailLift;
    }

    public void setHasTailLift(Boolean hasTailLift) {
        this.hasTailLift = hasTailLift;
    }

    public Boolean getHasTools() {
        return hasTools;
    }

    public void setHasTools(Boolean hasTools) {
        this.hasTools = hasTools;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
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
