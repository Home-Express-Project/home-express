package com.homeexpress.home_express_api.controller.transport;

import com.homeexpress.home_express_api.dto.response.ApiResponse;
import com.homeexpress.home_express_api.dto.response.TransportCategoryResponse;
import com.homeexpress.home_express_api.service.TransportCategoryService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transport/categories")
public class TransportCategoryController {

    private final TransportCategoryService transportCategoryService;

    public TransportCategoryController(TransportCategoryService transportCategoryService) {
        this.transportCategoryService = transportCategoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCategories(
            @RequestParam(name = "isActive", required = false, defaultValue = "true") boolean isActive) {
        List<TransportCategoryResponse> categories = transportCategoryService.getCategories(isActive);
        Map<String, Object> data = Map.of("categories", categories);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
