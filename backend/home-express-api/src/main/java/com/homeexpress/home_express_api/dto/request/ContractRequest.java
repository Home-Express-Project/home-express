package com.homeexpress.home_express_api.dto.request;

import jakarta.validation.constraints.NotNull;

public class ContractRequest {

    @NotNull(message = "Quotation ID is required")
    private Long quotationId;

    @NotNull(message = "Terms and conditions are required")
    private String termsAndConditions;

    public ContractRequest() {
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }
}
