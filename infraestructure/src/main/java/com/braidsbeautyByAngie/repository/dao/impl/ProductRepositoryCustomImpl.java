package com.braidsbeautyByAngie.repository.dao.impl;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProductItemDetaill;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseVariationn;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
import com.braidsbeautyByAngie.repository.dao.ProductRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final PromotionMapper promotionMapper;
    @Transactional(readOnly = true)
    @Override
    public ResponseListPageableProduct filterProducts(RequestProductFilter filter) {
        log.info("Filtering products with parameters: {}", filter);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> query = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> productRoot = query.from(ProductEntity.class);

        // Joins necesarios
        Join<ProductEntity, ProductCategoryEntity> categoryJoin = productRoot.join("productCategoryEntity", JoinType.LEFT);
        Join<ProductEntity, ProductItemEntity> itemJoin = productRoot.join("productItemEntities", JoinType.LEFT);
        Join<ProductCategoryEntity, PromotionEntity> promotionJoin = categoryJoin.join("promotionEntities", JoinType.LEFT);
        Join<ProductItemEntity, VariationOptionEntity> variationOptionJoin = itemJoin.join("variationOptionEntitySet", JoinType.LEFT);
        Join<VariationOptionEntity, VariationEntity> variationJoin = variationOptionJoin.join("variationEntity", JoinType.LEFT);

        // Lista de predicados para construir la consulta
        List<Predicate> predicates = new ArrayList<>();

        // Filtro base: solo productos activos
        if (filter.getActiveOnly() == null || filter.getActiveOnly()) {
            predicates.add(cb.isTrue(productRoot.get("state")));
            predicates.add(cb.isTrue(itemJoin.get("state")));
        }

        // Filtros de producto principal
        addProductFilters(cb, productRoot, predicates, filter);

        // Filtros de categoría
        addCategoryFilters(cb, categoryJoin, predicates, filter);

        // Filtros de precio y stock
        addPriceAndStockFilters(cb, itemJoin, predicates, filter);

        // Filtros de variaciones
        addVariationFilters(cb, variationJoin, variationOptionJoin, predicates, filter);

        // Filtros de promociones
        addPromotionFilters(cb, promotionJoin, predicates, filter);

        // Aplicar todos los predicados
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Distinct para evitar duplicados debido a los joins
        query.distinct(true);

        // Ordenamiento
        addOrderBy(cb, query, productRoot, itemJoin, filter);

        // Ejecutar consulta con paginación
        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());
        TypedQuery<ProductEntity> typedQuery = entityManager.createQuery(query);

        // Calcular total de elementos para la paginación
        long totalElements = getTotalElements(filter);

        // Aplicar paginación
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ProductEntity> products = typedQuery.getResultList();

        // Convertir a DTOs
        List<ResponseProduct> responseProducts = convertToResponseProducts(products);

        // Crear página
        Page<ResponseProduct> page = new PageImpl<>(responseProducts, pageable, totalElements);

        return ResponseListPageableProduct.builder()
                .responseProductList(responseProducts)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .end(page.isLast())
                .build();
    }
    private void addProductFilters(CriteriaBuilder cb, Root<ProductEntity> productRoot,
                                   List<Predicate> predicates, RequestProductFilter filter) {

        // Filtro por nombre de producto
        if (filter.getProductName() != null && !filter.getProductName().trim().isEmpty()) {
            predicates.add(cb.like(cb.upper(productRoot.get("productName")),
                    "%" + filter.getProductName().toUpperCase() + "%"));
        }

        // Filtro por IDs de producto
        if (filter.getProductIds() != null && !filter.getProductIds().isEmpty()) {
            predicates.add(productRoot.get("productId").in(filter.getProductIds()));
        }

        // Búsqueda general en nombre y descripción
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
            String searchPattern = "%" + filter.getSearchTerm().toUpperCase() + "%";
            Predicate nameSearch = cb.like(cb.upper(productRoot.get("productName")), searchPattern);
            Predicate descSearch = cb.like(cb.upper(productRoot.get("productDescription")), searchPattern);
            predicates.add(cb.or(nameSearch, descSearch));
        }
    }

    private void addCategoryFilters(CriteriaBuilder cb, Join<ProductEntity, ProductCategoryEntity> categoryJoin,
                                    List<Predicate> predicates, RequestProductFilter filter) {

        // Filtro por IDs de categoría
        if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
            predicates.add(categoryJoin.get("productCategoryId").in(filter.getCategoryIds()));
        }

        // Filtro por nombre de categoría
        if (filter.getCategoryName() != null && !filter.getCategoryName().trim().isEmpty()) {
            predicates.add(cb.like(cb.upper(categoryJoin.get("productCategoryName")),
                    "%" + filter.getCategoryName().toUpperCase() + "%"));
        }

        // Filtro por categoría padre
        if (filter.getParentCategoryId() != null) {
            predicates.add(cb.equal(categoryJoin.get("parentCategory").get("productCategoryId"),
                    filter.getParentCategoryId()));
        }

        // Solo categorías activas
        predicates.add(cb.isTrue(categoryJoin.get("state")));
    }

    private void addPriceAndStockFilters(CriteriaBuilder cb, Join<ProductEntity, ProductItemEntity> itemJoin,
                                         List<Predicate> predicates, RequestProductFilter filter) {

        // Filtro por precio mínimo
        if (filter.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(itemJoin.get("productItemPrice"), filter.getMinPrice()));
        }

        // Filtro por precio máximo
        if (filter.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(itemJoin.get("productItemPrice"), filter.getMaxPrice()));
        }

        // Filtro por stock mínimo
        if (filter.getMinStock() != null) {
            predicates.add(cb.greaterThanOrEqualTo(itemJoin.get("productItemQuantityInStock"), filter.getMinStock()));
        }

        // Filtro solo productos en stock
        if (filter.getInStock() != null && filter.getInStock()) {
            predicates.add(cb.greaterThan(itemJoin.get("productItemQuantityInStock"), 0));
        }
    }

    private void addVariationFilters(CriteriaBuilder cb, Join<VariationOptionEntity, VariationEntity> variationJoin,
                                     Join<ProductItemEntity, VariationOptionEntity> variationOptionJoin,
                                     List<Predicate> predicates, RequestProductFilter filter) {

        // Filtro por nombres de variación
        if (filter.getVariationNames() != null && !filter.getVariationNames().isEmpty()) {
            predicates.add(variationJoin.get("variationName").in(filter.getVariationNames()));
        }

        // Filtro por valores de variación
        if (filter.getVariationValues() != null && !filter.getVariationValues().isEmpty()) {
            predicates.add(variationOptionJoin.get("variationOptionValue").in(filter.getVariationValues()));
        }

        // Filtro por IDs de opciones de variación
        if (filter.getVariationOptionIds() != null && !filter.getVariationOptionIds().isEmpty()) {
            predicates.add(variationOptionJoin.get("variationOptionId").in(filter.getVariationOptionIds()));
        }

        // Solo variaciones activas
        predicates.add(cb.isTrue(variationJoin.get("state")));
        predicates.add(cb.isTrue(variationOptionJoin.get("state")));
    }

    private void addPromotionFilters(CriteriaBuilder cb, Join<ProductCategoryEntity, PromotionEntity> promotionJoin,
                                     List<Predicate> predicates, RequestProductFilter filter) {

        // Filtro solo productos con promociones
        if (filter.getHasPromotion() != null && filter.getHasPromotion()) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            predicates.add(cb.isNotNull(promotionJoin.get("promotionId")));
            predicates.add(cb.isTrue(promotionJoin.get("state")));
            predicates.add(cb.or(
                    cb.isNull(promotionJoin.get("promotionStartDate")),
                    cb.lessThanOrEqualTo(promotionJoin.get("promotionStartDate"), now)
            ));
            predicates.add(cb.or(
                    cb.isNull(promotionJoin.get("promotionEndDate")),
                    cb.greaterThanOrEqualTo(promotionJoin.get("promotionEndDate"), now)
            ));
        }

        // Filtro por IDs de promoción
        if (filter.getPromotionIds() != null && !filter.getPromotionIds().isEmpty()) {
            predicates.add(promotionJoin.get("promotionId").in(filter.getPromotionIds()));
        }

        // Filtro por tasa de descuento mínima
        if (filter.getMinDiscountRate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionJoin.get("promotionDiscountRate"),
                    filter.getMinDiscountRate()));
        }

        // Filtro por tasa de descuento máxima
        if (filter.getMaxDiscountRate() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionJoin.get("promotionDiscountRate"),
                    filter.getMaxDiscountRate()));
        }
    }

    private void addOrderBy(CriteriaBuilder cb, CriteriaQuery<ProductEntity> query,
                            Root<ProductEntity> productRoot, Join<ProductEntity, ProductItemEntity> itemJoin,
                            RequestProductFilter filter) {

        Order order;
        boolean isAsc = "ASC".equalsIgnoreCase(filter.getSortDirection());

        switch (filter.getSortBy().toLowerCase()) {
            case "price":
                order = isAsc ? cb.asc(itemJoin.get("productItemPrice")) : cb.desc(itemJoin.get("productItemPrice"));
                break;
            case "stock":
                order = isAsc ? cb.asc(itemJoin.get("productItemQuantityInStock")) :
                        cb.desc(itemJoin.get("productItemQuantityInStock"));
                break;
            case "createdat":
                order = isAsc ? cb.asc(productRoot.get("createdAt")) : cb.desc(productRoot.get("createdAt"));
                break;
            case "productname":
            default:
                order = isAsc ? cb.asc(productRoot.get("productName")) : cb.desc(productRoot.get("productName"));
                break;
        }

        query.orderBy(order);
    }

    private long getTotalElements(RequestProductFilter filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductEntity> productRoot = countQuery.from(ProductEntity.class);

        // Mismos joins que en la consulta principal
        Join<ProductEntity, ProductCategoryEntity> categoryJoin = productRoot.join("productCategoryEntity", JoinType.LEFT);
        Join<ProductEntity, ProductItemEntity> itemJoin = productRoot.join("productItemEntities", JoinType.LEFT);
        Join<ProductCategoryEntity, PromotionEntity> promotionJoin = categoryJoin.join("promotionEntities", JoinType.LEFT);
        Join<ProductItemEntity, VariationOptionEntity> variationOptionJoin = itemJoin.join("variationOptionEntitySet", JoinType.LEFT);
        Join<VariationOptionEntity, VariationEntity> variationJoin = variationOptionJoin.join("variationEntity", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // Aplicar todos los filtros
        if (filter.getActiveOnly() == null || filter.getActiveOnly()) {
            predicates.add(cb.isTrue(productRoot.get("state")));
            predicates.add(cb.isTrue(itemJoin.get("state")));
        }

        addProductFilters(cb, productRoot, predicates, filter);
        addCategoryFilters(cb, categoryJoin, predicates, filter);
        addPriceAndStockFilters(cb, itemJoin, predicates, filter);
        addVariationFilters(cb, variationJoin, variationOptionJoin, predicates, filter);
        addPromotionFilters(cb, promotionJoin, predicates, filter);

        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        countQuery.select(cb.countDistinct(productRoot.get("productId")));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<ResponseProduct> convertToResponseProducts(List<ProductEntity> products) {
        return products.stream().map(product -> {
            // Construir categoría con promociones
            ResponseCategoryy responseCategory = null;
            if (product.getProductCategoryEntity() != null) {
                List<PromotionDTO> promotionDTOList = product.getProductCategoryEntity()
                        .getPromotionEntities().stream()
                        .map(promotionMapper::mapPromotionEntityToDto)
                        .collect(Collectors.toList());

                responseCategory = ResponseCategoryy.builder()
                        .productCategoryId(product.getProductCategoryEntity().getProductCategoryId())
                        .productCategoryName(product.getProductCategoryEntity().getProductCategoryName())
                        .promotionDTOList(promotionDTOList)
                        .build();
            }

            // Construir items del producto
            List<ResponseProductItemDetaill> productItemDetails = product.getProductItemEntities().stream()
                    .filter(item -> item.getState()) // Solo items activos
                    .map(item -> {
                        List<ResponseVariationn> variations = item.getVariationOptionEntitySet().stream()
                                .map(variationOption -> {
                                    String variationName = variationOption.getVariationEntity() != null ?
                                            variationOption.getVariationEntity().getVariationName() : "Unknown";
                                    return new ResponseVariationn(variationName, variationOption.getVariationOptionValue());
                                })
                                .collect(Collectors.toList());

                        return new ResponseProductItemDetaill(
                                item.getProductItemId(),
                                item.getProductItemSKU(),
                                item.getProductItemQuantityInStock(),
                                item.getProductItemImage(),
                                item.getProductItemPrice(),
                                variations
                        );
                    })
                    .collect(Collectors.toList());

            return new ResponseProduct(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductDescription(),
                    product.getProductImage(),
                    responseCategory,
                    productItemDetails
            );
        }).collect(Collectors.toList());
    }
}
