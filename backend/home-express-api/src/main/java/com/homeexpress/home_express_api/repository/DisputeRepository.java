package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Dispute;
import com.homeexpress.home_express_api.entity.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Dispute entity.
 * Provides methods for querying disputes by various criteria.
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    /**
     * Find all disputes for a specific booking, ordered by creation date descending
     */
    List<Dispute> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Find all disputes filed by a specific user, ordered by creation date descending
     */
    List<Dispute> findByFiledByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all disputes with a specific status, ordered by creation date descending
     */
    List<Dispute> findByStatusOrderByCreatedAtDesc(DisputeStatus status);

    /**
     * Count total disputes for a booking
     */
    Long countByBookingId(Long bookingId);

    /**
     * Count disputes for a booking with a specific status
     */
    Long countByBookingIdAndStatus(Long bookingId, DisputeStatus status);

    /**
     * Check if a booking has any pending disputes
     */
    boolean existsByBookingIdAndStatus(Long bookingId, DisputeStatus status);
}

