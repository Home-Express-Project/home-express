package com.homeexpress.home_express_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmCompletionRequest {
    
    private String feedback; // Optional customer feedback
    private Integer rating; // Optional rating (1-5)
}

