package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.entity.ProductModel;
import com.homeexpress.home_express_api.repository.ProductModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductModelService {

    private final ProductModelRepository productModelRepository;

    public ProductModelService(ProductModelRepository productModelRepository) {
        this.productModelRepository = productModelRepository;
    }

    public List<String> searchBrands(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return productModelRepository.findBrandsByQuery(query.trim());
    }

    public List<ProductModel> searchModels(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productModelRepository.findTopUsedModels();
        }
        return productModelRepository.searchModels(query.trim());
    }

    public List<ProductModel> findModelsByBrand(String brand, String modelQuery) {
        if (brand == null || brand.trim().isEmpty()) {
            return List.of();
        }
        String query = modelQuery == null ? "" : modelQuery.trim();
        return productModelRepository.findModelsByBrand(brand, query);
    }

    @Transactional
    public ProductModel saveOrUpdateModel(ProductModel model) {
        Optional<ProductModel> existing = productModelRepository.findByBrandAndModel(
            model.getBrand(), 
            model.getModel()
        );

        if (existing.isPresent()) {
            ProductModel existingModel = existing.get();
            existingModel.setUsageCount(existingModel.getUsageCount() + 1);
            existingModel.setLastUsedAt(LocalDateTime.now());
            
            if (model.getProductName() != null && !model.getProductName().isBlank()) {
                existingModel.setProductName(model.getProductName());
            }
            if (model.getCategoryId() != null) {
                existingModel.setCategoryId(model.getCategoryId());
            }
            if (model.getWeightKg() != null) {
                existingModel.setWeightKg(model.getWeightKg());
            }
            if (model.getDimensionsMm() != null) {
                existingModel.setDimensionsMm(model.getDimensionsMm());
            }
            
            return productModelRepository.save(existingModel);
        } else {
            model.setUsageCount(1);
            model.setLastUsedAt(LocalDateTime.now());
            if (model.getSource() == null) {
                model.setSource("manual_entry");
            }
            return productModelRepository.save(model);
        }
    }

    @Transactional
    public void recordUsage(Long modelId) {
        productModelRepository.incrementUsageCount(modelId);
    }
}
