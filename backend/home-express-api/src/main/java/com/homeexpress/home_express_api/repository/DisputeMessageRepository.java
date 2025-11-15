package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.DisputeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for DisputeMessage entity.
 * Provides methods for querying dispute messages.
 */
@Repository
public interface DisputeMessageRepository extends JpaRepository<DisputeMessage, Long> {

    /**
     * Find all messages for a specific dispute, ordered by creation date ascending
     */
    List<DisputeMessage> findByDisputeIdOrderByCreatedAtAsc(Long disputeId);

    /**
     * Count total messages for a dispute
     */
    Long countByDisputeId(Long disputeId);
}

