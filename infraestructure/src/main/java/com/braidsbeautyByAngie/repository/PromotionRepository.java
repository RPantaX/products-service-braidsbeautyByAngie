package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.PromotionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    boolean existsByPromotionName(String name);

    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PromotionEntity p WHERE p.promotionId = :promotionId AND p.state = true")
    boolean existsByPromotionIdAndStateTrue(Long promotionId);

    @Query("SELECT p FROM PromotionEntity p WHERE p.promotionId = :promotionId AND p.state = true")
    Optional<PromotionEntity> findPromotionByIdWithStateTrue(Long promotionId);

    @Query(value = "SELECT p FROM PromotionEntity p WHERE p.promotionId IN :promotionIdList AND p.state = true")
    List<PromotionEntity> findAllByPromotionIdAndStateTrue(List<Long> promotionIdList);

    @Query(value = "SELECT p FROM PromotionEntity p WHERE p.state = true")
    Page<PromotionEntity> findAllByStateTrueAmdPageable(Pageable pageable);

    @Query(value = "SELECT p FROM PromotionEntity p WHERE p.state = true")
    List<PromotionEntity> findAllByStateTrue();

    Optional<PromotionEntity> findByPromotionNameAndStateTrue(String promotionName);
}
