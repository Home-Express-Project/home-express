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

    @Query("SELECT pm.brand FROM ProductModel pm WHERE LOWER(pm.brand) LIKE LOWER(CONCAT('%', :query, '%')) GROUP BY pm.brand ORDER BY MAX(pm.usageCount) DESC")
    List<String> findBrandsByQuery(@Param("query") String query);

    @Query("SELECT pm FROM ProductModel pm WHERE " +
           "LOWER(pm.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(pm.model) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(pm.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> searchModels(@Param("query") String query);

    @Query("SELECT pm FROM ProductModel pm WHERE " +
           "LOWER(pm.brand) = LOWER(:brand) AND " +
           "LOWER(pm.model) LIKE LOWER(CONCAT('%', :modelQuery, '%')) " +
           "ORDER BY pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> findModelsByBrand(@Param("brand") String brand, @Param("modelQuery") String modelQuery);

    @Modifying
    @Query("UPDATE ProductModel pm SET pm.usageCount = pm.usageCount + 1, pm.lastUsedAt = CURRENT_TIMESTAMP WHERE pm.modelId = :modelId")
    void incrementUsageCount(@Param("modelId") Long modelId);

    @Query("SELECT pm FROM ProductModel pm ORDER BY pm.usageCount DESC, pm.lastUsedAt DESC")
    List<ProductModel> findTopUsedModels();
}
