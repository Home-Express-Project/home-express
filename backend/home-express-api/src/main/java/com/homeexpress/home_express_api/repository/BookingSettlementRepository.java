package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.BookingSettlement;
import com.homeexpress.home_express_api.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSettlementRepository extends JpaRepository<BookingSettlement, Long> {

    Optional<BookingSettlement> findByBookingId(Long bookingId);

    List<BookingSettlement> findByStatusIn(List<SettlementStatus> statuses);

    List<BookingSettlement> findByTransportIdAndStatus(Long transportId, SettlementStatus status);

    List<BookingSettlement> findByTransportId(Long transportId);

    @Query("SELECT s FROM BookingSettlement s WHERE s.transportId = :transportId " +
           "AND s.status IN :statuses ORDER BY s.createdAt DESC")
    List<BookingSettlement> findByTransportIdAndStatusIn(
        @Param("transportId") Long transportId,
        @Param("statuses") List<SettlementStatus> statuses
    );

    @Query("SELECT s FROM BookingSettlement s WHERE s.status = :status " +
           "ORDER BY s.readyAt DESC")
    List<BookingSettlement> findByStatusOrderByReadyAtDesc(@Param("status") SettlementStatus status);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT DISTINCT s.transportId FROM BookingSettlement s WHERE s.status = 'READY'")
    List<Long> findTransportsWithReadySettlements();

    @Query("SELECT s FROM BookingSettlement s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<BookingSettlement> findByStatus(@Param("status") SettlementStatus status);

    @Query("SELECT s FROM BookingSettlement s ORDER BY s.createdAt DESC")
    List<BookingSettlement> findAllOrderByCreatedAtDesc();
}
