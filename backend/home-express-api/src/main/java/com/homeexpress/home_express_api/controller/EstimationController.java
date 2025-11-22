package com.homeexpress.home_express_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.homeexpress.home_express_api.dto.estimation.AutoEstimationRequest;
import com.homeexpress.home_express_api.dto.estimation.AutoEstimationResponse;
import com.homeexpress.home_express_api.service.EstimationService;

@RestController
@RequestMapping("/api/v1/estimation")
public class EstimationController {

    private final EstimationService estimationService;

    public EstimationController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @PostMapping("/auto")
    public ResponseEntity<AutoEstimationResponse> autoEstimate(@Valid @RequestBody AutoEstimationRequest request) {
        AutoEstimationResponse response = estimationService.generateAutoEstimation(request);
        return ResponseEntity
                .status(response.isSuccess() ? 200 : 422)
                .body(response);
    }
}
