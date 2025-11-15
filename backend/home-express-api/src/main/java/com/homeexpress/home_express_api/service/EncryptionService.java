package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.EncryptionConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption Service for sensitive payment and financial data
 * 
 * Uses AES-256-GCM for authenticated encryption with associated data (AEAD).
 * GCM mode provides both confidentiality and authenticity.
 * 
 * Format: [IV (12 bytes)][Encrypted Data][Auth Tag (16 bytes)]
 * Output: Base64-encoded string
 * 
 * Thread-safe and suitable for high-throughput operations.
 */
@Slf4j
@Service
public class EncryptionService {
    
    private final EncryptionConfig config;
    private final SecureRandom secureRandom;
    private SecretKey masterKey;
    
    public EncryptionService(EncryptionConfig config) {
        this.config = config;
        this.secureRandom = new SecureRandom();
    }
    
    @PostConstruct
    public void init() {
        try {
            config.validate();
            
            // Decode master key from Base64
            byte[] keyBytes = Base64.getDecoder().decode(config.getMasterKey());
            this.masterKey = new SecretKeySpec(keyBytes, "AES");
            
            log.info("Encryption service initialized with AES-{}-GCM", config.getKeySize());
        } catch (Exception e) {
            log.error("Failed to initialize encryption service", e);
            throw new RuntimeException("Encryption service initialization failed", e);
        }
    }
    
    /**
     * Encrypt a string value
     * 
     * @param plaintext The plaintext to encrypt
     * @return Base64-encoded encrypted data with IV prepended
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[config.getIvLength()];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(config.getAlgorithm());
            GCMParameterSpec parameterSpec = new GCMParameterSpec(config.getGcmTagLength(), iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, parameterSpec);
            
            // Encrypt
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);
            
            // Combine IV + ciphertext (ciphertext already includes auth tag)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // Return Base64-encoded result
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Decrypt a string value
     * 
     * @param encryptedData Base64-encoded encrypted data with IV prepended
     * @return Decrypted plaintext
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            // Decode from Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[config.getIvLength()];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(config.getAlgorithm());
            GCMParameterSpec parameterSpec = new GCMParameterSpec(config.getGcmTagLength(), iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, parameterSpec);
            
            // Decrypt
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            
            return new String(plaintextBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Encrypt a Long value (for amounts in VND)
     * 
     * @param value The value to encrypt
     * @return Base64-encoded encrypted value
     */
    public String encryptLong(Long value) {
        if (value == null) {
            return null;
        }
        return encrypt(value.toString());
    }
    
    /**
     * Decrypt to Long value
     * 
     * @param encryptedData Base64-encoded encrypted data
     * @return Decrypted Long value
     */
    public Long decryptLong(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }
        String decrypted = decrypt(encryptedData);
        return Long.parseLong(decrypted);
    }
    
    /**
     * Encrypt a BigDecimal value (for payment amounts)
     * 
     * @param value The value to encrypt
     * @return Base64-encoded encrypted value
     */
    public String encryptBigDecimal(java.math.BigDecimal value) {
        if (value == null) {
            return null;
        }
        return encrypt(value.toPlainString());
    }
    
    /**
     * Decrypt to BigDecimal value
     * 
     * @param encryptedData Base64-encoded encrypted data
     * @return Decrypted BigDecimal value
     */
    public java.math.BigDecimal decryptBigDecimal(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }
        String decrypted = decrypt(encryptedData);
        return new java.math.BigDecimal(decrypted);
    }
    
    /**
     * Generate a new random encryption key
     * Useful for key rotation
     * 
     * @return Base64-encoded 256-bit key
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, new SecureRandom());
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Mask sensitive data for display purposes
     * Shows only first and last few characters
     * 
     * @param data The data to mask
     * @param visibleChars Number of characters to show at start and end
     * @return Masked string
     */
    public static String maskData(String data, int visibleChars) {
        if (data == null || data.length() <= visibleChars * 2) {
            return "***";
        }
        String start = data.substring(0, visibleChars);
        String end = data.substring(data.length() - visibleChars);
        return start + "***" + end;
    }
    
    /**
     * Mask bank account number for display
     * 
     * @param accountNumber The account number to mask
     * @return Masked account number (e.g., "1234***7890")
     */
    public static String maskBankAccount(String accountNumber) {
        return maskData(accountNumber, 4);
    }
    
    /**
     * Mask transaction ID for display
     * 
     * @param transactionId The transaction ID to mask
     * @return Masked transaction ID
     */
    public static String maskTransactionId(String transactionId) {
        return maskData(transactionId, 6);
    }
}

