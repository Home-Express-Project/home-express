package com.homeexpress.home_express_api.entity;

/**
 * Enum representing the status of a dispute.
 */
public enum DisputeStatus {
    /**
     * Dispute has been filed and is awaiting review
     */
    PENDING,
    
    /**
     * Dispute is currently being reviewed by management
     */
    UNDER_REVIEW,
    
    /**
     * Dispute has been resolved successfully
     */
    RESOLVED,
    
    /**
     * Dispute has been rejected
     */
    REJECTED,
    
    /**
     * Dispute has been escalated to higher management
     */
    ESCALATED
}

