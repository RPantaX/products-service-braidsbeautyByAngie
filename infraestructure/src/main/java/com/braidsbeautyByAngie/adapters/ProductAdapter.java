package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.*;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseSubCategory;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public ProductDTO createProductOut(RequestProduct requestProduct) {
        logger.info("Creating product with name: {}", requestProduct.getProductName());
        if(productNameExistsByName(requestProduct.getProductName())) throw new RuntimeException("The product name already exists");
        if (!productCategoryRepository.existByProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId())) throw new RuntimeException("Category not found");
        ProductCategoryEntity productCategoryEntity = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));;
        //todo: validar la existencia por id de las promociones
        List<PromotionEntity> promotionSaved = productCategoryEntity.getPromotionEntities().stream().collect(Collectors.toList());
        List<PromotionEntity> promotionEntityList= promotionRepository.findAllByPromotionIdAndStateTrue(requestProduct.getPromotionId());
        promotionSaved.addAll(promotionEntityList);
        productCategoryEntity.setPromotionEntities(promotionSaved.stream().collect(Collectors.toSet()));
        //add promotiones to our repository
        ProductCategoryEntity productCategorySaved = productCategoryRepository.save(productCategoryEntity);
        //todo:quitar el precio de producto, ya que existe solo en productoItem
        ProductEntity productEntity = ProductEntity.builder()
                .productName(requestProduct.getProductName())
                .productDescription(requestProduct.getProductDescription())
                .productImage(requestProduct.getProductImage())
                .productCategoryEntity(productCategorySaved)
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("TEST")
                .createdAt(Constants.getTimestamp())
                .build();
        logger.info("Product '{}' created successfully with ID: {}",productEntity.getProductName(),productEntity.getProductId());
        return productMapper.mapProductEntityToDto(productRepository.save(productEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResponseProduct> findProductByIdOut(Long productId) {
        logger.info("Searching for product with ID: {}", productId);
        Optional<ProductEntity> productEntityOpt = getProductEntity(productId);
        if (productEntityOpt.isEmpty()) {
            return Optional.empty();
        }
        ProductEntity productEntity = productEntityOpt.get();

        // Construir subcategorías y promociones para la categoría del producto
        List<ResponseSubCategory> subCategoryList = productEntity.getProductCategoryEntity().getSubCategories()
                .stream()
                .map(subCat -> {
                    ResponseSubCategory responseSubCategory = new ResponseSubCategory();
                    responseSubCategory.setProductCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(subCat));
                    return responseSubCategory;
                }).toList();

        List<PromotionDTO> promotionDTOList = promotionMapper.mapPromotionListToDtoList(
                productEntity.getProductCategoryEntity().getPromotionEntities());

        ResponseCategory responseCategory = ResponseCategory.builder()
                .responseSubCategoryList(subCategoryList)
                .productCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(productEntity.getProductCategoryEntity()))
                .promotionDTOList(Set.copyOf(promotionDTOList))
                .build();

        // Construir opciones de variación y variaciones finales para el producto
        List<ResponseVariationFinal> responseVariationFinals = productEntity.getProductItemEntities()
                .stream()
                .map(productItemEntity -> {
                    ResponseVariationFinal responseVariationFinal = new ResponseVariationFinal();
                    List<VariationOptionDTO> variationOptionDTOList = productItemEntity.getVariationOptionEntitySet()
                            .stream()
                            .map(variationOptionMapper::mapVariationOptionEntityToDto)
                            .toList();
                    responseVariationFinal.setVariationOptionDTOList(variationOptionDTOList);
                    return responseVariationFinal;
                }).toList();

        List<ResponseVariation> responseVariationList = responseVariationFinals.stream()
                .map(responseVariationFinal -> {
                    ResponseVariation responseVariation = new ResponseVariation();
                    responseVariation.setResponseVariationFinals(responseVariationFinals);
                    return responseVariation;
                }).toList();

        // Construir lista de ítems de productos y producto de respuesta final
        List<ResponseItemProduct> responseItemProducts = productEntity.getProductItemEntities()
                .stream()
                .map(productItem -> ResponseItemProduct.builder()
                        .productDTO(productMapper.mapProductEntityToDto(productEntity))
                        .responseCategory(responseCategory)
                        .responseVariationList(responseVariationList)
                        .build())
                .toList();

        ResponseProduct responseProduct = ResponseProduct.builder()
                .responseItemProducts(responseItemProducts)
                .build();
        logger.info("Product with ID {} found", productId);
        return Optional.of(responseProduct);
    }
    @Transactional
    @Override
    public ProductDTO updateProductOut(Long productId, RequestProduct requestProduct) {
        logger.info("Searching for update product with ID: {}", productId);
        Optional<ProductEntity> productEntitySaved = getProductEntity(productId);

        if(!productEntitySaved.get().getProductName().equalsIgnoreCase(requestProduct.getProductName()) && productNameExistsByName(requestProduct.getProductName())) {throw new RuntimeException("The product name already exists");}
        if (!productCategoryRepository.existByProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId())) {
            logger.error("Error updating product with ID: {}", productId);
            throw new RuntimeException("Category not found");
        }

        ProductCategoryEntity productCategoryEntity = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).get();
        //todo: validar la existencia por id de las promociones
        List<PromotionEntity> promotionSaved = new ArrayList<>(productCategoryEntity.getPromotionEntities());
        List<PromotionEntity> promotionEntityList= promotionRepository.findAllByPromotionIdAndStateTrue(requestProduct.getPromotionId());
        promotionSaved.addAll(promotionEntityList);
        productCategoryEntity.setPromotionEntities(new HashSet<>(promotionSaved));
        //add promotiones to our repository
        ProductCategoryEntity productCategorySaved = productCategoryRepository.save(productCategoryEntity);

        //todo:quitar el precio de producto, ya que existe solo en productoItem
        productEntitySaved.get().setModifiedByUser("TEST");
        productEntitySaved.get().setModifiedAt(Constants.getTimestamp());
        productEntitySaved.get().setProductName(requestProduct.getProductName());
        productEntitySaved.get().setProductDescription(requestProduct.getProductDescription());
        productEntitySaved.get().setProductImage(requestProduct.getProductImage());
        productEntitySaved.get().setProductCategoryEntity(productCategorySaved);
        ProductEntity productEntityUpdated =productRepository.save(productEntitySaved.get());
        logger.info("product updated with ID: {}", productEntitySaved.get().getProductId());
        return productMapper.mapProductEntityToDto(productEntityUpdated);
    }
    @Override
    public ProductDTO deleteProductOut(Long productId) {
        logger.info("Searching product for delete with ID: {}", productId);
        Optional<ProductEntity> productEntitySaved = getProductEntity(productId);
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

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        if (productRepository.findAllByStateTrueAndPageable(pageable).isEmpty()) return null;

        Page<ProductEntity> productEntityPage = productRepository.findAllByStateTrueAndPageable(pageable);
        List<ResponseProduct> responseProductList = productEntityPage.getContent().stream()
                .map(productEntity -> {
                    List<ResponseSubCategory> subCategoryList = productEntity.getProductCategoryEntity().getSubCategories()
                            .stream()
                            .map(subCat -> {
                                ResponseSubCategory responseSubCategory = new ResponseSubCategory();
                                responseSubCategory.setProductCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(subCat));
                                return responseSubCategory;
                            }).toList();

                    List<PromotionDTO> promotionDTOList = promotionMapper.mapPromotionListToDtoList(
                            productEntity.getProductCategoryEntity().getPromotionEntities());

                    ResponseCategory responseCategory = ResponseCategory.builder()
                            .responseSubCategoryList(subCategoryList)
                            .productCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(productEntity.getProductCategoryEntity()))
                            .promotionDTOList(Set.copyOf(promotionDTOList))
                            .build();

                    // Construir opciones de variación y variaciones finales para el producto
                    List<ResponseVariationFinal> responseVariationFinals = productEntity.getProductItemEntities()
                            .stream()
                            .map(productItemEntity -> {
                                ResponseVariationFinal responseVariationFinal = new ResponseVariationFinal();
                                List<VariationOptionDTO> variationOptionDTOList = productItemEntity.getVariationOptionEntitySet()
                                        .stream()
                                        .map(variationOptionMapper::mapVariationOptionEntityToDto)
                                        .toList();
                                responseVariationFinal.setVariationOptionDTOList(variationOptionDTOList);
                                return responseVariationFinal;
                            }).toList();

                    List<ResponseVariation> responseVariationList = responseVariationFinals.stream()
                            .map(responseVariationFinal -> {
                                ResponseVariation responseVariation = new ResponseVariation();
                                responseVariation.setResponseVariationFinals(responseVariationFinals);
                                return responseVariation;
                            }).toList();

                    // Construir lista de ítems de productos y producto de respuesta final
                    List<ResponseItemProduct> responseItemProducts = productEntity.getProductItemEntities()
                            .stream()
                            .map(productItem -> ResponseItemProduct.builder()
                                    .productDTO(productMapper.mapProductEntityToDto(productEntity))
                                    .responseCategory(responseCategory)
                                    .responseVariationList(responseVariationList)
                                    .build())
                            .toList();
                    return ResponseProduct.builder()
                            .responseItemProducts(responseItemProducts)
                            .build();
                }).toList();
        logger.info("Products found with the following parameters: {}", Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));
        return ResponseListPageableProduct.builder()
                .responseProductList(responseProductList)
                .pageNumber(productEntityPage.getNumber())
                .totalElements(productEntityPage.getTotalElements())
                .totalPages(productEntityPage.getTotalPages())
                .pageSize(productEntityPage.getSize())
                .end(productEntityPage.isLast())
                .build();
    }
    private boolean productNameExistsByName(String productName){ return productRepository.existsByProductName(productName); }
    private boolean productExistsById(Long productId) {
        return productRepository.existsByProductIdWithStateTrue(productId);
    }
    private Optional<ProductEntity> getProductEntity(Long productId) {
        if(!productExistsById(productId)) throw new RuntimeException("The product does not exist.");
        return productRepository.findProductByProductIdWithStateTrue(productId);
    }
}
