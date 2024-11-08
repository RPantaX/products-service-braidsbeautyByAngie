package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.VariationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariationRepository extends JpaRepository<Long, VariationEntity> {
}
