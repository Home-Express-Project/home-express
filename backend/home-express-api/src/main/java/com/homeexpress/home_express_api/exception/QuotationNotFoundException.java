package com.homeexpress.home_express_api.exception;

/**
 * Exception thrown when a quotation is not found
 */
public class QuotationNotFoundException extends ResourceNotFoundException {
    
    public QuotationNotFoundException(Long quotationId) {
        super("Quotation", "id", quotationId);
    }
    
    public QuotationNotFoundException(String message) {
        super(message);
    }
}

