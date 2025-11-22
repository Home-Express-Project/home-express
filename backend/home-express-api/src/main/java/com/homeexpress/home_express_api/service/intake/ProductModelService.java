package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.entity.ProductModel;
import com.homeexpress.home_express_api.repository.ProductModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;

import java.util.Optional;

@Service
public class ProductModelService {

    private static final Logger log = LoggerFactory.getLogger(ProductModelService.class);
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
        // 1. Validate: Chặn rác ngay từ đầu
        if (!isValidInput(model.getBrand()) || !isValidInput(model.getModel())) {
            log.warn("Từ chối lưu model rác: Brand='{}', Model='{}'", model.getBrand(), model.getModel());
            return null; // Hoặc throw exception tùy ý
        }

        // 2. Chuẩn hóa: Trim khoảng trắng, viết hoa chữ cái đầu để tránh trùng lặp "samsung" vs "Samsung"
        model.setBrand(normalize(model.getBrand()));
        model.setModel(model.getModel().trim()); // Model thì chỉ cần trim, giữ nguyên case

        Optional<ProductModel> existing = productModelRepository.findByBrandAndModel(
            model.getBrand(), 
            model.getModel()
        );

        if (existing.isPresent()) {
            ProductModel existingModel = existing.get();
            // Nếu đã có thì tăng điểm tin cậy (usageCount)
            existingModel.setUsageCount(existingModel.getUsageCount() + 1);
            existingModel.setLastUsedAt(LocalDateTime.now());
            
            // Cập nhật thêm thông tin bổ sung nếu có
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
            // Nếu chưa có thì tạo mới
            model.setUsageCount(1); // Mới tạo thì uy tín thấp (1)
            model.setLastUsedAt(LocalDateTime.now());
            if (model.getSource() == null) {
                model.setSource("user_save"); // Mặc định là do user nhập
            }
            return productModelRepository.save(model);
        }
    }

    // Hàm kiểm tra rác: Ngắn quá hoặc chứa ký tự bậy bạ
    private boolean isValidInput(String text) {
        if (text == null || text.trim().length() < 2) return false; // Quá ngắn
        if (text.length() > 50) return false; // Quá dài
        // Regex chặn các ký tự đặc biệt vô nghĩa (cho phép chữ, số, khoảng trắng, gạch ngang)
        return text.matches("^[a-zA-Z0-9\\s\\-\\.]+$");
    }

    // Hàm chuẩn hóa tên Brand: "  sam sung " -> "Sam Sung"
    private String normalize(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return trimmed;
        // Viết hoa chữ cái đầu mỗi từ
        String[] words = trimmed.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Scheduled Job: Chạy mỗi đêm lúc 3h sáng để dọn rác
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupStaleEntries() {
        // Xóa những cái tạo ra đã 7 ngày mà vẫn chỉ có 1 người dùng (usage < 2)
        // Trừ những cái do hệ thống (system) tạo ra
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        productModelRepository.deleteStaleEntries(cutoff);
        log.info("Đã dọn dẹp các model rác/mồ côi cũ hơn {}", cutoff);
    }

    @Transactional
    public void recordUsage(Long modelId) {
        productModelRepository.incrementUsageCount(modelId);
    }
}
