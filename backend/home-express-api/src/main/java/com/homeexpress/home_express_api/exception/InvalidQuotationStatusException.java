package com.homeexpress.home_express_api.exception;

import com.homeexpress.home_express_api.entity.QuotationStatus;

/**
 * Exception thrown when an invalid quotation status operation is attempted
 */
public class InvalidQuotationStatusException extends RuntimeException {
    
    public InvalidQuotationStatusException(String message) {
        super(message);
    }
    
    public InvalidQuotationStatusException(QuotationStatus currentStatus, String operation) {
        super(String.format("Cannot %s quotation with status: %s", operation, currentStatus));
    }
}

