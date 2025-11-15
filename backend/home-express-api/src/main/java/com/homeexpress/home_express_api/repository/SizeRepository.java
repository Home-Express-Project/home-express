package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Size;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    
    List<Size> findByCategory_CategoryId(Long categoryId);
    
    List<Size> findByCategory_CategoryIdIn(List<Long> categoryIds);
    
    Optional<Size> findByCategory_CategoryIdAndNameIgnoreCase(Long categoryId, String name);
}
