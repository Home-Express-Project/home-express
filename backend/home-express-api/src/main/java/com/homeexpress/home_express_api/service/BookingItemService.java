package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import com.homeexpress.home_express_api.dto.ai.EnhancedDetectedItem;
import com.homeexpress.home_express_api.dto.booking.BookingItemsPersistResponse;
import com.homeexpress.home_express_api.entity.Booking;
import com.homeexpress.home_express_api.entity.BookingItem;
import com.homeexpress.home_express_api.entity.Category;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.BookingItemRepository;
import com.homeexpress.home_express_api.repository.BookingRepository;
import com.homeexpress.home_express_api.repository.CategoryRepository;
import com.homeexpress.home_express_api.service.ai.AIDetectionMapper;
import com.homeexpress.home_express_api.service.ai.AiCategoryMappingService;
import com.homeexpress.home_express_api.service.ai.AiCategoryMappingService.CategorySizeMapping;
import jakarta.transaction.Transactional;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handles persistence of booking items, including AI-detected payloads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingItemService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final CategoryRepository categoryRepository;
    private final AIDetectionMapper detectionMapper;
    private final AiCategoryMappingService aiCategoryMappingService;

    @Transactional
    public BookingItemsPersistResponse persistDetectedItems(Long bookingId,
                                                            DetectionResult detectionResult,
                                                            boolean replaceExisting) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        List<EnhancedDetectedItem> enhancedItems = coalesceEnhancedItems(detectionResult);
        if (enhancedItems.isEmpty()) {
            throw new IllegalArgumentException("No detected items available to persist");
        }

        List<Category> categories = categoryRepository.findAll();

        if (replaceExisting) {
            bookingItemRepository.deleteByBookingId(bookingId);
        }

        List<BookingItem> entities = new ArrayList<>();
        for (EnhancedDetectedItem item : enhancedItems) {
            if (!hasText(item.getName())) {
                continue;
            }

            CategorySizeMapping mapping = aiCategoryMappingService.map(item);
            Long categoryId = mapping.categoryId();
            Long sizeId = mapping.sizeId();

            if (categoryId == null) {
                categoryId = resolveCategoryId(item, categories);
            }

            BookingItem entity = detectionMapper.toBookingItem(item, bookingId, categoryId, sizeId);

            if (!hasText(entity.getName())) {
                entity.setName("Detected Item");
            }

            entity.setAiMetadata(detectionMapper.toAIMetadataJson(item));
            entities.add(entity);
        }

        if (entities.isEmpty()) {
            throw new IllegalArgumentException("Detected items could not be mapped to booking items");
        }

        List<BookingItem> savedItems = bookingItemRepository.saveAll(entities);
        log.info("Persisted {} AI detected items for booking {}", savedItems.size(), bookingId);

        return BookingItemsPersistResponse.fromEntities(booking.getBookingId(), replaceExisting, savedItems);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<BookingItem> getBookingItems(Long bookingId) {
        bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        return bookingItemRepository.findByBookingId(bookingId);
    }

    private List<EnhancedDetectedItem> coalesceEnhancedItems(DetectionResult detectionResult) {
        if (detectionResult == null) {
            return List.of();
        }

        List<EnhancedDetectedItem> enhanced = detectionResult.getEnhancedItems();
        if (enhanced != null && !enhanced.isEmpty()) {
            return enhanced.stream()
                .filter(Objects::nonNull)
                .filter(item -> hasText(item.getName()))
                .toList();
        }

        if (detectionResult.getItems() == null || detectionResult.getItems().isEmpty()) {
            return List.of();
        }

        return detectionResult.getItems().stream()
            .map(EnhancedDetectedItem::fromDetectedItem)
            .filter(Objects::nonNull)
            .filter(item -> hasText(item.getName()))
            .toList();
    }

    private Long resolveCategoryId(EnhancedDetectedItem item, List<Category> categories) {
        List<String> candidates = new ArrayList<>();
        if (hasText(item.getCategory())) {
            candidates.add(item.getCategory());
        }
        if (hasText(item.getSubcategory())) {
            candidates.add(item.getSubcategory());
        }
        if (hasText(item.getName())) {
            candidates.addAll(extractNameTokens(item.getName()));
        }

        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (!hasText(normalizedCandidate)) {
                continue;
            }

            for (Category category : categories) {
                if (matchesCategory(normalizedCandidate, category)) {
                    return category.getCategoryId();
                }
            }
        }

        return null;
    }

    private List<String> extractNameTokens(String name) {
        String normalized = normalize(name);
        if (!hasText(normalized)) {
            return List.of();
        }

        String[] parts = normalized.split(" ");
        List<String> tokens = new ArrayList<>();

        for (String part : parts) {
            if (part.length() >= 3) {
                tokens.add(part);
            }
        }

        if (normalized.contains(" ")) {
            tokens.add(normalized.replace(" ", ""));
        }

        return tokens;
    }

    private boolean matchesCategory(String candidate, Category category) {
        String name = normalize(category.getName());
        String nameEn = normalize(category.getNameEn());

        return candidate.equals(name)
            || candidate.equals(nameEn)
            || (name != null && name.contains(candidate))
            || (nameEn != null && nameEn.contains(candidate));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-z0-9 ]", " ");
        return normalized.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
