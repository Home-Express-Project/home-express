package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.BookingItem;
import com.homeexpress.home_express_api.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    @Query("""
            SELECT COALESCE(cat.name, 'Khác'), COUNT(DISTINCT bi.bookingId), COALESCE(SUM(b.finalPrice),0), 0.0
            FROM BookingItem bi
            JOIN bi.booking b
            LEFT JOIN bi.category cat
            WHERE b.transportId = :transportId
              AND b.status = :status
              AND ((b.actualEndTime IS NOT NULL AND b.actualEndTime BETWEEN :start AND :end)
                   OR (b.actualEndTime IS NULL AND b.updatedAt BETWEEN :start AND :end))
            GROUP BY bi.categoryId, cat.name
            ORDER BY COALESCE(SUM(b.finalPrice),0) DESC
            """)
    List<Object[]> aggregateCategoryPerformance(@Param("transportId") Long transportId,
                                                @Param("status") BookingStatus status,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    void deleteByBookingId(Long bookingId);

    List<BookingItem> findByBookingId(Long bookingId);

    long countByBookingId(Long bookingId);

    Long countByCategoryCategoryId(Long categoryId);

    @Query("SELECT COUNT(DISTINCT bi) FROM BookingItem bi WHERE bi.category.categoryId = :categoryId AND bi.booking.status IN :statuses")
    Long countByCategoryCategoryIdAndBookingStatusIn(@Param("categoryId") Long categoryId, @Param("statuses") List<BookingStatus> statuses);
}
