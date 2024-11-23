package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestVariationName;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ItemProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.ProductInsufficientQuantityException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemProductAdapter implements ItemProductServiceOut {
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final PromotionMapper promotionMapper;
    private final VariationMapper variationMapper;
    private final VariationOptionMapper variationOptionMapper;
    private final ProductItemMapper productItemMapper;

    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;
    private final VariationRepository variationRepository;
    private final VariationOptionRepository variationOptionRepository;

    private static final Logger logger = LoggerFactory.getLogger(ItemProductAdapter.class);

    @Transactional
    @Override
    public ProductItemDTO createItemProductOut(RequestItemProduct requestItemProduct) {
        logger.info("Creating itemProduct id parent: {}", requestItemProduct.getProductId());
        if (productItemExistsBySKU(requestItemProduct.getProductItemSKU())) throw new RuntimeException("The sku already exists.");
        if(!productRepository.existsById(requestItemProduct.getProductId())) throw new RuntimeException("The product doesn't  exists.");
        ProductEntity productEntity = productRepository.findById(requestItemProduct.getProductId()).get();
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
                .modifiedByUser("TEST")
                .state(Constants.STATUS_ACTIVE)
                .build();

        ProductItemEntity productItemEntitySaved =  productItemRepository.save(productItemEntity);
        logger.info("itemProduct '{}' created successfully with ID: {}", productItemEntity.getProductItemId(), productItemEntity.getProductEntity().getProductId());
        return productItemMapper.mapProductItemEntityToDto(productItemEntitySaved);
    }

    @Override
    public ResponseProductItemDetail findItemProductByIdOut(Long itemProductId) {
        List<Object[]> results = productItemRepository.findProductItemWithVariations(itemProductId);

        if (results.isEmpty()) {
            throw new AppExceptionNotFound("Product Item not found");
        }

        // Tomar los datos generales del primer resultado
        Object[] firstResult = results.get(0);
        ResponseProductItemDetail dto = new ResponseProductItemDetail();
        dto.setProductItemId((Long) firstResult[0]);
        dto.setProductItemSKU((String) firstResult[1]);
        dto.setProductItemQuantityInStock((Integer) firstResult[2]);
        dto.setProductItemImage((String) firstResult[3]);
        dto.setProductItemPrice((BigDecimal) firstResult[4]);

        // Construir la lista de variaciones
        List<ResponseVariationn> variations = results.stream()
                .map(result -> new ResponseVariationn((String) result[5], (String) result[6]))
                .toList();

        dto.setVariations(variations);
        return dto;
    }
    @Transactional
    @Override
    public ProductItemDTO updateItemProductOut(Long itemProductId, RequestItemProduct requestItemProduct) {
        logger.info("Searching for update product with ID: {}", itemProductId);

        if(!productRepository.existsById(itemProductId)) throw new AppExceptionNotFound("The product doesn't  exists.");
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
                .modifiedByUser("TEST-UPDATED")
                .build();
        ProductItemEntity productItemSaved = productItemRepository.save(productItemEntity1);
        logger.info("itemProduct updated with ID: {}", productItemSaved.getProductItemId());
        return productItemMapper.mapProductItemEntityToDto(productItemSaved);
    }


    @Override
    public ProductItemDTO deleteItemProductOut(Long itemProductId) {
        logger.info("Searching itemProduct for delete with ID: {}", itemProductId);

        ProductItemEntity productItemEntityOptional = getProductItemById(itemProductId).orElseThrow(
                ()-> new AppExceptionNotFound("The itemProduct does not exist.")
        );
        productItemEntityOptional.setDeletedAt(Constants.getTimestamp());
        productItemEntityOptional.setState(Constants.STATUS_INACTIVE);
        productItemEntityOptional.setModifiedByUser("TEST");
        productItemEntityOptional.setProductItemId(null);
        ProductItemEntity itemProductDeleted = productItemRepository.save(productItemEntityOptional);

        logger.info("Product deleted with ID: {}", itemProductDeleted.getProductItemId());

        return productItemMapper.mapProductItemEntityToDto(itemProductDeleted);
    }

    @Override
    public List<Product> reserveProductOut(Long shopOrderId, List<Product> desiredProducts) {

        List<ProductItemEntity> productItemEntityList = desiredProducts.stream()
                .map(requestProductsEvent -> {
                    Optional<ProductItemEntity> productItemEntity = productItemRepository.findByProductItemIdAndStateTrue(requestProductsEvent.getProductId());
                    if (productItemEntity.isEmpty()) {
                        throw new AppExceptionNotFound("The product does not exist.");
                    }
                    ProductItemEntity productItemSaved = productItemEntity.get();
                    if (productItemSaved.getProductItemQuantityInStock() < requestProductsEvent.getQuantity()) {
                        throw new ProductInsufficientQuantityException(productItemSaved.getProductItemId(), shopOrderId);
                    }
                    productItemSaved.setProductItemQuantityInStock(productItemSaved.getProductItemQuantityInStock() - requestProductsEvent.getQuantity());
                    return productItemEntity.get();
                }).toList();
        productItemRepository.saveAll(productItemEntityList);
        return desiredProducts.stream().map(p-> {
            ProductItemEntity productItemEntity = productItemRepository.findByProductItemIdAndStateTrue(p.getProductId()).orElseThrow(()-> new AppExceptionNotFound("The product does not exist."));
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
        List<ProductItemEntity> productItemEntityList = productsToCancel.stream()
                .map(requestProductsEvent -> {
                    Optional<ProductItemEntity> productItemEntity = productItemRepository.findByProductItemIdAndStateTrue(requestProductsEvent.getProductId());
                    if (productItemEntity.isEmpty()) {
                        throw new AppExceptionNotFound("The product does not exist.");
                    }
                    ProductItemEntity productItemSaved = productItemEntity.get();
                    productItemSaved.setProductItemQuantityInStock(productItemSaved.getProductItemQuantityInStock() + requestProductsEvent.getQuantity());
                    return productItemEntity.get();
                }).toList();
        productItemRepository.saveAll(productItemEntityList);
    }
    private boolean itemProductExistsById(Long itemProductId) {
        return productItemRepository.existsById(itemProductId);
    }
    private Optional<ProductItemEntity> getProductItemById(Long itemProductId) {
        if (!itemProductExistsById(itemProductId)) throw new AppExceptionNotFound("The itemProduct does not exist.");
        return productItemRepository.findById(itemProductId);
    }

    private boolean productItemExistsBySKU(String sku) {
        return productItemRepository.existsByProductItemSKU(sku);
    }
    private Set<VariationOptionEntity> saveVariations(List<RequestVariationName> requestVariationNameList) {
        return requestVariationNameList.stream().map(
                requestVariationName -> {
                    VariationEntity variationEntity = variationRepository.findByVariationName(requestVariationName.getVariationName()).orElseThrow(
                            ()-> new AppExceptionNotFound("The variation does not exist.")
                    );
                    if (variationOptionRepository.existsByVariationOptionValue(requestVariationName.getVariationOptionValue())) {
                        return variationOptionRepository.findByVariationOptionValue(requestVariationName.getVariationOptionValue()).get();
                    }
                    VariationOptionEntity variationOptionEntity = VariationOptionEntity.builder()
                            .variationEntity(variationEntity)
                            .variationOptionValue(requestVariationName.getVariationOptionValue())
                            .state(Constants.STATUS_ACTIVE)
                            .modifiedAt(Constants.getTimestamp())
                            .modifiedByUser("TEST")
                            .build();
                    return variationOptionRepository.save(variationOptionEntity);
                }).collect(Collectors.toSet());
    }
}
