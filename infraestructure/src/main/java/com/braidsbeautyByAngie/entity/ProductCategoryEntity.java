package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Product_Category")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Product_Category_ID", nullable = false)
    private Long productCategoryId;

    @Column(name = "Product_Category_Name", nullable = false, unique = true)
    private String productCategoryName;

    // Relación de "padre" (ManyToOne) a "hijos" (OneToMany) en la misma entidad
    @ManyToOne
    @JoinColumn(name = "Product_Category_Parent_ID")
    private ProductCategoryEntity parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<ProductCategoryEntity> subCategories = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = PromotionEntity.class, cascade = CascadeType.PERSIST)
    @JoinTable(name = "Promotion_Product_Category",
            joinColumns = @JoinColumn(name = "Product_Category_ID", referencedColumnName = "Product_Category_ID"),
            inverseJoinColumns = @JoinColumn(name = "Promotion_ID", referencedColumnName = "Promotion_ID"))
    private Set<PromotionEntity> promotionEntities;

    @OneToMany(mappedBy = "productCategoryEntity", cascade = CascadeType.ALL) //Cascade en hibernate significa que cualquier operacion que le hagamos al producto también será para todos los objetos relacionados.
    private List<ProductEntity> productEntities= new ArrayList<>();

    @OneToMany(mappedBy = "productCategoryEntity", cascade = CascadeType.ALL) //Cascade en hibernate significa que cualquier operacion que le hagamos al producto también será para todos los objetos relacionados.
    private List<VariationEntity> variationEntities= new ArrayList<>();

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
