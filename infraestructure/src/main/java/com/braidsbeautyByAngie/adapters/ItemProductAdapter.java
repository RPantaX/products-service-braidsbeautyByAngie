package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.constants.ProductsErrorEnum;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestVariationName;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ItemProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.GlobalErrorEnum;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ValidateUtil;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemProductAdapter implements ItemProductServiceOut {
    private final PromotionMapper promotionMapper;
    private final ProductItemMapper productItemMapper;

    private final ProductItemRepository productItemRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final VariationRepository variationRepository;
    private final VariationOptionRepository variationOptionRepository;


    @Transactional
    @Override
    public ProductItemDTO createItemProductOut(RequestItemProduct requestItemProduct) {
        log.info("Creating itemProduct id parent: {}", requestItemProduct.getProductId());
        if (productItemExistsBySKU(requestItemProduct.getProductItemSKU())) ValidateUtil.evaluar(false, ProductsErrorEnum.ITEM_PRODUCT_ALREADY_EXISTS_ERI00002);
        ProductEntity productEntity = validateAndGetProduct(requestItemProduct.getProductId());
        //varation
        Set<VariationOptionEntity> variationEntitiesSaved = saveVariations(requestItemProduct.getRequestVariations());
        ProductItemEntity productItemEntity = ProductItemEntity.builder()
                .variationOptionEntitySet(variationEntitiesSaved)
                .productEntity(productEntity)
                .productItemSKU(requestItemProduct.getProductItemSKU())
                .productItemImage(requestItemProduct.getProductItemImage())
                .productItemPrice(requestItemProduct.getProductItemPrice())
                .productItemQuantityInStock(requestItemProduct.getProductItemQuantityInStock())
                .createdAt(Constants.getTimestamp())
                .modifiedByUser(Constants.getUserInSession())
                .state(Constants.STATUS_ACTIVE)
                .build();

        ProductItemEntity productItemEntitySaved =  productItemRepository.save(productItemEntity);
        log.info("itemProduct '{}' created successfully with ID: {}", productItemEntity.getProductItemId(), productItemEntity.getProductEntity().getProductId());
        return productItemMapper.mapProductItemEntityToDto(productItemEntitySaved);
    }

    @Override
    public ResponseProductItemDetail findItemProductByIdOut(Long itemProductId) {
        List<Object[]> results = productItemRepository.findProductItemWithVariations(itemProductId);

        if (results.isEmpty()) {
            log.error("Product Item not found with ID: {}", itemProductId);
            ValidateUtil.requerido(null, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
        }
        return buildProductItemDetail(itemProductId, results);
    }
    @Transactional
    @Override
    public ProductItemDTO updateItemProductOut(Long itemProductId, RequestItemProduct requestItemProduct) {
        log.info("Searching for update product with ID: {}", itemProductId);
        ValidateUtil.evaluar(productRepository.existsById(itemProductId), ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
        //varation
        Set<VariationOptionEntity> variationOptionEntities = new HashSet<>();
        if (!requestItemProduct.getRequestVariations().isEmpty()) {
            variationOptionEntities = saveVariations(requestItemProduct.getRequestVariations());
        }
        ProductItemEntity productItemEntity1 = ProductItemEntity.builder()
                .variationOptionEntitySet(variationOptionEntities)
                .productItemSKU(requestItemProduct.getProductItemSKU())
                .productItemImage(requestItemProduct.getProductItemImage())
                .productItemPrice(requestItemProduct.getProductItemPrice())
                .productItemQuantityInStock(requestItemProduct.getProductItemQuantityInStock())
                .modifiedAt(Constants.getTimestamp())
                .modifiedByUser(Constants.getUserInSession())
                .build();
        ProductItemEntity productItemSaved = productItemRepository.save(productItemEntity1);
        log.info("itemProduct updated with ID: {}", productItemSaved.getProductItemId());
        return productItemMapper.mapProductItemEntityToDto(productItemSaved);
    }


    @Override
    public ProductItemDTO deleteItemProductOut(Long itemProductId) {
        log.info("Searching itemProduct for delete with ID: {}", itemProductId);

        ProductItemEntity productItemEntityOptional = getProductItemById(itemProductId).orElse(null);
        if (productItemEntityOptional == null) {
            log.error("Product Item not found with ID: {}", itemProductId);
            ValidateUtil.requerido(productItemEntityOptional, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
        }
        productItemEntityOptional.setDeletedAt(Constants.getTimestamp());
        productItemEntityOptional.setState(Constants.STATUS_INACTIVE);
        productItemEntityOptional.setModifiedByUser(Constants.getUserInSession());
        productItemEntityOptional.setProductEntity(null);
        ProductItemEntity itemProductDeleted = productItemRepository.save(productItemEntityOptional);

        log.info("Product deleted with ID: {}", itemProductDeleted.getProductItemId());

        return productItemMapper.mapProductItemEntityToDto(itemProductDeleted);
    }

    @Override
    public List<Product> reserveProductOut(Long shopOrderId, List<Product> desiredProducts) {

        updateStock(desiredProducts, -1);
        return desiredProducts.stream().map(p-> {
            ProductItemEntity productItemEntity = productItemRepository.findByProductItemIdAndStateTrue(p.getProductId()).orElse(null);
            if(productItemEntity == null) {
                log.error("Product Item not found with ID: {}", p.getProductId());
                ValidateUtil.requerido(productItemEntity, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
            }
            BigDecimal productPrice = productItemEntity.getProductItemPrice();

            if(!productItemEntity.getProductEntity().getProductCategoryEntity().getPromotionEntities().isEmpty()){
                BigDecimal discountRate = productItemEntity.getProductEntity().getProductCategoryEntity().getPromotionEntities().stream()
                        .map(PromotionEntity::getPromotionDiscountRate)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                productPrice = productPrice.subtract(productPrice.multiply(discountRate));
            }

            return Product.builder()
                    .productId(p.getProductId())
                    .price(productPrice)
                    .productName(productItemEntity.getProductEntity().getProductName())
                    .quantity(p.getQuantity()).build();
        }).toList();
    }

    @Override
    public void cancelProductReservationOut(Long shopOrderId, List<Product> productsToCancel) {
        updateStock(productsToCancel, 1);
    }

    @Override
    public List<ResponseProductItemDetail> listItemProductsByIdsOut(List<Long> itemProductIds) {
        if (itemProductIds == null || itemProductIds.isEmpty()) {
            log.error("No Product Item IDs provided for listing.");
            ValidateUtil.evaluar(false, ProductsErrorEnum.ITEM_PRODUCT_ALREADY_EXISTS_ERI00002);
        }

        // Consultar los datos necesarios para todos los IDs proporcionados
        List<Object[]> results = productItemRepository.findProductItemsWithVariations(itemProductIds);

        if (results.isEmpty()) {
            log.error("No Product Items found for the provided IDs: {}", itemProductIds);
            ValidateUtil.requerido(null, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
        }

        // Agrupar resultados por ProductItemId para construir los DTOs
        Map<Long, List<Object[]>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(result -> (Long) result[0]));

        // Construir la lista de ResponseProductItemDetail
        List<ResponseProductItemDetail> responseList = new ArrayList<>();

        for (Long productItemId : groupedResults.keySet()) {
            List<Object[]> productResults = groupedResults.get(productItemId);
            ResponseProductItemDetail dto = buildProductItemDetail(productItemId, productResults);
            responseList.add(dto);
        }
        return responseList;
    }

    private boolean itemProductExistsById(Long itemProductId) {
        return productItemRepository.existsById(itemProductId);
    }
    private Optional<ProductItemEntity> getProductItemById(Long itemProductId) {
        if (!itemProductExistsById(itemProductId)) {
            log.error("Product Item not found with ID: {}", itemProductId);
            ValidateUtil.requerido(null, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
            return Optional.empty();
        }
        return productItemRepository.findById(itemProductId);
    }

    private boolean productItemExistsBySKU(String sku) {
        return productItemRepository.existsByProductItemSKU(sku);
    }
    private Set<VariationOptionEntity> saveVariations(List<RequestVariationName> requestVariationNameList) {
        return requestVariationNameList.stream().map(
                requestVariationName -> {
                    VariationEntity variationEntity = variationRepository.findByVariationName(requestVariationName.getVariationName()).orElse(null);
                    // Check if the variation option already exists
                    if( variationEntity == null) {
                        log.error("The variation does not exist.");
                        ValidateUtil.requerido(variationEntity, ProductsErrorEnum.VARIATION_NOT_FOUND_ERP00029);
                    }
                    if (variationOptionRepository.existsByVariationOptionValue(requestVariationName.getVariationOptionValue())) {
                        return variationOptionRepository.findByVariationOptionValue(requestVariationName.getVariationOptionValue()).get();
                    }
                    VariationOptionEntity variationOptionEntity = VariationOptionEntity.builder()
                            .variationEntity(variationEntity)
                            .variationOptionValue(requestVariationName.getVariationOptionValue())
                            .state(Constants.STATUS_ACTIVE)
                            .createdAt(Constants.getTimestamp())
                            .modifiedByUser(Constants.getUserInSession())
                            .build();
                    return variationOptionRepository.save(variationOptionEntity);
                }).collect(Collectors.toSet());
    }

    private ProductEntity validateAndGetProduct(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId).orElse(null);
        if (productEntity == null){
            log.error("Product not found with ID: {}", productId);
            ValidateUtil.requerido(productEntity, ProductsErrorEnum.PRODUCT_NOT_FOUND_ERP00001);
        }
        return productEntity;
    }


    private ProductCategoryEntity validateAndGetCategory(Long categoryId) {
        ProductCategoryEntity category = productCategoryRepository.findProductCategoryIdAndStateTrue(categoryId).orElse(null);
        if (category == null){
            log.error("Category not found with ID: {}", categoryId);
            ValidateUtil.requerido(category, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
        }
        return category;
    }

    private ProductItemEntity validateAndGetProductItem(Long itemProductId) {
        ProductItemEntity productItem = productItemRepository.findById(itemProductId).orElse(null);
        if(productItem == null) {
            log.error("Product Item not found with ID: {}", itemProductId);
            ValidateUtil.requerido(productItem, ProductsErrorEnum.ITEM_PRODUCT_NOT_FOUND_ERI00001);
        }
        return productItem;
    }
    private List<PromotionDTO> mapPromotionsToDTOs(Set<PromotionEntity> promotionEntities) {
        return promotionEntities.stream()
                .map(promotionMapper::mapPromotionEntityToDto)
                .collect(Collectors.toList());
    }
    private ResponseCategoryy buildResponseCategory(ProductCategoryEntity productCategory) {
        List<PromotionDTO> promotionDTOList = mapPromotionsToDTOs(productCategory.getPromotionEntities());

        return ResponseCategoryy.builder()
                .productCategoryId(productCategory.getProductCategoryId())
                .productCategoryName(productCategory.getProductCategoryName())
                .promotionDTOList(promotionDTOList)
                .build();
    }
    private ResponseProductItemDetail buildProductItemDetail(Long productItemId, List<Object[]> results) {
        Object[] firstResult = results.get(0);

        ProductItemEntity productItemEntity = validateAndGetProductItem(productItemId);
        ProductEntity productEntity = validateAndGetProduct(productItemEntity.getProductEntity().getProductId());
        ProductCategoryEntity productCategory = validateAndGetCategory(productEntity.getProductCategoryEntity().getProductCategoryId());

        ResponseCategoryy responseCategoryy = buildResponseCategory(productCategory);
        List<ResponseVariationn> variations = results.stream()
                .map(result -> new ResponseVariationn((String) result[5], (String) result[6]))
                .toList();

        return ResponseProductItemDetail.builder()
                .productItemId((Long) firstResult[0])
                .productItemSKU((String) firstResult[1])
                .productItemQuantityInStock((Integer) firstResult[2])
                .productItemImage((String) firstResult[3])
                .productItemPrice((BigDecimal) firstResult[4])
                .responseCategoryy(responseCategoryy)
                .variations(variations)
                .build();
    }
    private void updateStock(List<Product> products, int multiplier) {
        List<ProductItemEntity> productItemEntityList = products.stream()
                .map(product -> {
                    ProductItemEntity productItemEntity = validateAndGetProductItem(product.getProductId());
                    productItemEntity.setProductItemQuantityInStock(
                            productItemEntity.getProductItemQuantityInStock() + (multiplier * product.getQuantity())
                    );
                    return productItemEntity;
                }).toList();
        productItemRepository.saveAll(productItemEntityList);
        log.info("Stock updated successfully");
    }
}
