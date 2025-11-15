package com.homeexpress.home_express_api.integration.payout;

/**
 * Result returned by the external payout gateway integration.
 */
public class ExternalPayoutResult {

    private final boolean success;
    private final String transactionReference;
    private final String failureReason;
    private final boolean retryable;

    private ExternalPayoutResult(boolean success, String transactionReference, String failureReason, boolean retryable) {
        this.success = success;
        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
        this.retryable = retryable;
    }

    public static ExternalPayoutResult success(String transactionReference) {
        return new ExternalPayoutResult(true, transactionReference, null, true);
    }

    public static ExternalPayoutResult failure(String failureReason, boolean retryable) {
        return new ExternalPayoutResult(false, null, failureReason, retryable);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
