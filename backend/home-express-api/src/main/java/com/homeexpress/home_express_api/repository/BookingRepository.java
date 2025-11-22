package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status);

    List<Booking> findByTransportId(Long transportId);

    List<Booking> findByTransportIdAndStatus(Long transportId, BookingStatus status);

    List<Booking> findByTransportIdAndStatusInOrderByPreferredDateAsc(Long transportId, List<BookingStatus> statuses);

    long countByTransportId(Long transportId);

    long countByTransportIdAndStatus(Long transportId, BookingStatus status);

    List<Booking> findByTransportIdAndCreatedAtBetween(Long transportId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByTransportIdAndStatusAndActualEndTimeBetween(Long transportId, BookingStatus status,
                                                                    LocalDateTime start, LocalDateTime end);

    List<Booking> findByTransportIdAndStatusAndUpdatedAtBetween(Long transportId, BookingStatus status,
                                                                LocalDateTime start, LocalDateTime end);

    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :customerId " +
           "AND b.preferredDate BETWEEN :startDate AND :endDate " +
           "ORDER BY b.preferredDate DESC")
    List<Booking> findByCustomerIdAndDateRange(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Booking b WHERE b.preferredDate BETWEEN :startDate AND :endDate " +
           "ORDER BY b.preferredDate DESC")
    List<Booking> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Booking> findByStatusIn(@Param("statuses") List<BookingStatus> statuses);

    boolean existsByBookingIdAndCustomerId(Long bookingId, Long customerId);

    List<Booking> findByStatusInAndTransportIdIsNullOrderByCreatedAtDesc(List<BookingStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.finalPrice),0) FROM Booking b WHERE b.transportId = :transportId AND b.status = :status")
    BigDecimal sumFinalPriceByTransportAndStatus(@Param("transportId") Long transportId,
                                                 @Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.finalPrice),0) FROM Booking b WHERE b.customerId = :customerId AND b.status = :status")
    BigDecimal sumFinalPriceByCustomerAndStatus(@Param("customerId") Long customerId,
                                                @Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.finalPrice),0) FROM Booking b WHERE b.transportId = :transportId AND b.status = :status " +
            "AND ((b.actualEndTime IS NOT NULL AND b.actualEndTime BETWEEN :start AND :end) " +
            "OR (b.actualEndTime IS NULL AND b.updatedAt BETWEEN :start AND :end))")
    BigDecimal sumFinalPriceByTransportAndStatusBetween(@Param("transportId") Long transportId,
                                                        @Param("status") BookingStatus status,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    long countByCustomerIdAndStatusIn(Long customerId, List<BookingStatus> statuses);

    long countByTransportIdAndStatusIn(Long transportId, List<BookingStatus> statuses);
}
