package com.homeexpress.home_express_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPricingListResponse {
    private List<CategoryPricingResponse> pricingRules;
}
