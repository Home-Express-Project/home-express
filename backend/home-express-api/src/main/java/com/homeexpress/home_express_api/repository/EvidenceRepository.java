package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Evidence;
import com.homeexpress.home_express_api.entity.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    // Incident evidence queries
    List<Evidence> findByIncidentIdOrderByUploadedAtDesc(Long incidentId);

    // Booking evidence queries
    List<Evidence> findByBookingIdOrderByUploadedAtDesc(Long bookingId);

    List<Evidence> findByBookingIdAndEvidenceTypeOrderByUploadedAtDesc(Long bookingId, EvidenceType evidenceType);

    // User evidence queries
    List<Evidence> findByUploadedByUserIdOrderByUploadedAtDesc(Long uploadedByUserId);

    // Count queries
    long countByBookingId(Long bookingId);

    long countByBookingIdAndEvidenceType(Long bookingId, EvidenceType evidenceType);
}
