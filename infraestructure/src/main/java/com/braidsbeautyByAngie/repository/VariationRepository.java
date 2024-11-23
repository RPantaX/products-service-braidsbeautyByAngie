package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.VariationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VariationRepository extends JpaRepository<VariationEntity, Long> {
    boolean existsByVariationName(String name);

    Optional<VariationEntity> findByVariationName(String name);
}
