package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Category> findByIsActive(Boolean isActive);
    
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    Optional<Category> findByNameIgnoreCase(String name);
    
    Optional<Category> findByNameEnIgnoreCase(String nameEn);
}
