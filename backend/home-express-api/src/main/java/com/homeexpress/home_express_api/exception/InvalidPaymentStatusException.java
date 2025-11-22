package com.homeexpress.home_express_api.exception;

import com.homeexpress.home_express_api.entity.PaymentStatus;

/**
 * Exception thrown when an invalid payment status operation is attempted
 */
public class InvalidPaymentStatusException extends RuntimeException {
    
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
    
    public InvalidPaymentStatusException(PaymentStatus currentStatus, String operation) {
        super(String.format("Cannot %s payment with status: %s", operation, currentStatus));
    }
}

