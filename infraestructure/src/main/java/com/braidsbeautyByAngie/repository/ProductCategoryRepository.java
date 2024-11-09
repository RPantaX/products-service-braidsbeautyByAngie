package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, Long> {
    Boolean existsByProductCategoryName(String categoryName);
}
