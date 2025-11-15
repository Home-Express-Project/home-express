package com.homeexpress.home_express_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a dispute filed for a booking.
 * Disputes can be filed by customers or transport providers regarding various issues.
 */
@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id")
    private Long disputeId;

    @NotNull
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    @NotNull
    @Column(name = "filed_by_user_id", nullable = false)
    private Long filedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filed_by_user_id", insertable = false, updatable = false)
    private User filedByUser;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_type", nullable = false, length = 50)
    private DisputeType disputeType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.PENDING;

    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requested_resolution", columnDefinition = "TEXT")
    private String requestedResolution;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by_user_id")
    private Long resolvedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id", insertable = false, updatable = false)
    private User resolvedByUser;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DisputeMessage> messages = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "dispute_evidence",
        joinColumns = @JoinColumn(name = "dispute_id"),
        inverseJoinColumns = @JoinColumn(name = "evidence_id")
    )
    @Builder.Default
    private Set<Evidence> evidence = new HashSet<>();

    // Helper methods
    public void addMessage(DisputeMessage message) {
        messages.add(message);
        message.setDispute(this);
    }

    public void removeMessage(DisputeMessage message) {
        messages.remove(message);
        message.setDispute(null);
    }

    public void addEvidence(Evidence evidenceItem) {
        evidence.add(evidenceItem);
    }

    public void removeEvidence(Evidence evidenceItem) {
        evidence.remove(evidenceItem);
    }

    /**
     * Mark dispute as resolved
     */
    public void resolve(Long resolvedByUserId, String resolutionNotes) {
        this.status = DisputeStatus.RESOLVED;
        this.resolvedByUserId = resolvedByUserId;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Mark dispute as rejected
     */
    public void reject(Long resolvedByUserId, String resolutionNotes) {
        this.status = DisputeStatus.REJECTED;
        this.resolvedByUserId = resolvedByUserId;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Escalate dispute to higher management
     */
    public void escalate() {
        this.status = DisputeStatus.ESCALATED;
    }

    /**
     * Put dispute under review
     */
    public void putUnderReview() {
        this.status = DisputeStatus.UNDER_REVIEW;
    }
}

