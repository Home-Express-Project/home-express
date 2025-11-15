package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.SavedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SavedItem entity.
 * Provides database operations for customer saved items.
 */
@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {
    
    /**
     * Find all saved items for a specific customer
     */
    List<SavedItem> findByCustomerId(Long customerId);
    
    /**
     * Find a saved item by ID and customer ID (ensures authorization)
     */
    Optional<SavedItem> findByIdAndCustomerId(Long id, Long customerId);
    
    /**
     * Delete all saved items for a specific customer
     */
    void deleteByCustomerId(Long customerId);
    
    /**
     * Count saved items for a customer
     */
    long countByCustomerId(Long customerId);
}
