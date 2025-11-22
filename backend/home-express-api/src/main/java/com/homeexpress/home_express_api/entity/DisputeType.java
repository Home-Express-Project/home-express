package com.homeexpress.home_express_api.entity;

/**
 * Enum representing the type of dispute filed for a booking.
 */
public enum DisputeType {
    /**
     * Dispute related to pricing or billing issues
     */
    PRICING_DISPUTE,
    
    /**
     * Claim for damaged items during transport
     */
    DAMAGE_CLAIM,
    
    /**
     * Issues with service quality or professionalism
     */
    SERVICE_QUALITY,
    
    /**
     * Problems with delivery timing or location
     */
    DELIVERY_ISSUE,
    
    /**
     * Payment processing or refund issues
     */
    PAYMENT_ISSUE,
    
    /**
     * Other types of disputes not covered above
     */
    OTHER
}

