package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    Optional<ProductModel> findByBrandAndModel(String brand, String model);

    // Chỉ lấy brand có ít nhất 2 lượt dùng (để lọc rác) hoặc là hàng system xịn
    @Query("SELECT pm.brand FROM ProductModel pm " +
           "WHERE LOWER(pm.brand) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND (pm.usageCount >= 2 OR pm.source = 'system') " +
           "GROUP BY pm.brand " +
           "ORDER BY MAX(CASE WHEN pm.source = 'system' THEN 1 ELSE 0 END) DESC, " + // Ưu tiên hàng hệ thống
           "MAX(pm.usageCount) DESC") // Sau đó đến hàng nhiều người dùng
    List<String> findBrandsByQuery(@Param("query") String query);

    // Tìm model cũng áp dụng cơ chế lọc rác tương tự
    @Query("SELECT pm FROM ProductModel pm WHERE " +
           "(LOWER(pm.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(pm.model) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(pm.productName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (pm.usageCount >= 2 OR pm.source = 'system') " +
           "ORDER BY CASE WHEN pm.source = 'system' THEN 1 ELSE 0 END DESC, " +
           "pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> searchModels(@Param("query") String query);

    // Tìm model theo brand cụ thể
    @Query("SELECT pm FROM ProductModel pm WHERE " +
           "LOWER(pm.brand) = LOWER(:brand) AND " +
           "LOWER(pm.model) LIKE LOWER(CONCAT('%', :modelQuery, '%')) " +
           "AND (pm.usageCount >= 2 OR pm.source = 'system') " +
           "ORDER BY CASE WHEN pm.source = 'system' THEN 1 ELSE 0 END DESC, " +
           "pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> findModelsByBrand(@Param("brand") String brand, @Param("modelQuery") String modelQuery);

    @Modifying
    @Query("UPDATE ProductModel pm SET pm.usageCount = pm.usageCount + 1, pm.lastUsedAt = CURRENT_TIMESTAMP WHERE pm.modelId = :modelId")
    void incrementUsageCount(@Param("modelId") Long modelId);

    @Query("SELECT pm FROM ProductModel pm WHERE pm.usageCount >= 2 OR pm.source = 'system' ORDER BY pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> findTopUsedModels();

    // Xóa mấy cái rác mồ côi (dùng 1 lần, không phải system, cũ quá hạn)
    @Modifying
    @Query("DELETE FROM ProductModel pm WHERE pm.usageCount < 2 AND pm.source != 'system' AND pm.createdAt < :cutoffDate")
    void deleteStaleEntries(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
