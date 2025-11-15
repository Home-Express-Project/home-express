package com.homeexpress.home_express_api.entity;

public enum TokenType {
    VERIFY_EMAIL,      // xac thuc email sau register
    RESET_PASSWORD,    // reset password quen mat khau
    INVITE,           // moi manager moi
    MFA_RECOVERY      // recovery codes cho 2FA (future)
}
