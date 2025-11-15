package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.entity.ProductModel;
import com.homeexpress.home_express_api.service.intake.ProductModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/product-models")
public class ProductModelController {

    private final ProductModelService productModelService;

    public ProductModelController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }

    @GetMapping("/brands/search")
    public ResponseEntity<Map<String, Object>> searchBrands(@RequestParam(required = false) String q) {
        List<String> brands = productModelService.searchBrands(q);
        Map<String, Object> response = new HashMap<>();
        response.put("brands", brands);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models/search")
    public ResponseEntity<Map<String, Object>> searchModels(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String brand) {
        
        List<ProductModel> models;
        if (brand != null && !brand.isBlank()) {
            models = productModelService.findModelsByBrand(brand, q);
        } else {
            models = productModelService.searchModels(q);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("models", models);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductModel> saveModel(@RequestBody ProductModel model) {
        ProductModel saved = productModelService.saveOrUpdateModel(model);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{modelId}/use")
    public ResponseEntity<Void> recordUsage(@PathVariable Long modelId) {
        productModelService.recordUsage(modelId);
        return ResponseEntity.ok().build();
    }
}
