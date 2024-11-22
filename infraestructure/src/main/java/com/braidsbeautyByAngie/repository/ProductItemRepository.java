package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ProductItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItemEntity, Long> {
    boolean existsByProductItemSKU(String sku);

    @Query(value = "SELECT p FROM ProductItemEntity p WHERE p.productItemId = :productId AND p.state = true")
    Optional<ProductItemEntity> findByProductItemIdAndStateTrue(Long productId);

    @Query(value = """
        SELECT pi.productItemId AS productItemId, 
               pi.productItemSKU AS productItemSKU, 
               pi.productItemQuantityInStock AS productItemQuantityInStock, 
               pi.productItemImage AS productItemImage, 
               pi.productItemPrice AS productItemPrice, 
               v.variationName AS variationName, 
               vo.variationOptionValue AS variationOptionValue
        FROM ProductItemEntity pi
        JOIN pi.variationOptionEntitySet vo
        JOIN vo.variationEntity v
        WHERE pi.productItemId = :productItemId
          AND pi.state = true
    """)
    List<Object[]> findProductItemWithVariations(@Param("productItemId") Long productItemId);

    @Query("SELECT pi.productItemId, pi.productItemSKU, pi.productItemQuantityInStock, pi.productItemImage, pi.productItemPrice, " +
            "vo.variationOptionValue, v.variationName, pc.productCategoryId, pc.productCategoryName, " +
            "p.promotionId, p.promotionName, p.promotionDescription, p.promotionDiscountRate, p.promotionStartDate, p.promotionEndDate " +
            "FROM ProductItemEntity pi " +
            "JOIN pi.productEntity pEntity " +
            "JOIN pEntity.productCategoryEntity pc " +
            "LEFT JOIN pi.variationOptionEntitySet vo " +
            "LEFT JOIN vo.variationEntity v " +
            "LEFT JOIN pEntity.productCategoryEntity.promotionEntities p " +
            "WHERE pi.productItemId = :itemProductId")
    List<Object[]> findProductItemWithVariationsWithCategory(@Param("itemProductId") Long itemProductId);

}
