package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtDesc(Long bookingId);

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAsc(Long bookingId);

    int countByBookingId(Long bookingId);
}
