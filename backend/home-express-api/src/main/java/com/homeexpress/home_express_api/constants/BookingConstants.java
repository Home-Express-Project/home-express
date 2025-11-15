package com.homeexpress.home_express_api.constants;

/**
 * Constants for booking, quotation, contract, and payment related business rules.
 * Centralizes magic numbers and configuration values for maintainability.
 */
public final class BookingConstants {

    private BookingConstants() {
        // Utility class - prevent instantiation
    }

    // ========================================================================
    // QUOTATION CONSTANTS
    // ========================================================================
    
    /**
     * Default quotation validity period in days
     */
    public static final int DEFAULT_QUOTATION_VALIDITY_DAYS = 7;
    
    /**
     * Default quotation expiry deadline extension in days
     */
    public static final int DEFAULT_QUOTATION_EXPIRY_DAYS = 7;

    // ========================================================================
    // CONTRACT CONSTANTS
    // ========================================================================
    
    /**
     * Default deposit percentage for contracts (50% of total)
     * Note: This differs from PaymentService which uses 30% - this is for contract creation
     */
    public static final double DEFAULT_CONTRACT_DEPOSIT_PERCENTAGE = 0.5;
    
    /**
     * Number of days allowed to pay deposit after contract creation
     */
    public static final int DEPOSIT_DUE_DAYS = 3;
    
    /**
     * Number of days allowed to pay balance after deposit payment
     */
    public static final int BALANCE_DUE_DAYS = 7;
    
    /**
     * Contract number prefix format
     */
    public static final String CONTRACT_NUMBER_PREFIX = "CNT-";
    
    /**
     * Contract sequence number format (4 digits with leading zeros)
     */
    public static final String CONTRACT_SEQUENCE_FORMAT = "%04d";

    // ========================================================================
    // INTAKE SESSION CONSTANTS
    // ========================================================================
    
    /**
     * Intake session expiry time in hours
     */
    public static final int INTAKE_SESSION_EXPIRY_HOURS = 24;
    
    /**
     * Days to keep expired sessions before permanent deletion
     */
    public static final int INTAKE_SESSION_CLEANUP_DAYS = 7;
    
    /**
     * Scheduled cleanup job frequency (every 6 hours)
     */
    public static final String INTAKE_SESSION_CLEANUP_CRON = "0 0 */6 * * *";

    // ========================================================================
    // QUOTATION EXPIRY JOB CONSTANTS
    // ========================================================================
    
    /**
     * Scheduled job frequency for expiring quotations (every 15 minutes)
     */
    public static final String QUOTATION_EXPIRY_JOB_CRON = "0 */15 * * * *";

    // ========================================================================
    // PRICE BREAKDOWN JSON KEYS
    // ========================================================================
    
    public static final String PRICE_BREAKDOWN_BASE_PRICE = "basePrice";
    public static final String PRICE_BREAKDOWN_DISTANCE_PRICE = "distancePrice";
    public static final String PRICE_BREAKDOWN_ITEM_HANDLING_PRICE = "itemHandlingPrice";
    public static final String PRICE_BREAKDOWN_ADDITIONAL_SERVICES_PRICE = "additionalServicesPrice";
    public static final String PRICE_BREAKDOWN_INCLUDES_PACKAGING = "includesPackaging";
    public static final String PRICE_BREAKDOWN_INCLUDES_DISASSEMBLY = "includesDisassembly";
    public static final String PRICE_BREAKDOWN_INCLUDES_INSURANCE = "includesInsurance";
    public static final String PRICE_BREAKDOWN_INSURANCE_VALUE = "insuranceValue";
    public static final String PRICE_BREAKDOWN_ESTIMATED_DURATION_HOURS = "estimatedDurationHours";
}

