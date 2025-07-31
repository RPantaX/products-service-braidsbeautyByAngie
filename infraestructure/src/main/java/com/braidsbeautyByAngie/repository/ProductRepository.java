package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
    boolean existsByProductName(String productName);

    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProductEntity p WHERE p.productId = :productId AND p.state = true")
    boolean existsByProductIdWithStateTrue(Long productId);

    @Query(value = "SELECT p FROM ProductEntity p WHERE p.productId = :productId AND p.state = true")
    Optional<ProductEntity> findProductByProductIdWithStateTrue(Long productId);

    @Query(value = "SELECT p FROM ProductEntity p WHERE p.state=true")
    Page<ProductEntity> findAllByStateTrueAndPageable(Pageable pageable);

    @Query(value = """
    SELECT p.productId AS productId,
           p.productName AS productName,
           p.productDescription AS productDescription,
           p.productImage AS productImage,
           pi.productItemId AS productItemId,
           pi.productItemSKU AS productItemSKU,
           pi.productItemQuantityInStock AS productItemQuantityInStock,
           pi.productItemImage AS productItemImage,
           pi.productItemPrice AS productItemPrice,
           v.variationName AS variationName,
           vo.variationOptionValue AS variationOptionValue
    FROM ProductEntity p
    FULL JOIN p.productItemEntities pi
    FULL JOIN pi.variationOptionEntitySet vo
    FULL JOIN vo.variationEntity v
    WHERE p.productId = :productId
      AND p.state = true
""")
    List<Object[]> findProductDetailsById(@Param("productId") Long productId);

    @Query(value = """
    SELECT p.productId AS productId,
           p.productName AS productName,
           p.productDescription AS productDescription,
           p.productImage AS productImage,
           pi.productItemId AS productItemId,
           pi.productItemSKU AS productItemSKU,
           pi.productItemQuantityInStock AS productItemQuantityInStock,
           pi.productItemImage AS productItemImage,
           pi.productItemPrice AS productItemPrice,
           v.variationName AS variationName,
           vo.variationOptionValue AS variationOptionValue,
           pc.productCategoryId AS productCategoryId,
           pc.productCategoryName AS productCategoryName,
           pr.promotionId AS promotionId,
           pr.promotionName AS promotionName
    FROM ProductEntity p
    LEFT JOIN p.productItemEntities pi
    LEFT JOIN pi.variationOptionEntitySet vo
    LEFT JOIN vo.variationEntity v
    LEFT JOIN p.productCategoryEntity pc
    LEFT JOIN pc.promotionEntities pr
    WHERE p.productId = :productId AND p.state = true
""")
    List<Object[]> findProductDetailWithCategoryById(@Param("productId") Long productId);

    @Query(value = "SELECT p.Product_ID AS productId, p.Product_Name AS productName, " +
            "p.Product_Description AS productDescription, p.Product_Image AS productImage, " +
            "pc.Product_Category_ID AS productCategoryId, pc.Product_Category_Name AS productCategoryName, " +
            "pr.Promotion_ID AS promotionId, pr.Promotion_Name AS promotionName, " +
            "pr.Promotion_Description AS promotionDescription, pr.Promotion_Discount_Rate AS promotionDiscountRate, " +
            "pr.Promotion_Start_Date AS promotionStartDate, pr.Promotion_End_Date AS promotionEndDate, " +
            "pi.Product_Item_ID AS productItemId, pi.Product_Item_SKU AS productItemSKU, " +
            "pi.Product_Item_Quantity_In_Stock AS productItemQuantityInStock, " +
            "pi.Product_Item_Image AS productItemImage, pi.Product_Item_Price AS productItemPrice, " +
            "v.Variation_ID AS variationId, v.Variation_Name AS variationName, " +
            "vo.Variation_Option_Value AS variationOptionValue " +
            "FROM Product p " +
            "JOIN Product_Category pc ON p.Product_Category_ID = pc.Product_Category_ID " +
            "LEFT JOIN Product_Item pi ON p.Product_ID = pi.Product_ID " +
            "LEFT JOIN Variation v ON pc.Product_Category_ID = v.Product_Category_ID " +
            "LEFT JOIN Variation_Option vo ON v.Variation_ID = vo.Variation_ID " +
            "LEFT JOIN Promotion_Product_Category ppc ON pc.Product_Category_ID = ppc.Product_Category_ID " +
            "LEFT JOIN Promotion pr ON ppc.Promotion_ID = pr.Promotion_ID " +
            "WHERE p.Product_ID = :productId AND p.state = true " +
            "LIMIT 1", nativeQuery = true)
    ResponseProduct findProductById(@Param("productId") Long productId);

    @Query(value = """
    SELECT p.productId AS productId, 
           p.productName AS productName, 
           p.productDescription AS productDescription, 
           p.productImage AS productImage, 
           pc.productCategoryId AS productCategoryId, 
           pc.productCategoryName AS productCategoryName,
           pi.productItemId AS productItemId, 
           pi.productItemSKU AS productItemSKU, 
           pi.productItemQuantityInStock AS productItemQuantityInStock, 
           pi.productItemImage AS productItemImage, 
           pi.productItemPrice AS productItemPrice, 
           v.variationName AS variationName, 
           vo.variationOptionValue AS variationOptionValue,
           pr.promotionId AS promotionId, 
           pr.promotionName AS promotionName, 
           pr.promotionDescription AS promotionDescription, 
           pr.promotionDiscountRate AS promotionDiscountRate, 
           pr.promotionStartDate AS promotionStartDate, 
           pr.promotionEndDate AS promotionEndDate
    FROM ProductEntity p
    JOIN p.productCategoryEntity pc
    JOIN p.productItemEntities pi
    LEFT JOIN pi.variationOptionEntitySet vo
    LEFT JOIN vo.variationEntity v
    LEFT JOIN p.productCategoryEntity.promotionEntities pr
    WHERE p.productId = :productId
      AND pi.state = true
""")
    List<Object[]> findProductWithItemsAndCategory(@Param("productId") Long productId);
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.productCategoryEntity.productCategoryId = :categoryId AND p.state = true")
    int countByCategoryIdAndStateTrue(@Param("categoryId") Long categoryId);
}
