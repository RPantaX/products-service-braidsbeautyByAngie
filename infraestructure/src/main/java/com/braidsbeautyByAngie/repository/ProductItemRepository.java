package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long> {
    boolean existsByProductItemSKU(String sku);

    @Query(value = "SELECT p FROM ProductItemEntity p WHERE p.productItemId = :productId AND p.state = true")
    Optional<ProductItemEntity> findByProductItemIdAndStateTrue(Long productId);
}
