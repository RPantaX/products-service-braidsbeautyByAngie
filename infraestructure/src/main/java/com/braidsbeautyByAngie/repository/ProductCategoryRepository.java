package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import com.braidsbeautyByAngie.repository.dao.ProductRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity, Long>, ProductRepositoryCustom {

    Boolean existsByProductCategoryName(String categoryName);

    @Query(value = "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ProductCategoryEntity c WHERE c.productCategoryId = :categoryId AND c.state = true")
    Boolean existByProductCategoryIdAndStateTrue(Long categoryId);

    @Query(value = "SELECT c FROM ProductCategoryEntity c WHERE c.productCategoryId = :categoryId AND c.state = true")
    Optional<ProductCategoryEntity> findProductCategoryIdAndStateTrue(Long categoryId);

    @Query("SELECT c FROM ProductCategoryEntity c WHERE c.parentCategory IS NULL AND c.state=true")
    Page<ProductCategoryEntity> findAllCategoriesPageableAndStatusTrue(Pageable pageable);

    @Query("SELECT c FROM ProductCategoryEntity c JOIN c.promotionEntities p WHERE p.promotionId IN :promotionId AND c.state = true")
    List<ProductCategoryEntity> findAllByPromotionIdAndStateTrue(List<Long> promotionId);

    Optional<ProductCategoryEntity> findByProductCategoryNameAndStateTrue(String categoryName);
    List<ProductCategoryEntity> findAllByStateTrue();
}
