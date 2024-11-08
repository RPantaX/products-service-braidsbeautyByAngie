package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Long, ProductEntity> {
}
