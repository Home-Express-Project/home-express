package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.SecurityConfigProperties;
import com.homeexpress.home_express_api.entity.OtpCode;
import com.homeexpress.home_express_api.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private SecurityConfigProperties securityConfig;

    @Mock
    private SecurityConfigProperties.OtpConfig otpConfig;

    @InjectMocks
    private OtpService otpService;

    private OtpCode mockOtpCode;

    @BeforeEach
    void setUp() {
        // Setup mock OTP code
        mockOtpCode = new OtpCode(
                "test@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(5)
        );
        mockOtpCode.setOtpId(1L);
        mockOtpCode.setIsUsed(false);
    }

    @Test
    void testGenerateOtp_Success() {
        // Given
        when(securityConfig.getOtp()).thenReturn(otpConfig);
        when(otpConfig.getCodeLength()).thenReturn(6);

        // When
        String otp = otpService.generateOtp();

        // Then
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}")); // Verify it's 6 digits
        
        // Verify OTP is in valid range (100000 - 999999)
        int otpValue = Integer.parseInt(otp);
        assertTrue(otpValue >= 100000);
        assertTrue(otpValue <= 999999);
    }

    @Test
    void testCreateAndSendOtp_Success() {
        // Given
        String email = "customer@test.com";
        when(securityConfig.getOtp()).thenReturn(otpConfig);
        when(otpConfig.getCodeLength()).thenReturn(6);
        when(otpConfig.getExpirationMinutes()).thenReturn(5);
        when(otpRepository.save(any(OtpCode.class))).thenReturn(mockOtpCode);

        // When
        otpService.createAndSendOtp(email);

        // Then
        verify(otpRepository, times(1)).save(argThat(otp -> 
            otp.getEmail().equals(email) &&
            otp.getCode() != null &&
            otp.getCode().length() == 6 &&
            otp.getExpiresAt() != null &&
            otp.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void testVerifyOtp_Success() {
        // Given
        String email = "test@example.com";
        String code = "123456";
        
        // Create OTP that is not expired
        OtpCode validOtp = new OtpCode(
                email,
                code,
                LocalDateTime.now().plusMinutes(3) // Expires in 3 minutes
        );
        validOtp.setIsUsed(false);

        when(otpRepository.findByEmailAndCodeAndIsUsedFalse(email, code))
                .thenReturn(Optional.of(validOtp));
        when(otpRepository.save(any(OtpCode.class))).thenReturn(validOtp);

        // When
        boolean result = otpService.verifyOtp(email, code);

        // Then
        assertTrue(result);
        assertTrue(validOtp.getIsUsed()); // Should be marked as used
        verify(otpRepository, times(1)).findByEmailAndCodeAndIsUsedFalse(email, code);
        verify(otpRepository, times(1)).save(validOtp);
    }

    @Test
    void testVerifyOtp_InvalidCode() {
        // Given
        String email = "test@example.com";
        String code = "999999";

        when(otpRepository.findByEmailAndCodeAndIsUsedFalse(email, code))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            otpService.verifyOtp(email, code);
        });

        assertEquals("Invalid OTP code", exception.getMessage());
        verify(otpRepository, times(1)).findByEmailAndCodeAndIsUsedFalse(email, code);
        verify(otpRepository, never()).save(any(OtpCode.class));
    }

    @Test
    void testVerifyOtp_ExpiredCode() {
        // Given
        String email = "test@example.com";
        String code = "123456";

        // Create OTP that is already expired
        OtpCode expiredOtp = new OtpCode(
                email,
                code,
                LocalDateTime.now().minusMinutes(1) // Expired 1 minute ago
        );
        expiredOtp.setIsUsed(false);

        when(otpRepository.findByEmailAndCodeAndIsUsedFalse(email, code))
                .thenReturn(Optional.of(expiredOtp));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            otpService.verifyOtp(email, code);
        });

        assertEquals("OTP code has expired", exception.getMessage());
        assertFalse(expiredOtp.getIsUsed()); // Should NOT be marked as used
        verify(otpRepository, times(1)).findByEmailAndCodeAndIsUsedFalse(email, code);
        verify(otpRepository, never()).save(any(OtpCode.class));
    }

    @Test
    void testCleanupExpiredOtps_Success() {
        // When
        otpService.cleanupExpiredOtps();

        // Then
        verify(otpRepository, times(1)).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
