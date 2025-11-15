package com.homeexpress.home_express_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    private BankInfo bank;
    private Double depositPercentage;
    
    // Initialize defaults in constructor
    public PaymentConfig() {
        this.bank = new BankInfo();
        this.depositPercentage = 0.3; // 30% default
    }
    
    @Data
    public static class BankInfo {
        private String bank = "MBBank";
        private String accountNumber = "7969679999";
        private String accountName = "CONG TY TNHH HOME EXPRESS";
        private String branch = "Chi nhánh Hà Nội";
    }
}
