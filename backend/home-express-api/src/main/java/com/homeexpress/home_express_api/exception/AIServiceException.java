package com.homeexpress.home_express_api.exception;

/**
 * Exception thrown when AI detection services fail
 */
public class AIServiceException extends RuntimeException {
    
    private final String serviceName;
    private final String errorCode;
    
    public AIServiceException(String message) {
        super(message);
        this.serviceName = "UNKNOWN";
        this.errorCode = "AI_SERVICE_ERROR";
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = "UNKNOWN";
        this.errorCode = "AI_SERVICE_ERROR";
    }
    
    public AIServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = "AI_SERVICE_ERROR";
    }
    
    public AIServiceException(String serviceName, String errorCode, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
