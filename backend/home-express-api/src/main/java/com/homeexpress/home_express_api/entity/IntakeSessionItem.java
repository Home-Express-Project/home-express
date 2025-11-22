package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "intake_session_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntakeSessionItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private IntakeSession session;
    
    @Column(name = "item_id", length = 100)
    private String itemId;
    
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm;
    
    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;
    
    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;
    
    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;
    
    @Column(name = "volume_m3", precision = 10, scale = 4)
    private BigDecimal volumeM3;
    
    @Column(name = "is_fragile")
    @Builder.Default
    private Boolean isFragile = false;
    
    @Column(name = "is_high_value")
    @Builder.Default
    private Boolean isHighValue = false;
    
    @Column(name = "requires_disassembly")
    @Builder.Default
    private Boolean requiresDisassembly = false;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;
    
    @Column(name = "ai_detected")
    @Builder.Default
    private Boolean aiDetected = false;
    
    @Column(name = "source", length = 50)
    private String source;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
