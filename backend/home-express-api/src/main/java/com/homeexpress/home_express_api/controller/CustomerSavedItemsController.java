package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.request.SaveItemRequest;
import com.homeexpress.home_express_api.dto.request.SaveItemsRequest;
import com.homeexpress.home_express_api.dto.response.SavedItemResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.service.SavedItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for customer saved items operations.
 * Handles saving, retrieving, updating, and deleting saved items.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer/saved-items")
@RequiredArgsConstructor
public class CustomerSavedItemsController {

    private final SavedItemService savedItemService;

    /**
     * Get all saved items for the current customer
     * GET /api/v1/customer/saved-items
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSavedItems(
            @AuthenticationPrincipal User user) {
        
        log.debug("Customer {} fetching saved items", user.getUserId());
        
        List<SavedItemResponse> items = savedItemService.getSavedItems(user.getUserId());
        
        return ResponseEntity.ok(Map.of(
                "items", items,
                "count", items.size()
        ));
    }

    /**
     * Save a single item
     * POST /api/v1/customer/saved-items/single
     */
    @PostMapping("/single")
    public ResponseEntity<SavedItemResponse> saveSingleItem(
            @Valid @RequestBody SaveItemRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} saving single item '{}'", user.getUserId(), request.getName());
        
        SavedItemResponse response = savedItemService.saveSingleItem(user.getUserId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Save multiple items at once
     * POST /api/v1/customer/saved-items
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMultipleItems(
            @Valid @RequestBody SaveItemsRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} saving {} items", user.getUserId(), request.getItems().size());
        
        int count = savedItemService.saveMultipleItems(user.getUserId(), request.getItems());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "count", count,
                "message", count + " item(s) saved successfully"
        ));
    }

    /**
     * Update a saved item
     * PUT /api/v1/customer/saved-items/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SavedItemResponse> updateSavedItem(
            @PathVariable Long id,
            @Valid @RequestBody SaveItemRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} updating saved item {}", user.getUserId(), id);
        
        SavedItemResponse response = savedItemService.updateSavedItem(id, user.getUserId(), request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a single saved item
     * DELETE /api/v1/customer/saved-items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSavedItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} deleting saved item {}", user.getUserId(), id);
        
        savedItemService.deleteSavedItem(id, user.getUserId());
        
        return ResponseEntity.ok(Map.of(
                "message", "Saved item deleted successfully"
        ));
    }

    /**
     * Delete multiple saved items
     * DELETE /api/v1/customer/saved-items
     * Body: { "ids": [1, 2, 3] }
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteMultipleSavedItems(
            @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal User user) {
        
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "IDs list is required"
            ));
        }
        
        log.info("Customer {} deleting {} saved items", user.getUserId(), ids.size());
        
        int deletedCount = savedItemService.deleteMultipleSavedItems(user.getUserId(), ids);
        
        return ResponseEntity.ok(Map.of(
                "count", deletedCount,
                "message", deletedCount + " item(s) deleted successfully"
        ));
    }

    /**
     * Delete all saved items
     * DELETE /api/v1/customer/saved-items/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllSavedItems(
            @AuthenticationPrincipal User user) {
        
        log.info("Customer {} deleting all saved items", user.getUserId());
        
        savedItemService.deleteAllSavedItems(user.getUserId());
        
        return ResponseEntity.ok(Map.of(
                "message", "All saved items deleted successfully"
        ));
    }
}
