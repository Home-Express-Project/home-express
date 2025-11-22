package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
    
    // tim theo business license
    Optional<Transport> findByBusinessLicenseNumber(String licenseNumber);
    
    // tim theo verification status
    List<Transport> findByVerificationStatus(VerificationStatus status);
    Page<Transport> findByVerificationStatus(VerificationStatus status, Pageable pageable);
    
    // check ton tai
    boolean existsByBusinessLicenseNumber(String licenseNumber);
    boolean existsByTaxCode(String taxCode);
    boolean existsByNationalIdNumber(String nationalId);
    
    // tim transports da approved, sap xep theo rating
    List<Transport> findByVerificationStatusOrderByAverageRatingDesc(VerificationStatus status);

    long countByVerificationStatus(VerificationStatus status);
    
    // tim transport theo city
    List<Transport> findByCity(String city);
    
    // tim transport theo user ID
    Optional<Transport> findByUser_UserId(Long userId);
}
