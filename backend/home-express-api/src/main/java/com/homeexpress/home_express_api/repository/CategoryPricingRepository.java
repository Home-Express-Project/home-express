package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.CategoryPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryPricingRepository extends JpaRepository<CategoryPricing, Long> {

    @Query("SELECT cp FROM CategoryPricing cp WHERE cp.transport.transportId = :transportId " +
           "AND cp.category.categoryId = :categoryId " +
           "AND (:sizeId IS NULL AND cp.size IS NULL OR cp.size.sizeId = :sizeId) " +
           "AND cp.isActive = true " +
           "AND cp.validFrom <= :checkDate " +
           "AND (cp.validTo IS NULL OR cp.validTo >= :checkDate)")
    Optional<CategoryPricing> findActiveByCategoryAndSizeAndDate(
            @Param("transportId") Long transportId,
            @Param("categoryId") Long categoryId,
            @Param("sizeId") Long sizeId,
            @Param("checkDate") LocalDateTime checkDate);

    @Query("SELECT cp FROM CategoryPricing cp WHERE cp.transport.transportId = :transportId " +
           "AND cp.category.categoryId = :categoryId " +
           "AND (:sizeId IS NULL AND cp.size IS NULL OR cp.size.sizeId = :sizeId) " +
           "AND cp.isActive = true")
    List<CategoryPricing> findActiveByCategoryAndSize(
            @Param("transportId") Long transportId,
            @Param("categoryId") Long categoryId,
            @Param("sizeId") Long sizeId);

    @Query("SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END FROM CategoryPricing cp " +
           "WHERE cp.transport.transportId = :transportId " +
           "AND cp.category.categoryId = :categoryId " +
           "AND (:sizeId IS NULL AND cp.size IS NULL OR cp.size.sizeId = :sizeId) " +
           "AND cp.isActive = true " +
           "AND (:excludeId IS NULL OR cp.categoryPricingId <> :excludeId) " +
           "AND (:validTo IS NULL OR cp.validFrom <= :validTo) " +
           "AND (cp.validTo IS NULL OR :validFrom <= cp.validTo)")
    boolean hasOverlappingActivePricing(
            @Param("transportId") Long transportId,
            @Param("categoryId") Long categoryId,
            @Param("sizeId") Long sizeId,
            @Param("validFrom") LocalDateTime validFrom,
            @Param("validTo") LocalDateTime validTo,
            @Param("excludeId") Long excludeId);

    List<CategoryPricing> findByIsActive(Boolean isActive);

    List<CategoryPricing> findByTransport_TransportId(Long transportId);

    List<CategoryPricing> findByCategory_CategoryId(Long categoryId);

    @Query("SELECT cp FROM CategoryPricing cp WHERE cp.transport.transportId = :transportId " +
           "AND cp.category.categoryId = :categoryId " +
           "ORDER BY cp.validFrom DESC")
    List<CategoryPricing> findByTransportAndCategoryOrderByValidFromDesc(
            @Param("transportId") Long transportId,
            @Param("categoryId") Long categoryId);
}
