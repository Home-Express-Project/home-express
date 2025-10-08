package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.entity.OtpCode;
import com.homeexpress.home_express_api.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    // Generate OTP 6 chu so
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Tao va gui OTP qua email
    public void createAndSendOtp(String email) {
        // Generate OTP code
        String otpCode = generateOtp();

        // Thoi gian het han: 5 phut
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Luu OTP vao database
        OtpCode otp = new OtpCode(email, otpCode, expiresAt);
        otpRepository.save(otp);

        // Gui email
        emailService.sendOtpEmail(email, otpCode);
    }

    // Verify OTP
    public boolean verifyOtp(String email, String code) {
        // Tim OTP chua su dung
        OtpCode otp = otpRepository.findByEmailAndCodeAndIsUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Invalid OTP code"));

        // Kiem tra het han chua
        if (otp.isExpired()) {
            throw new RuntimeException("OTP code has expired");
        }

        // Danh dau da su dung
        otp.setIsUsed(true);
        otpRepository.save(otp);

        return true;
    }

    // Xoa OTP het han (chay dinh ky)
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
