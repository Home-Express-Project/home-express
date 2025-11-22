package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing commission rules.
 * Supports time-based effective rules and transport-specific overrides.
 */
@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    /**
     * Find active commission rule for a specific transport at a given date.
     * Returns the rule where:
     * - transport_id matches
     * - is_active = true
     * - effective_from <= effectiveDate
     * - effective_to is null OR effective_to > effectiveDate
     * 
     * @param transportId ID of the transport
     * @param effectiveDate The date to check rule validity
     * @return Optional containing the matching rule
     */
    @Query("SELECT cr FROM CommissionRule cr " +
           "WHERE cr.transportId = :transportId " +
           "AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :effectiveDate " +
           "AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :effectiveDate) " +
           "ORDER BY cr.effectiveFrom DESC")
    Optional<CommissionRule> findActiveRuleForTransport(
        @Param("transportId") Long transportId, 
        @Param("effectiveDate") LocalDateTime effectiveDate
    );

    /**
     * Find default (platform-wide) commission rule at a given date.
     * Returns the rule where:
     * - transport_id is null (default rule)
     * - is_active = true
     * - effective_from <= effectiveDate
     * - effective_to is null OR effective_to > effectiveDate
     * 
     * @param effectiveDate The date to check rule validity
     * @return Optional containing the default rule
     */
    @Query("SELECT cr FROM CommissionRule cr " +
           "WHERE cr.transportId IS NULL " +
           "AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :effectiveDate " +
           "AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :effectiveDate) " +
           "ORDER BY cr.effectiveFrom DESC")
    Optional<CommissionRule> findDefaultActiveRule(@Param("effectiveDate") LocalDateTime effectiveDate);
}
