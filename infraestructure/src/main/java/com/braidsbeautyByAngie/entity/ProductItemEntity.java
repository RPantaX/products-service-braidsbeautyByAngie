package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "Product_Item")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Product_Item_ID", nullable = false)
    private Long productItemId;

    @Column(name = "Product_Item_SKU", nullable = true)
    private String productItemSKU;

    @Column(name = "Product_Item_Quantity_In_Stock", nullable = false)
    private int productItemQuantityInStock;

    @Column(name = "Product_Item_Image", nullable = true)
    private String productItemImage;

    @Column(name = "Product_Item_Price", nullable = false)
    private BigDecimal productItemPrice;
    @Column(name = "Shoppring_Cart_Item_ID", nullable = true)
    private Long shoppingCartItemId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "Product_ID")
    private ProductEntity productEntity;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = VariationOptionEntity.class, cascade = CascadeType.PERSIST)
    @JoinTable(name = "Product_Configuration",
            joinColumns = @JoinColumn(name = "Product_Item_ID", referencedColumnName = "Product_Item_ID"),
            inverseJoinColumns = @JoinColumn(name = "Variation_Option_ID", referencedColumnName = "Variation_Option_ID"))
    private Set<VariationOptionEntity> variationOptionEntitySet;

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
