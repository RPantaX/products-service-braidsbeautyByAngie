package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long> {
}
