package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.request.SaveItemRequest;
import com.homeexpress.home_express_api.dto.request.SaveItemsRequest;
import com.homeexpress.home_express_api.dto.response.SavedItemResponse;
import com.homeexpress.home_express_api.entity.Customer;
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
     * Helper to extract user ID from principal
     */
    private Long getUserId(Object principal) {
        if (principal instanceof Customer) {
            return ((Customer) principal).getCustomerId();
        } else if (principal instanceof User) {
            return ((User) principal).getUserId();
        } else if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new IllegalArgumentException("Unknown principal type: " + (principal != null ? principal.getClass().getName() : "null"));
    }

    /**
     * Get all saved items for the current customer
     * GET /api/v1/customer/saved-items
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSavedItems(
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.debug("Customer {} fetching saved items", userId);
        
        List<SavedItemResponse> items = savedItemService.getSavedItems(userId);
        
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
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.info("Customer {} saving single item '{}'", userId, request.getName());
        
        SavedItemResponse response = savedItemService.saveSingleItem(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Save multiple items at once
     * POST /api/v1/customer/saved-items
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> saveMultipleItems(
            @Valid @RequestBody SaveItemsRequest request,
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.info("Customer {} saving {} items", userId, request.getItems().size());
        
        int count = savedItemService.saveMultipleItems(userId, request.getItems());
        
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
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.info("Customer {} updating saved item {}", userId, id);
        
        SavedItemResponse response = savedItemService.updateSavedItem(id, userId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a single saved item
     * DELETE /api/v1/customer/saved-items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSavedItem(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.info("Customer {} deleting saved item {}", userId, id);
        
        savedItemService.deleteSavedItem(id, userId);
        
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
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "IDs list is required"
            ));
        }
        
        log.info("Customer {} deleting {} saved items", userId, ids.size());
        
        int deletedCount = savedItemService.deleteMultipleSavedItems(userId, ids);
        
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
            @AuthenticationPrincipal Object principal) {
        
        Long userId = getUserId(principal);
        log.info("Customer {} deleting all saved items", userId);
        
        savedItemService.deleteAllSavedItems(userId);
        
        return ResponseEntity.ok(Map.of(
                "message", "All saved items deleted successfully"
        ));
    }
}
