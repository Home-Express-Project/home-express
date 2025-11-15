package com.homeexpress.home_express_api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReadyToQuoteStatusResponse {

    private boolean readyToQuote;
    private String reason;
    private int rateCardsCount;
    private int expiredCardsCount;
    private LocalDateTime nextExpiryAt;

    public boolean isReadyToQuote() {
        return readyToQuote;
    }

    public void setReadyToQuote(boolean readyToQuote) {
        this.readyToQuote = readyToQuote;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getRateCardsCount() {
        return rateCardsCount;
    }

    public void setRateCardsCount(int rateCardsCount) {
        this.rateCardsCount = rateCardsCount;
    }

    public int getExpiredCardsCount() {
        return expiredCardsCount;
    }

    public void setExpiredCardsCount(int expiredCardsCount) {
        this.expiredCardsCount = expiredCardsCount;
    }

    public LocalDateTime getNextExpiryAt() {
        return nextExpiryAt;
    }

    public void setNextExpiryAt(LocalDateTime nextExpiryAt) {
        this.nextExpiryAt = nextExpiryAt;
    }
}

