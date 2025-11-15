package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intake_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntakeSession {
    
    @Id
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "active";
    
    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;
    
    @Column(name = "estimated_volume", precision = 10, scale = 2)
    private BigDecimal estimatedVolume;
    
    @Column(name = "ai_service_used", length = 50)
    private String aiServiceUsed;
    
    @Column(name = "average_confidence", precision = 5, scale = 4)
    private BigDecimal averageConfidence;
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntakeSessionItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    public void addItem(IntakeSessionItem item) {
        items.add(item);
        item.setSession(this);
        this.totalItems = items.size();
    }
    
    public void removeItem(IntakeSessionItem item) {
        items.remove(item);
        item.setSession(null);
        this.totalItems = items.size();
    }
}
