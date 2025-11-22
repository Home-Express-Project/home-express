package com.homeexpress.home_express_api.entity;

/**
 * Evidence Type Enum
 * 
 * Categorizes evidence files by their purpose/content type.
 * Used for filtering and organizing evidence in the UI.
 */
public enum EvidenceType {
    /**
     * Photo taken at pickup location (before loading)
     */
    PICKUP_PHOTO,
    
    /**
     * Photo taken at delivery location (after unloading)
     */
    DELIVERY_PHOTO,
    
    /**
     * Photo documenting damage to items or property
     */
    DAMAGE_PHOTO,
    
    /**
     * Customer or recipient signature document
     */
    SIGNATURE,
    
    /**
     * Invoice or receipt document
     */
    INVOICE,
    
    /**
     * Other types of evidence not covered by specific categories
     */
    OTHER
}

