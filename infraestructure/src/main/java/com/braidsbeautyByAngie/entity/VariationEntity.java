package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Variation")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Variation_ID", nullable = false)
    private Long variationId;

    @Column(name = "Variation_Name", nullable = false)
    private String variationName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Product_Category_ID", insertable = false, updatable = false)
    private ProductCategoryEntity productCategoryEntity;

    @OneToMany(mappedBy = "variationEntity", cascade = CascadeType.ALL) //Cascade en hibernate significa que cualquier operacion que le hagamos al producto también será para todos los objetos relacionados.
    private List<VariationOptionEntity> variationOptionEntities= new ArrayList<>();

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
