package com.homeexpress.home_express_api.entity;

/**
 * Operational lifecycle for transport partners.
 * Separate from KYC {@link VerificationStatus} so we can gate access
 * until the partner finishes configuration (rate card, vehicles, etc.).
 */
public enum TransportOperationalStatus {
    REGISTERED,
    PENDING_VERIFICATION,
    APPROVED,
    READY_TO_QUOTE,
    SUSPENDED,
    NEEDS_UPDATE
}
