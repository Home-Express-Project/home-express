package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    
    Optional<OtpCode> findByEmailAndCodeAndIsUsedFalse(String email, String code);
    
    Optional<OtpCode> findTopByEmailOrderByCreatedAtDesc(String email);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
