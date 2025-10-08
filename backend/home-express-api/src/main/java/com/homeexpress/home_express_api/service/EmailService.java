package com.homeexpress.home_express_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Gui email OTP
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@homeexpress.com");
            message.setTo(toEmail);
            message.setSubject("Home Express - Ma xac thuc OTP");
            message.setText(
                "Xin chao,\n\n" +
                "Ma OTP cua ban la: " + otpCode + "\n\n" +
                "Ma nay co hieu luc trong 5 phut.\n\n" +
                "Neu ban khong yeu cau reset mat khau, vui long bo qua email nay.\n\n" +
                "Tran trong,\n" +
                "Home Express Team"
            );

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
