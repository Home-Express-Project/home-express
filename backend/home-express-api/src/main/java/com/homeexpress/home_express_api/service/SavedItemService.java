package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.SaveItemRequest;
import com.homeexpress.home_express_api.dto.response.SavedItemResponse;
import com.homeexpress.home_express_api.entity.SavedItem;
import com.homeexpress.home_express_api.repository.SavedItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing customer saved items.
 * Handles CRUD operations for saved items with proper authorization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SavedItemService {

    private final SavedItemRepository savedItemRepository;

    /**
     * Get all saved items for a customer
     */
    public List<SavedItemResponse> getSavedItems(Long customerId) {
        log.debug("Fetching saved items for customer {}", customerId);
        
        return savedItemRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Save a single item
     */
    public SavedItemResponse saveSingleItem(Long customerId, SaveItemRequest request) {
        log.info("Saving item '{}' for customer {}", request.getName(), customerId);
        
        SavedItem item = new SavedItem();
        item.setCustomerId(customerId);
        item.setName(request.getName());
        item.setBrand(request.getBrand());
        item.setModel(request.getModel());
        item.setCategoryId(request.getCategoryId());
        item.setSize(request.getSize());
        item.setWeightKg(request.getWeightKg());
        item.setDimensions(request.getDimensions());
        item.setDeclaredValueVnd(request.getDeclaredValueVnd());
        item.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        item.setIsFragile(request.getIsFragile() != null ? request.getIsFragile() : false);
        item.setRequiresDisassembly(request.getRequiresDisassembly() != null ? request.getRequiresDisassembly() : false);
        item.setRequiresPackaging(request.getRequiresPackaging() != null ? request.getRequiresPackaging() : false);
        item.setNotes(request.getNotes());
        item.setMetadata(request.getMetadata());
        
        SavedItem saved = savedItemRepository.save(item);
        return convertToResponse(saved);
    }

    /**
     * Save multiple items at once
     */
    public int saveMultipleItems(Long customerId, List<SaveItemRequest> items) {
        log.info("Saving {} items for customer {}", items.size(), customerId);
        
        List<SavedItem> savedItems = items.stream()
                .map(request -> {
                    SavedItem item = new SavedItem();
                    item.setCustomerId(customerId);
                    item.setName(request.getName());
                    item.setBrand(request.getBrand());
                    item.setModel(request.getModel());
                    item.setCategoryId(request.getCategoryId());
                    item.setSize(request.getSize());
                    item.setWeightKg(request.getWeightKg());
                    item.setDimensions(request.getDimensions());
                    item.setDeclaredValueVnd(request.getDeclaredValueVnd());
                    item.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
                    item.setIsFragile(request.getIsFragile() != null ? request.getIsFragile() : false);
                    item.setRequiresDisassembly(request.getRequiresDisassembly() != null ? request.getRequiresDisassembly() : false);
                    item.setRequiresPackaging(request.getRequiresPackaging() != null ? request.getRequiresPackaging() : false);
                    item.setNotes(request.getNotes());
                    item.setMetadata(request.getMetadata());
                    return item;
                })
                .collect(Collectors.toList());
        
        savedItemRepository.saveAll(savedItems);
        return savedItems.size();
    }

    /**
     * Delete a single saved item
     */
    public void deleteSavedItem(Long itemId, Long customerId) {
        log.info("Deleting saved item {} for customer {}", itemId, customerId);
        
        SavedItem item = savedItemRepository.findByIdAndCustomerId(itemId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Saved item not found or not authorized"));
        
        savedItemRepository.delete(item);
    }

    /**
     * Delete multiple saved items
     */
    public int deleteMultipleSavedItems(Long customerId, List<Long> itemIds) {
        log.info("Deleting {} items for customer {}", itemIds.size(), customerId);
        
        int count = 0;
        for (Long itemId : itemIds) {
            if (savedItemRepository.findByIdAndCustomerId(itemId, customerId).isPresent()) {
                savedItemRepository.deleteById(itemId);
                count++;
            }
        }
        return count;
    }

    /**
     * Delete all saved items for a customer
     */
    public void deleteAllSavedItems(Long customerId) {
        log.info("Deleting all saved items for customer {}", customerId);
        
        savedItemRepository.deleteByCustomerId(customerId);
    }

    /**
     * Update a saved item
     */
    public SavedItemResponse updateSavedItem(Long itemId, Long customerId, SaveItemRequest request) {
        log.info("Updating saved item {} for customer {}", itemId, customerId);
        
        SavedItem item = savedItemRepository.findByIdAndCustomerId(itemId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Saved item not found or not authorized"));
        
        item.setName(request.getName());
        item.setBrand(request.getBrand());
        item.setModel(request.getModel());
        item.setCategoryId(request.getCategoryId());
        item.setSize(request.getSize());
        item.setWeightKg(request.getWeightKg());
        item.setDimensions(request.getDimensions());
        item.setDeclaredValueVnd(request.getDeclaredValueVnd());
        item.setQuantity(request.getQuantity());
        item.setIsFragile(request.getIsFragile());
        item.setRequiresDisassembly(request.getRequiresDisassembly());
        item.setRequiresPackaging(request.getRequiresPackaging());
        item.setNotes(request.getNotes());
        item.setMetadata(request.getMetadata());
        
        SavedItem updated = savedItemRepository.save(item);
        return convertToResponse(updated);
    }

    // Helper method to convert entity to response DTO
    private SavedItemResponse convertToResponse(SavedItem item) {
        return new SavedItemResponse(
                item.getSavedItemId(),
                item.getName(),
                item.getBrand(),
                item.getModel(),
                item.getCategoryId(),
                item.getSize(),
                item.getWeightKg(),
                item.getDimensions(),
                item.getDeclaredValueVnd(),
                item.getQuantity(),
                item.getIsFragile(),
                item.getRequiresDisassembly(),
                item.getRequiresPackaging(),
                item.getNotes(),
                item.getMetadata(),
                item.getCreatedAt()
        );
    }
}
