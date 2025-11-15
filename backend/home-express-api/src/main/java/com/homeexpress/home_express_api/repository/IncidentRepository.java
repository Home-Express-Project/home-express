package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Incident;
import com.homeexpress.home_express_api.entity.IncidentStatus;
import com.homeexpress.home_express_api.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    List<Incident> findByBookingIdOrderByReportedAtDesc(Long bookingId);
    
    List<Incident> findByStatusOrderByReportedAtDesc(IncidentStatus status);
    
    List<Incident> findBySeverityOrderByReportedAtDesc(Severity severity);
    
    List<Incident> findByBookingIdAndStatusOrderByReportedAtDesc(Long bookingId, IncidentStatus status);
    
    List<Incident> findByReportedByUserIdOrderByReportedAtDesc(Long reportedByUserId);
    
    List<Incident> findAllByOrderByReportedAtDesc();
}
