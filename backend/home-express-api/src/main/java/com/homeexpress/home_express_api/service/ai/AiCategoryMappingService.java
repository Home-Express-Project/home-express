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
 * Service chuyên nhiệm vụ "phiên dịch" danh mục.
 * AI thường trả về tên tiếng Anh hoặc từ chung chung (VD: "fridge").
 * Class này sẽ map chúng về danh mục chuẩn trong database (VD: "Refrigerator").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCategoryMappingService {

    // Từ điển mapping cứng (Hardcoded rules)
    // Key: Từ khóa AI trả về (viết thường)
    // Value: Tên danh mục chuẩn trong DB
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

    /**
     * Hàm chính: Nhận vào item từ AI -> Trả về ID danh mục và ID kích thước trong DB.
     */
    public CategorySizeMapping map(EnhancedDetectedItem item) {
        if (item == null) {
            return CategorySizeMapping.empty();
        }

        // Gom tất cả các từ khóa có thể dùng để đoán (tên, category, subcategory, notes)
        List<String> candidates = collectCandidates(item);
        
        // Duyệt qua từng từ khóa xem có khớp với từ điển không
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

    // Tìm Category và Size trong DB dựa trên rule
    private CategorySizeMapping applyRule(MappingRule rule) {
        // Tìm category theo tên (tiếng Anh hoặc tiếng Việt)
        Optional<Category> categoryOpt = categoryRepository.findByNameEnIgnoreCase(rule.categoryName())
            .or(() -> categoryRepository.findByNameIgnoreCase(rule.categoryName()));

        if (categoryOpt.isEmpty()) {
            log.debug("Không tìm thấy danh mục '{}' trong database", rule);
            return CategorySizeMapping.empty();
        }

        Category category = categoryOpt.get();
        Long sizeId = null;

        // Nếu rule có quy định size cụ thể thì tìm size ID luôn
        if (rule.sizeName() != null) {
            sizeId = sizeRepository.findByCategory_CategoryIdAndNameIgnoreCase(category.getCategoryId(), rule.sizeName())
                .map(Size::getSizeId)
                .orElse(null);
        }

        return new CategorySizeMapping(category.getCategoryId(), sizeId);
    }

    // Gom nhặt các từ khóa tiềm năng từ item
    private List<String> collectCandidates(EnhancedDetectedItem item) {
        Set<String> tokens = new LinkedHashSet<>();

        addCandidate(tokens, item.getCategory());
        addCandidate(tokens, item.getSubcategory());
        addCandidate(tokens, item.getName());
        addCandidate(tokens, item.getNotes());

        return new ArrayList<>(tokens);
    }

    // Chuẩn hóa text và thêm vào danh sách token
    private void addCandidate(Set<String> tokens, String rawText) {
        String normalized = normalize(rawText);
        if (normalized == null) {
            return;
        }

        tokens.add(normalized);

        // Tách thành các từ đơn nếu chuỗi dài
        String[] parts = normalized.split(" ");
        if (parts.length > 1) {
            for (String part : parts) {
                if (part.length() >= 3) { // Chỉ lấy từ có độ dài >= 3 cho đỡ nhiễu
                    tokens.add(part);
                }
            }
        }
    }

    // Hàm làm sạch chuỗi (bỏ dấu, bỏ ký tự đặc biệt)
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
