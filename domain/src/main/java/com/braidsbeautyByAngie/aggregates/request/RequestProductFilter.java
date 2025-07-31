package com.braidsbeautyByAngie.aggregates.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestProductFilter {

    // Filtros de producto principal
    private String productName;
    private List<Long> productIds;

    // Filtros de categoría
    private List<Long> categoryIds;
    private String categoryName;
    private Long parentCategoryId; // Para filtrar por categoría padre

    // Filtros de precio
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Filtros de stock
    private Integer minStock;
    private Boolean inStock; // true = solo productos con stock > 0

    // Filtros de variaciones
    private List<String> variationNames; // ej: ["Color", "Talla"]
    private List<String> variationValues; // ej: ["Rojo", "M", "L"]
    private List<Long> variationOptionIds;

    // Filtros de promociones
    private Boolean hasPromotion; // true = solo productos con promociones activas
    private List<Long> promotionIds;
    private BigDecimal minDiscountRate;
    private BigDecimal maxDiscountRate;

    // Filtros de búsqueda general
    private String searchTerm; // Busca en nombre y descripción del producto

    // Filtros de estado
    private Boolean activeOnly; // Por defecto true

    // Paginación
    private int pageNumber = 0;
    private int pageSize = 10;

    // Ordenamiento
    private String sortBy = "productName"; // productName, price, createdAt, stock
    private String sortDirection = "ASC"; // ASC, DESC
}
