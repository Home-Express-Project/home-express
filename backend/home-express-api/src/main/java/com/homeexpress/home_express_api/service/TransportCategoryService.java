package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.response.TransportCategoryResponse;
import com.homeexpress.home_express_api.dto.response.TransportSizeResponse;
import com.homeexpress.home_express_api.entity.Category;
import com.homeexpress.home_express_api.repository.CategoryRepository;
import com.homeexpress.home_express_api.repository.SizeRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TransportCategoryService {

    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;

    public TransportCategoryService(CategoryRepository categoryRepository, SizeRepository sizeRepository) {
        this.categoryRepository = categoryRepository;
        this.sizeRepository = sizeRepository;
    }

    public List<TransportCategoryResponse> getCategories(boolean activeOnly) {
        List<Category> categories = activeOnly
                ? categoryRepository.findByIsActive(true)
                : categoryRepository.findAll();

        if (categories.isEmpty()) {
            return List.of();
        }

        List<Long> categoryIds = categories.stream()
                .map(Category::getCategoryId)
                .toList();

        Map<Long, List<TransportSizeResponse>> sizesByCategory = sizeRepository
                .findByCategory_CategoryIdIn(categoryIds)
                .stream()
                .map(TransportSizeResponse::fromEntity)
                .collect(Collectors.groupingBy(TransportSizeResponse::getCategoryId));

        return categories.stream()
                .sorted(Comparator.comparingInt(cat -> cat.getDisplayOrder() == null ? 0 : cat.getDisplayOrder()))
                .map(category -> TransportCategoryResponse.fromEntity(
                        category,
                        sizesByCategory.getOrDefault(category.getCategoryId(), Collections.emptyList())))
                .toList();
    }
}
