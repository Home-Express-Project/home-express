package com.homeexpress.home_express_api.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.homeexpress.home_express_api.entity.PaymentMethod;

/**
 * Request-level payment method values accepted by public customer payment APIs.
 * JSON payloads should use lowercase values: "cash" or "bank".
 */
public enum PaymentMethodRequest {

    CASH("cash", PaymentMethod.CASH),
    BANK("bank", PaymentMethod.BANK_TRANSFER);

    private final String value;
    private final PaymentMethod entityMethod;

    PaymentMethodRequest(String value, PaymentMethod entityMethod) {
        this.value = value;
        this.entityMethod = entityMethod;
    }

    @JsonCreator
    public static PaymentMethodRequest fromValue(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String normalized = raw.trim().toLowerCase();
        for (PaymentMethodRequest candidate : values()) {
            if (candidate.value.equals(normalized)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unsupported payment method: " + raw);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Map request-level value to the canonical entity enum used in persistence.
     */
    public PaymentMethod toEntityMethod() {
        return entityMethod;
    }
}

