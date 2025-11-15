package com.homeexpress.home_express_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Encryption Configuration
 * 
 * Manages encryption settings for sensitive payment and financial data.
 * Uses AES-256-GCM for data at rest encryption.
 * 
 * Configuration properties:
 * - encryption.master-key: Base64-encoded 256-bit master key
 * - encryption.key-rotation-enabled: Enable automatic key rotation
 * - encryption.key-rotation-days: Days between key rotations
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "encryption")
public class EncryptionConfig {
    
    /**
     * Master encryption key (Base64-encoded 256-bit key)
     * Should be stored in environment variables or secure vault in production
     */
    private String masterKey;
    
    /**
     * Enable automatic key rotation
     */
    private boolean keyRotationEnabled = false;
    
    /**
     * Days between key rotations
     */
    private int keyRotationDays = 90;
    
    /**
     * Algorithm to use for encryption
     */
    private String algorithm = "AES/GCM/NoPadding";
    
    /**
     * Key size in bits
     */
    private int keySize = 256;
    
    /**
     * GCM tag length in bits
     */
    private int gcmTagLength = 128;
    
    /**
     * IV (Initialization Vector) length in bytes
     */
    private int ivLength = 12;
    
    /**
     * Enable encryption for payment amounts
     */
    private boolean encryptPaymentAmounts = true;
    
    /**
     * Enable encryption for transaction IDs
     */
    private boolean encryptTransactionIds = true;
    
    /**
     * Enable encryption for bank account numbers
     */
    private boolean encryptBankAccounts = true;
    
    /**
     * Enable encryption for settlement amounts
     */
    private boolean encryptSettlementAmounts = true;
    
    /**
     * Validate configuration on startup
     */
    public void validate() {
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalStateException(
                "Encryption master key is not configured. " +
                "Set encryption.master-key in application properties or environment variable ENCRYPTION_MASTER_KEY"
            );
        }
        
        // Validate key is Base64 and correct length
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(masterKey);
            if (keyBytes.length != keySize / 8) {
                throw new IllegalStateException(
                    String.format("Master key must be %d bits (%d bytes), got %d bytes", 
                        keySize, keySize / 8, keyBytes.length)
                );
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Master key must be valid Base64-encoded string", e);
        }
    }
}

