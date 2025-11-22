package com.homeexpress.home_express_api.exception;

// Exception khi user không có quyền truy cập
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
