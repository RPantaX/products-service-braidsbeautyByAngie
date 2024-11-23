package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Product")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Product_ID", nullable = false)
    private Long productId;

    @Column(name = "Product_Name", nullable = false, unique = true)
    private String productName;

    @Column(name = "Product_Description", nullable = true)
    private String productDescription;

    @Column(name = "Product_Image", nullable = true)
    private String productImage;

    @ManyToOne(optional = true)
    @JoinColumn(name = "Product_Category_ID")
    private ProductCategoryEntity productCategoryEntity;

    @OneToMany(mappedBy = "productEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER) //Cascade en hibernate significa que cualquier operacion que le hagamos al producto también será para todos los objetos relacionados.
    private List<ProductItemEntity> productItemEntities= new ArrayList<>();

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
