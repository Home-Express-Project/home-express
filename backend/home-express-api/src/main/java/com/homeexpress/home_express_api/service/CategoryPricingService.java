package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.CategoryPricingRequest;
import com.homeexpress.home_express_api.dto.response.CategoryPricingResponse;
import com.homeexpress.home_express_api.entity.Category;
import com.homeexpress.home_express_api.entity.CategoryPricing;
import com.homeexpress.home_express_api.entity.Size;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.repository.CategoryPricingRepository;
import com.homeexpress.home_express_api.repository.CategoryRepository;
import com.homeexpress.home_express_api.repository.SizeRepository;
import com.homeexpress.home_express_api.repository.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryPricingService {

    @Autowired
    private CategoryPricingRepository categoryPricingRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Transactional
    public CategoryPricingResponse createCategoryPricing(CategoryPricingRequest request) {
        Transport transport = transportRepository.findById(request.getTransportId())
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + request.getTransportId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        Size size = null;
        if (request.getSizeId() != null) {
            size = sizeRepository.findById(request.getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found with ID: " + request.getSizeId()));
        }

        if (request.getValidTo() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new RuntimeException("Valid to date must be after valid from date");
        }

        boolean hasOverlap = categoryPricingRepository.hasOverlappingActivePricing(
                request.getTransportId(),
                request.getCategoryId(),
                request.getSizeId(),
                request.getValidFrom(),
                request.getValidTo(),
                null
        );

        if (hasOverlap) {
            throw new RuntimeException("Overlapping active pricing exists for this category and size combination");
        }

        List<CategoryPricing> activePricings = categoryPricingRepository.findActiveByCategoryAndSize(
                request.getTransportId(),
                request.getCategoryId(),
                request.getSizeId()
        );

        for (CategoryPricing activePricing : activePricings) {
            activePricing.setIsActive(false);
            if (activePricing.getValidTo() == null) {
                activePricing.setValidTo(request.getValidFrom().minusSeconds(1));
            }
            categoryPricingRepository.save(activePricing);
        }

        CategoryPricing categoryPricing = new CategoryPricing();
        categoryPricing.setTransport(transport);
        categoryPricing.setCategory(category);
        categoryPricing.setSize(size);
        categoryPricing.setPricePerUnitVnd(request.getPricePerUnitVnd());
        categoryPricing.setFragileMultiplier(request.getFragileMultiplier());
        categoryPricing.setDisassemblyMultiplier(request.getDisassemblyMultiplier());
        categoryPricing.setHeavyMultiplier(request.getHeavyMultiplier());
        categoryPricing.setHeavyThresholdKg(request.getHeavyThresholdKg());
        categoryPricing.setValidFrom(request.getValidFrom());
        categoryPricing.setValidTo(request.getValidTo());
        categoryPricing.setIsActive(true);

        CategoryPricing saved = categoryPricingRepository.save(categoryPricing);
        return CategoryPricingResponse.fromEntity(saved);
    }

    public List<CategoryPricingResponse> getAllCategoryPricing() {
        return categoryPricingRepository.findAll().stream()
                .map(CategoryPricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoryPricingResponse> getCategoryPricingByTransport(Long transportId) {
        return categoryPricingRepository.findByTransport_TransportId(transportId).stream()
                .map(CategoryPricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoryPricingResponse> getCategoryPricingByCategory(Long categoryId) {
        return categoryPricingRepository.findByCategory_CategoryId(categoryId).stream()
                .map(CategoryPricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoryPricingResponse> getActiveCategoryPricing() {
        return categoryPricingRepository.findByIsActive(true).stream()
                .map(CategoryPricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryPricingResponse getCurrentActivePricing(Long transportId, Long categoryId, Long sizeId) {
        LocalDateTime now = LocalDateTime.now();
        
        return categoryPricingRepository.findActiveByCategoryAndSizeAndDate(transportId, categoryId, sizeId, now)
                .map(CategoryPricingResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("No active pricing found for category ID: " + categoryId + 
                        (sizeId != null ? " and size ID: " + sizeId : "")));
    }

    public CategoryPricingResponse getCategoryPricingById(Long id) {
        CategoryPricing pricing = categoryPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category pricing not found with ID: " + id));
        return CategoryPricingResponse.fromEntity(pricing);
    }

    @Transactional
    public CategoryPricingResponse updateCategoryPricing(Long id, CategoryPricingRequest request) {
        CategoryPricing existing = categoryPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category pricing not found with ID: " + id));

        if (request.getValidTo() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new RuntimeException("Valid to date must be after valid from date");
        }

        boolean hasOverlap = categoryPricingRepository.hasOverlappingActivePricing(
                request.getTransportId(),
                request.getCategoryId(),
                request.getSizeId(),
                request.getValidFrom(),
                request.getValidTo(),
                id
        );

        if (hasOverlap) {
            throw new RuntimeException("Overlapping active pricing exists for this category and size combination");
        }

        existing.setPricePerUnitVnd(request.getPricePerUnitVnd());
        existing.setFragileMultiplier(request.getFragileMultiplier());
        existing.setDisassemblyMultiplier(request.getDisassemblyMultiplier());
        existing.setHeavyMultiplier(request.getHeavyMultiplier());
        existing.setHeavyThresholdKg(request.getHeavyThresholdKg());
        existing.setValidFrom(request.getValidFrom());
        existing.setValidTo(request.getValidTo());

        CategoryPricing updated = categoryPricingRepository.save(existing);
        return CategoryPricingResponse.fromEntity(updated);
    }

    @Transactional
    public void deactivateCategoryPricing(Long id) {
        CategoryPricing pricing = categoryPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category pricing not found with ID: " + id));
        
        pricing.setIsActive(false);
        if (pricing.getValidTo() == null) {
            pricing.setValidTo(LocalDateTime.now());
        }
        categoryPricingRepository.save(pricing);
    }
}
