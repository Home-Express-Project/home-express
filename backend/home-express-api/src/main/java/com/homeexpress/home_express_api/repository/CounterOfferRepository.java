package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.CounterOffer;
import com.homeexpress.home_express_api.entity.CounterOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CounterOfferRepository extends JpaRepository<CounterOffer, Long> {

    /**
     * Find all counter-offers for a quotation
     */
    List<CounterOffer> findByQuotationIdOrderByCreatedAtDesc(Long quotationId);

    /**
     * Find all counter-offers for a booking
     */
    List<CounterOffer> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Find counter-offers by status
     */
    List<CounterOffer> findByStatusOrderByCreatedAtDesc(CounterOfferStatus status);

    /**
     * Find counter-offers made by a specific user
     */
    List<CounterOffer> findByOfferedByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find the latest counter-offer for a quotation
     */
    Optional<CounterOffer> findFirstByQuotationIdOrderByCreatedAtDesc(Long quotationId);

    /**
     * Find pending counter-offers for a quotation
     */
    List<CounterOffer> findByQuotationIdAndStatusOrderByCreatedAtDesc(Long quotationId, CounterOfferStatus status);

    /**
     * Find expired counter-offers that need to be marked as expired
     */
    @Query("SELECT co FROM CounterOffer co WHERE co.status = 'PENDING' AND co.expiresAt < :now")
    List<CounterOffer> findExpiredCounterOffers(@Param("now") LocalDateTime now);

    /**
     * Count counter-offers for a quotation
     */
    long countByQuotationId(Long quotationId);

    /**
     * Count counter-offers for a quotation with a specific status
     */
    long countByQuotationIdAndStatus(Long quotationId, CounterOfferStatus status);

    /**
     * Check if a quotation has any pending counter-offers
     */
    boolean existsByQuotationIdAndStatus(Long quotationId, CounterOfferStatus status);

    /**
     * Find counter-offers for a booking with a specific status
     */
    List<CounterOffer> findByBookingIdAndStatusOrderByCreatedAtDesc(Long bookingId, CounterOfferStatus status);

    /**
     * Find counter-offers made by a user for a specific quotation
     */
    List<CounterOffer> findByQuotationIdAndOfferedByUserIdOrderByCreatedAtDesc(Long quotationId, Long userId);
}

