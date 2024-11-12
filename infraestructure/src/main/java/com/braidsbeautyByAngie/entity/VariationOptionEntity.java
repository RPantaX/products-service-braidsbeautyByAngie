package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "Variation_Option")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariationOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Variation_Option_ID", nullable = false)
    private Long variationOptionId;

    @Column(name = "Variation_Option_Value", nullable = false)
    private String variationOptionValue;

    @ManyToOne(optional = true)
    @JoinColumn(name = "Variation_ID")
    private VariationEntity variationEntity;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = ProductItemEntity.class, cascade = CascadeType.PERSIST)
    @JoinTable(name = "Product_Configuration",
            joinColumns = @JoinColumn(name = "Variation_Option_ID", referencedColumnName = "Variation_Option_ID"),
            inverseJoinColumns = @JoinColumn(name = "Product_Item_ID", referencedColumnName = "Product_Item_ID"))
    private Set<ProductItemEntity> variationOptionEntitySet;

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
