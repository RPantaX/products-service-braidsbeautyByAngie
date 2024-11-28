package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.*;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdapter implements ProductServiceOut {

    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductItemRepository productItemRepository;

    @Transactional
    @Override
    public ProductDTO createProductOut(RequestProduct requestProduct) {
        log.info("Creating product with name: {}", requestProduct.getProductName());
        if(productNameExistsByName(requestProduct.getProductName())) throw new AppException("The product name already exists");
        ProductCategoryEntity productCategorySaved = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElseThrow(()->
                new AppExceptionNotFound("Category not found"));

        ProductEntity productEntity = ProductEntity.builder()
                .productName(requestProduct.getProductName())
                .productDescription(requestProduct.getProductDescription())
                .productImage(requestProduct.getProductImage())
                .productCategoryEntity(productCategorySaved)
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("TEST")
                .createdAt(Constants.getTimestamp())
                .build();

        ProductEntity productEntitySaved = productRepository.save(productEntity);

        log.info("Product '{}' created successfully with ID: {}",productEntity.getProductName(),productEntity.getProductId());
        return productMapper.mapProductEntityToDto(productEntitySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseProduct findProductByIdOut(Long productId) {
        List<Object[]> results = productRepository.findProductDetailWithCategoryById(productId);
        if (results.isEmpty()) {
            throw new AppExceptionNotFound("Product not found with ID: " + productId);
        }

        ProductEntity productEntity = productRepository.findProductByProductIdWithStateTrue(productId).orElseThrow(() ->
                new AppExceptionNotFound("Product not found with ID: " + productId));

        ProductCategoryEntity productCategory = productCategoryRepository.findProductCategoryIdAndStateTrue(
                Optional.ofNullable(productEntity.getProductCategoryEntity())
                        .map(ProductCategoryEntity::getProductCategoryId)
                        .orElseThrow(() -> new AppExceptionNotFound("Category not found"))
        ).orElseThrow(() -> new AppExceptionNotFound("Category not found"));

        List<PromotionDTO> promotionDTOList = productCategory.getPromotionEntities().stream()
                .map(promotionMapper::mapPromotionEntityToDto)
                .collect(Collectors.toList());

        ResponseCategoryy responseCategoryy = ResponseCategoryy.builder()
                .productCategoryId(productCategory.getProductCategoryId())
                .productCategoryName(productCategory.getProductCategoryName())
                .promotionDTOList(promotionDTOList)
                .build();

        // Mapear datos del producto
        ResponseProduct productDetail = ResponseProduct.builder()
                .productId(Optional.ofNullable((Long) results.get(0)[0]).orElse(null))
                .productName(Optional.ofNullable((String) results.get(0)[1]).orElse(""))
                .productDescription(Optional.ofNullable((String) results.get(0)[2]).orElse(""))
                .productImage(Optional.ofNullable((String) results.get(0)[3]).orElse(""))
                .responseProductItemDetails(new ArrayList<>())
                .responseCategory(responseCategoryy)
                .build();

        // Mapear ítems y variaciones
        Map<Long, ResponseProductItemDetaill> itemMap = new HashMap<>();

        for (Object[] row : results) {
            Long itemId = Optional.ofNullable((Long) row[4]).orElse(null);
            if (itemId != null && !itemMap.containsKey(itemId)) {
                ResponseProductItemDetaill itemDetail = ResponseProductItemDetaill.builder()
                        .productItemId(itemId)
                        .productItemSKU(Optional.ofNullable((String) row[5]).orElse(""))
                        .productItemQuantityInStock(Optional.ofNullable((Integer) row[6]).orElse(0))
                        .productItemImage(Optional.ofNullable((String) row[7]).orElse(""))
                        .productItemPrice(Optional.ofNullable((BigDecimal) row[8]).orElse(BigDecimal.ZERO))
                        .variations(new ArrayList<>())
                        .build();
                itemMap.put(itemId, itemDetail);
                productDetail.getResponseProductItemDetails().add(itemDetail);
            }

            if (itemId != null) {
                ResponseVariationn variationDetail = ResponseVariationn.builder()
                        .variationName(Optional.ofNullable((String) row[9]).orElse(""))
                        .options(Optional.ofNullable((String) row[10]).orElse(""))
                        .build();
                itemMap.get(itemId).getVariations().add(variationDetail);
            }
        }

        return productDetail;
    }
    @Transactional
    @Override
    public ProductDTO updateProductOut(Long productId, RequestProduct requestProduct) {
        log.info("Searching for update product with ID: {}", productId);
        ProductEntity productEntitySaved = getProductEntity(productId);

        if(!productEntitySaved.getProductName().equalsIgnoreCase(requestProduct.getProductName()) && productNameExistsByName(requestProduct.getProductName())) {throw new RuntimeException("The product name already exists");}

        if(requestProduct.getProductCategoryId() !=null && productCategoryRepository.existByProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId())) {
            ProductCategoryEntity productCategorySaved = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElseThrow(()->
                    new AppExceptionNotFound("Category not found"));
            productEntitySaved.setProductCategoryEntity(productCategorySaved);

        }
        productEntitySaved.setModifiedByUser("TEST-UPDATED");
        productEntitySaved.setModifiedAt(Constants.getTimestamp());
        productEntitySaved.setProductName(requestProduct.getProductName());
        productEntitySaved.setProductDescription(requestProduct.getProductDescription());
        productEntitySaved.setProductImage(requestProduct.getProductImage());
        ProductEntity productEntityUpdated =productRepository.save(productEntitySaved);
        log.info("product updated with ID: {}", productEntitySaved.getProductId());
        return productMapper.mapProductEntityToDto(productEntityUpdated);
    }
    @Override
    public ProductDTO deleteProductOut(Long productId) {
        log.info("Searching product for delete with ID: {}", productId);
        ProductEntity productEntitySaved = getProductEntity(productId);
        List<ProductItemEntity> productItemEntities = productEntitySaved.getProductItemEntities();
        productItemEntities.forEach(productItemEntity -> {
            productItemEntity.setState(Constants.STATUS_INACTIVE);
            productItemEntity.setDeletedAt(Constants.getTimestamp());
            productItemEntity.setModifiedByUser("TEST-PRODUCT");
        });
        try {
            productItemRepository.saveAll(productItemEntities);
        } catch (Exception e) {
            log.error("Error deleting product items: {}", e.getMessage());
            throw new AppException("Error deleting product items");
        }
        productEntitySaved.setModifiedByUser("TEST");
        productEntitySaved.setProductCategoryEntity(null);
        productEntitySaved.setDeletedAt(Constants.getTimestamp());
        productEntitySaved.setState(Constants.STATUS_INACTIVE);
        log.info("Product deleted with ID: {}", productId);
        return productMapper.mapProductEntityToDto(productRepository.save(productEntitySaved));
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {

        log.info("Searching all products with the following parameters: {}",Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() :
                Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ProductEntity> productPage = productRepository.findAllByStateTrueAndPageable(pageable);

        // Convertir entidades a DTOs
        List<ResponseProduct> responseProductList = productPage.getContent().stream().map(product -> {

            ProductEntity productEntity = productRepository.findProductByProductIdWithStateTrue(product.getProductId()).orElseThrow(()-> {
                log.error("Product not found with ID: {}", product.getProductId());
                return new AppExceptionNotFound("Product not found with ID: " + product.getProductId());
            });

            ProductCategoryEntity productCategory = productCategoryRepository.findProductCategoryIdAndStateTrue(productEntity.getProductCategoryEntity().getProductCategoryId()).orElseThrow(()-> {
                log.error("Category not found");
                return new AppExceptionNotFound("Category not found");
            });
            List<PromotionDTO> promotionDTOList = productCategory.getPromotionEntities().stream()
                    .map(promotionMapper::mapPromotionEntityToDto)
                    .collect(Collectors.toList());
            ResponseCategoryy responseCategoryy = ResponseCategoryy.builder()
                    .productCategoryId(productCategory.getProductCategoryId())
                    .productCategoryName(productCategory.getProductCategoryName())
                    .promotionDTOList(promotionDTOList)
                    .build();

            List<ResponseProductItemDetaill> productItemDetails = product.getProductItemEntities().stream().map(item -> {

                List<ResponseVariationn> variations = item.getVariationOptionEntitySet().stream()
                        .map(variationOption -> {
                            VariationEntity variationEntity = variationOption.getVariationEntity();
                            // Manejar casos nulos
                            String variationName = variationEntity != null ? variationEntity.getVariationName() : "Unknown Variation";
                            String variationValue = variationOption.getVariationOptionValue();

                            return new ResponseVariationn(variationName, variationValue);
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
            }).collect(Collectors.toList());

            return new ResponseProduct(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductDescription(),
                    product.getProductImage(),
                    responseCategoryy,
                    productItemDetails

            );
        }).collect(Collectors.toList());

        // Crear el objeto de respuesta paginada
        return new ResponseListPageableProduct(
                responseProductList,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.isLast()
        );
    }


    private boolean productNameExistsByName(String productName){ return productRepository.existsByProductName(productName); }

    private ProductEntity getProductEntity(Long productId) {
        return productRepository.findProductByProductIdWithStateTrue(productId).orElseThrow(() -> {
            log.error("Product not found with ID: {}", productId);
            return new AppExceptionNotFound("The product does not exist.");
        });
    }

}
