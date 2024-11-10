package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    boolean existsByPromotionName(String name);
}
