package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "Promotion")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Promotion_ID", nullable = false)
    private Long promotionId;

    @Column(name = "Promotion_Name", nullable = true, unique = true)
    private String promotionName;

    @Column(name = "Promotion_Description", nullable = true)
    private String promotionDescription;

    @Column(name = "Promotion_Discount_Rate", nullable = true)
    private BigDecimal promotionDiscountRate;

    @Column(name = "Promotion_Start_Date", nullable = true)
    private Timestamp promotionStartDate;

    @Column(name = "Promotion_End_Date", nullable = true)
    private Timestamp promotionEndDate;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = ProductCategoryEntity.class, cascade = CascadeType.PERSIST)
    @JoinTable(name = "Promotion_Product_Category",
            joinColumns = @JoinColumn(name = "Promotion_ID", referencedColumnName = "Promotion_ID"),
            inverseJoinColumns = @JoinColumn(name = "Product_Category_ID", referencedColumnName = "Product_Category_ID"))
    private Set<ProductCategoryEntity> productCategoryEntities;

    @Column(name = "state", nullable = false)
    private Boolean state;

    @Column(name = "modified_by_user", nullable = false, length = 15)
    private String modifiedByUser;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "modified_at")
    private Timestamp modifiedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;
}
