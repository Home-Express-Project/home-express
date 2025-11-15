package com.homeexpress.home_express_api.dto;

import java.util.ArrayList;
import java.util.List;

public class SettlementEligibilityDTO {

    private boolean eligible;
    private List<String> reasons;
    private Long bookingId;
    private String bookingStatus;
    private Long agreedPriceVnd;
    private Long totalCollectedVnd;
    private boolean fullyPaid;
    private int openIncidentCount;

    public SettlementEligibilityDTO() {
        this.reasons = new ArrayList<>();
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void addReason(String reason) {
        this.reasons.add(reason);
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Long getAgreedPriceVnd() {
        return agreedPriceVnd;
    }

    public void setAgreedPriceVnd(Long agreedPriceVnd) {
        this.agreedPriceVnd = agreedPriceVnd;
    }

    public Long getTotalCollectedVnd() {
        return totalCollectedVnd;
    }

    public void setTotalCollectedVnd(Long totalCollectedVnd) {
        this.totalCollectedVnd = totalCollectedVnd;
    }

    public boolean isFullyPaid() {
        return fullyPaid;
    }

    public void setFullyPaid(boolean fullyPaid) {
        this.fullyPaid = fullyPaid;
    }

    public int getOpenIncidentCount() {
        return openIncidentCount;
    }

    public void setOpenIncidentCount(int openIncidentCount) {
        this.openIncidentCount = openIncidentCount;
    }
}
