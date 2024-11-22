package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.*;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseSubCategory;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAdapter implements ProductServiceOut {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final PromotionMapper promotionMapper;
    private final VariationOptionMapper variationOptionMapper;

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final PromotionRepository promotionRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductAdapter.class);
    private final ProductItemMapper productItemMapper;
    private final ItemProductAdapter itemProductAdapter;
    private final ProductItemRepository productItemRepository;

    @Transactional
    @Override
    public ProductDTO createProductOut(RequestProduct requestProduct) {
        logger.info("Creating product with name: {}", requestProduct.getProductName());
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

        logger.info("Product '{}' created successfully with ID: {}",productEntity.getProductName(),productEntity.getProductId());
        return productMapper.mapProductEntityToDto(productEntitySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseProduct findProductByIdOut(Long productId) {
        List<Object[]> results = productRepository.findProductDetailsById(productId);

        if (results.isEmpty()) {
            throw new AppExceptionNotFound("Product not found with ID: " + productId);
        }

        // Mapear datos del producto
        ResponseProduct productDetail = ResponseProduct.builder()
                .productId((Long) results.get(0)[0])
                .productName((String) results.get(0)[1])
                .productDescription((String) results.get(0)[2])
                .productImage((String) results.get(0)[3])
                .responseProductItemDetails(new ArrayList<>())
                .build();

        // Mapear ítems y variaciones
        Map<Long, ResponseProductItemDetaill> itemMap = new HashMap<>();

        for (Object[] row : results) {
            Long itemId = (Long) row[4];
            if (!itemMap.containsKey(itemId)) {
                ResponseProductItemDetaill itemDetail = ResponseProductItemDetaill.builder()
                        .productItemId(itemId)
                        .productItemSKU((String) row[5])
                        .productItemQuantityInStock((Integer) row[6])
                        .productItemImage((String) row[7])
                        .productItemPrice((BigDecimal) row[8])
                        .variations(new ArrayList<>())
                        .build();
                itemMap.put(itemId, itemDetail);
                productDetail.getResponseProductItemDetails().add(itemDetail);
            }

            ResponseVariationn variationDetail = ResponseVariationn.builder()
                    .variationName((String) row[9])
                    .options((String) row[10])
                    .build();

            itemMap.get(itemId).getVariations().add(variationDetail);
        }

        return productDetail;
    }
    @Transactional
    @Override
    public ProductDTO updateProductOut(Long productId, RequestProduct requestProduct) {
        logger.info("Searching for update product with ID: {}", productId);
        Optional<ProductEntity> productEntitySaved = getProductEntity(productId);

        if(!productEntitySaved.get().getProductName().equalsIgnoreCase(requestProduct.getProductName()) && productNameExistsByName(requestProduct.getProductName())) {throw new RuntimeException("The product name already exists");}

        if(requestProduct.getProductCategoryId() !=null && !productCategoryRepository.existByProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId())) {
            ProductCategoryEntity productCategorySaved = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElseThrow(()->
                    new AppExceptionNotFound("Category not found"));
            productEntitySaved.get().setProductCategoryEntity(productCategorySaved);

        }
        productEntitySaved.get().setModifiedByUser("TEST-UPDATED");
        productEntitySaved.get().setModifiedAt(Constants.getTimestamp());
        productEntitySaved.get().setProductName(requestProduct.getProductName());
        productEntitySaved.get().setProductDescription(requestProduct.getProductDescription());
        productEntitySaved.get().setProductImage(requestProduct.getProductImage());
        ProductEntity productEntityUpdated =productRepository.save(productEntitySaved.get());
        logger.info("product updated with ID: {}", productEntitySaved.get().getProductId());
        return productMapper.mapProductEntityToDto(productEntityUpdated);
    }
    @Override
    public ProductDTO deleteProductOut(Long productId) {
        logger.info("Searching product for delete with ID: {}", productId);
        Optional<ProductEntity> productEntitySaved = getProductEntity(productId);
        List<ProductItemEntity> productItemEntities = productEntitySaved.get().getProductItemEntities();
        productItemEntities.forEach(productItemEntity -> {
            productItemEntity.setState(Constants.STATUS_INACTIVE);
            productItemEntity.setDeletedAt(Constants.getTimestamp());
            productItemEntity.setModifiedByUser("TEST-FROM-PRODUCT");
        });
        productItemRepository.deleteAll(productItemEntities);
        productEntitySaved.get().setModifiedByUser("TEST");
        productEntitySaved.get().setProductCategoryEntity(null);
        productEntitySaved.get().setDeletedAt(Constants.getTimestamp());
        productEntitySaved.get().setState(Constants.STATUS_INACTIVE);
        logger.info("Product deleted with ID: {}", productId);
        return productMapper.mapProductEntityToDto(productRepository.save(productEntitySaved.get()));
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {

        logger.info("Searching all products with the following parameters: {}",Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() :
                Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ProductEntity> productPage = productRepository.findAll(pageable);

        // Convertir entidades a DTOs
        List<ResponseProduct> responseProductList = productPage.getContent().stream().map(product -> {
            List<ResponseProductItemDetaill> productItemDetails = product.getProductItemEntities().stream().map(item -> {
                List<ResponseVariationn> variations = item.getVariationOptionEntitySet().stream()
                        .map(variationOption -> new ResponseVariationn(
                                variationOption.getVariationEntity().getVariationName(),
                                variationOption.getVariationOptionValue()))
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
    private boolean productExistsById(Long productId) {
        return productRepository.existsByProductIdWithStateTrue(productId);
    }
    private Optional<ProductEntity> getProductEntity(Long productId) {
        if(!productExistsById(productId)) throw new AppExceptionNotFound("The product does not exist.");
        return productRepository.findProductByProductIdWithStateTrue(productId);
    }
    private ResponseCategoryy buildCategory(Object[] firstResult, List<Object[]> results) {
        // Construir la categoría
        ResponseCategoryy category = new ResponseCategoryy();
        category.setProductCategoryId((Long) firstResult[4]);
        category.setProductCategoryName((String) firstResult[5]);

        // Agregar las promociones
        List<PromotionDTO> promotions = results.stream()
                .filter(result -> result[12] != null) // Filtra las filas que contienen promociones
                .map(result -> new PromotionDTO(
                        (Long) result[12],
                        (String) result[13],
                        (String) result[14],
                        (Double) result[15],
                        (Timestamp) result[16],
                        (Timestamp) result[17]
                ))
                .collect(Collectors.toList());

        category.setPromotionDTOList(promotions);
        return category;
    }

    private List<ResponseProductItemDetaill> buildProductItems(List<Object[]> results) {
        // Construir la lista de elementos de productos
        return results.stream()
                .map(result -> {
                    ResponseProductItemDetaill item = new ResponseProductItemDetaill();
                    item.setProductItemId((Long) result[6]);
                    item.setProductItemSKU((String) result[7]);
                    item.setProductItemQuantityInStock((Integer) result[8]);
                    item.setProductItemImage((String) result[9]);
                    item.setProductItemPrice((BigDecimal) result[10]);
                    item.setVariations(buildVariations(result));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<ResponseVariationn> buildVariations(Object[] result) {
        // Construir la lista de variaciones para cada producto item
        ResponseVariationn variation = new ResponseVariationn(
                (String) result[11],  // variationName
                (String) result[12]   // variationOptionValue
        );
        return List.of(variation);
    }
}
