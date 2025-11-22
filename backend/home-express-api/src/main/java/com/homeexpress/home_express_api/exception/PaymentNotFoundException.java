package com.homeexpress.home_express_api.exception;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends ResourceNotFoundException {
    
    public PaymentNotFoundException(Long paymentId) {
        super("Payment", "id", paymentId);
    }
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
}

