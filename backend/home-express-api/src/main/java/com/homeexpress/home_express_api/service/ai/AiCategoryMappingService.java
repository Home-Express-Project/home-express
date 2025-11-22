package com.homeexpress.home_express_api.service.ai;

import com.homeexpress.home_express_api.dto.ai.EnhancedDetectedItem;
import com.homeexpress.home_express_api.entity.Category;
import com.homeexpress.home_express_api.entity.Size;
import com.homeexpress.home_express_api.repository.CategoryRepository;
import com.homeexpress.home_express_api.repository.SizeRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Provides deterministic mapping between AI-detected labels and system categories/sizes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCategoryMappingService {

    private static final Map<String, MappingRule> CATEGORY_RULES = Map.ofEntries(
        Map.entry("refrigerator", new MappingRule("Refrigerator", null)),
        Map.entry("fridge", new MappingRule("Refrigerator", null)),
        Map.entry("freezer", new MappingRule("Refrigerator", null)),
        Map.entry("tv", new MappingRule("TV/Monitor", null)),
        Map.entry("television", new MappingRule("TV/Monitor", null)),
        Map.entry("monitor", new MappingRule("TV/Monitor", null)),
        Map.entry("washing machine", new MappingRule("Washing Machine", null)),
        Map.entry("washer", new MappingRule("Washing Machine", null)),
        Map.entry("laundry machine", new MappingRule("Washing Machine", null)),
        Map.entry("bed", new MappingRule("Bed", null)),
        Map.entry("queen bed", new MappingRule("Bed", null)),
        Map.entry("king bed", new MappingRule("Bed", null)),
        Map.entry("double bed", new MappingRule("Bed", null)),
        Map.entry("wardrobe", new MappingRule("Wardrobe", null)),
        Map.entry("closet", new MappingRule("Wardrobe", null)),
        Map.entry("armoire", new MappingRule("Wardrobe", null)),
        Map.entry("desk", new MappingRule("Desk", null)),
        Map.entry("work desk", new MappingRule("Desk", null)),
        Map.entry("office desk", new MappingRule("Desk", null)),
        Map.entry("dining table", new MappingRule("Dining Table", null)),
        Map.entry("table", new MappingRule("Dining Table", null)),
        Map.entry("sofa", new MappingRule("Sofa", null)),
        Map.entry("couch", new MappingRule("Sofa", null)),
        Map.entry("loveseat", new MappingRule("Sofa", null)),
        Map.entry("sectional", new MappingRule("Sofa", null)),
        Map.entry("cardboard box", new MappingRule("Cardboard Box", null)),
        Map.entry("moving box", new MappingRule("Cardboard Box", null)),
        Map.entry("box", new MappingRule("Cardboard Box", null)),
        Map.entry("carton", new MappingRule("Cardboard Box", null)),
        Map.entry("appliance", new MappingRule("Other", null)),
        Map.entry("furniture", new MappingRule("Other", null))
    );

    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;

    public CategorySizeMapping map(EnhancedDetectedItem item) {
        if (item == null) {
            return CategorySizeMapping.empty();
        }

        List<String> candidates = collectCandidates(item);
        for (String candidate : candidates) {
            MappingRule rule = CATEGORY_RULES.get(candidate);
            if (rule == null) {
                continue;
            }

            CategorySizeMapping mapping = applyRule(rule);
            if (mapping.isPresent()) {
                return mapping;
            }
        }

        return CategorySizeMapping.empty();
    }

    private CategorySizeMapping applyRule(MappingRule rule) {
        Optional<Category> categoryOpt = categoryRepository.findByNameEnIgnoreCase(rule.categoryName())
            .or(() -> categoryRepository.findByNameIgnoreCase(rule.categoryName()));

        if (categoryOpt.isEmpty()) {
            log.debug("Category mapping rule {} not found in database", rule);
            return CategorySizeMapping.empty();
        }

        Category category = categoryOpt.get();
        Long sizeId = null;

        if (rule.sizeName() != null) {
            sizeId = sizeRepository.findByCategory_CategoryIdAndNameIgnoreCase(category.getCategoryId(), rule.sizeName())
                .map(Size::getSizeId)
                .orElse(null);
        }

        return new CategorySizeMapping(category.getCategoryId(), sizeId);
    }

    private List<String> collectCandidates(EnhancedDetectedItem item) {
        Set<String> tokens = new LinkedHashSet<>();

        addCandidate(tokens, item.getCategory());
        addCandidate(tokens, item.getSubcategory());
        addCandidate(tokens, item.getName());
        addCandidate(tokens, item.getNotes());

        return new ArrayList<>(tokens);
    }

    private void addCandidate(Set<String> tokens, String rawText) {
        String normalized = normalize(rawText);
        if (normalized == null) {
            return;
        }

        tokens.add(normalized);

        String[] parts = normalized.split(" ");
        if (parts.length > 1) {
            for (String part : parts) {
                if (part.length() >= 3) {
                    tokens.add(part);
                }
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String withoutAccents = decomposed.replaceAll("\\p{M}", "");
        String cleaned = withoutAccents.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private record MappingRule(String categoryName, String sizeName) {
    }

    public record CategorySizeMapping(Long categoryId, Long sizeId) {

        public static CategorySizeMapping empty() {
            return new CategorySizeMapping(null, null);
        }

        public boolean isPresent() {
            return categoryId != null;
        }
    }
}
