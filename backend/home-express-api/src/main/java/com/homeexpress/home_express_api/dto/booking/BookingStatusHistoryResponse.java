package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.ActorRole;
import com.homeexpress.home_express_api.entity.BookingStatus;
import com.homeexpress.home_express_api.entity.BookingStatusHistory;
import java.time.LocalDateTime;

public class BookingStatusHistoryResponse {

    private Long id;
    private Long bookingId;
    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private Long changedBy;
    private ActorRole changedByRole;
    private String reason;
    private LocalDateTime changedAt;

    public BookingStatusHistoryResponse() {
    }

    public static BookingStatusHistoryResponse fromEntity(BookingStatusHistory history) {
        BookingStatusHistoryResponse response = new BookingStatusHistoryResponse();
        response.setId(history.getId());
        response.setBookingId(history.getBookingId());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedBy(history.getChangedBy());
        response.setChangedByRole(history.getChangedByRole());
        response.setReason(history.getReason());
        response.setChangedAt(history.getChangedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BookingStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(BookingStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public BookingStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(BookingStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public ActorRole getChangedByRole() {
        return changedByRole;
    }

    public void setChangedByRole(ActorRole changedByRole) {
        this.changedByRole = changedByRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
