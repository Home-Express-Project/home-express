package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.IntakeSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntakeSessionItemRepository extends JpaRepository<IntakeSessionItem, Long> {
    
    List<IntakeSessionItem> findBySessionSessionId(String sessionId);
    
    void deleteBySessionSessionId(String sessionId);
}
