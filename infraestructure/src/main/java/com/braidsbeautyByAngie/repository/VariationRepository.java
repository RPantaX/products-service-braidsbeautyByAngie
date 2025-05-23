package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.VariationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariationRepository extends JpaRepository<VariationEntity, Long> {
    boolean existsByVariationName(String name);

    Optional<VariationEntity> findByVariationName(String name);

    @Query(value = "SELECT v FROM VariationEntity v WHERE v.state = true")
    List<VariationEntity> findAllByStateTrue();

    @Query(value = "SELECT v FROM VariationEntity v WHERE v.variationId = :variationId AND v.state = true")
    Optional<VariationEntity> findByVariationIdAndStateTrue(Long variationId);

    @Query("SELECT v FROM VariationEntity v LEFT JOIN FETCH v.variationOptionEntities vo WHERE v.state = true")
    List<VariationEntity> findAllVariationsWithOptions();
}
