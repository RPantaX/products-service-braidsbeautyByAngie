package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.constants.ProductsErrorEnum;
import com.braidsbeautyByAngie.aggregates.dto.*;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.aws.IBucketUtil;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.BucketParams;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.GlobalErrorEnum;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ValidateUtil;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final VariationRepository variationRepository;
    private final VariationOptionRepository variationOptionRepository;
    private final IBucketUtil bucketUtil;
    @Value("${BUCKET_NAME_USUARIOS}")
    private String bucketName;

    @Transactional
    @Override
    public ProductDTO createProductOut(RequestProduct requestProduct) {
        String productNameUpperCase = requestProduct.getProductName().toUpperCase();
        log.info("Creating product with name: {}", productNameUpperCase);
        if(productNameExistsByName(productNameUpperCase)) ValidateUtil.evaluar(false, ProductsErrorEnum.PRODUCT_ALREADY_EXISTS_ERP00002);
        ProductCategoryEntity productCategorySaved = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElse(null);
        ValidateUtil.evaluar(productCategorySaved != null, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
        ProductEntity productEntity = ProductEntity.builder()
                .productName(productNameUpperCase)
                .productDescription(requestProduct.getProductDescription())
                .productCategoryEntity(productCategorySaved)
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser(Constants.getUserInSession())
                .createdAt(Constants.getTimestamp())
                .build();

        ProductEntity productEntitySaved = productRepository.save(productEntity);


        if( requestProduct.getImagen() != null && !requestProduct.getImagen().isEmpty()) {
            String imageUrl = saveImageInS3(requestProduct.getImagen(), productEntitySaved.getProductId());
            productEntitySaved.setProductImage(imageUrl);
            productRepository.save(productEntitySaved);
        }
        log.info("Product '{}' created successfully with ID: {}",productEntity.getProductName(),productEntity.getProductId());
        return productMapper.mapProductEntityToDto(productEntitySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseProduct findProductByIdOut(Long productId) {
        List<Object[]> results = productRepository.findProductDetailWithCategoryById(productId);
        if (results.isEmpty()) {
            ValidateUtil.evaluar(false, ProductsErrorEnum.PRODUCT_NOT_FOUND_ERP00001);
        }

        ProductEntity productEntity = productRepository.findProductByProductIdWithStateTrue(productId).orElse(null);
        ValidateUtil.evaluar(productEntity != null, ProductsErrorEnum.PRODUCT_NOT_FOUND_ERP00001);
        ValidateUtil.evaluar(productEntity.getProductCategoryEntity() != null, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
        ProductCategoryEntity productCategory = productCategoryRepository.findProductCategoryIdAndStateTrue(
                Optional.ofNullable(productEntity.getProductCategoryEntity())
                        .map(ProductCategoryEntity::getProductCategoryId)
                        .orElse(null)
        ).orElse(null);
        ValidateUtil.evaluar(productCategory != null, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
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
        String productNameUpperCase = requestProduct.getProductName().toUpperCase();
        log.info("Searching for update product with ID: {}", productId);
        ProductEntity productEntitySaved = getProductEntity(productId);

        if(!productEntitySaved.getProductName().equalsIgnoreCase(productNameUpperCase) && productNameExistsByName(productNameUpperCase)) {
            log.warn("Attempted to update product to an existing name: {}", productNameUpperCase);
            ValidateUtil.evaluar(false, ProductsErrorEnum.PRODUCT_ALREADY_EXISTS_ERP00002);
        }

        if(requestProduct.getProductCategoryId() !=null && productCategoryRepository.existByProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId())) {
            ProductCategoryEntity productCategorySaved = productCategoryRepository.findProductCategoryIdAndStateTrue(requestProduct.getProductCategoryId()).orElse(null);
            if (productCategorySaved == null) {
                log.error("Product category not found with ID: {}", requestProduct.getProductCategoryId());
                ValidateUtil.evaluar(false, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
            }
            productEntitySaved.setProductCategoryEntity(productCategorySaved);

        }
        productEntitySaved.setModifiedByUser(Constants.getUserInSession());
        productEntitySaved.setModifiedAt(Constants.getTimestamp());
        productEntitySaved.setProductName(productNameUpperCase);
        productEntitySaved.setProductDescription(requestProduct.getProductDescription());

        ProductEntity productEntityUpdated =productRepository.save(productEntitySaved);
        // Manejar actualización de imagen
        updateImageInS3(requestProduct.getImagen(), productId, productEntityUpdated, requestProduct.isDeleteFile());
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
            productItemEntity.setModifiedByUser(Constants.getUserInSession());
        });
        try {
            productItemRepository.saveAll(productItemEntities);
        } catch (Exception e) {
            log.error("Error deleting product items: {}", e.getMessage());
            ValidateUtil.evaluar(false, ProductsErrorEnum.PRODUCT_DELETION_FAILED_ERP00005);
        }
        productEntitySaved.setModifiedByUser(Constants.getUserInSession());
        productEntitySaved.setProductCategoryEntity(null);
        productEntitySaved.setDeletedAt(Constants.getTimestamp());
        productEntitySaved.setState(Constants.STATUS_INACTIVE);
        //deleteOldImageFromS3(productId);
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

            ProductEntity productEntity = productRepository.findProductByProductIdWithStateTrue(product.getProductId()).orElse(null);
            if (productEntity == null) {
                log.error("Product category is null for product ID: {}", product.getProductId());
                ValidateUtil.evaluar(false, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
            }
            ProductCategoryEntity productCategory = productCategoryRepository.findProductCategoryIdAndStateTrue(productEntity.getProductCategoryEntity().getProductCategoryId()).orElse(null);
            if (productCategory == null) {
                log.error("Category not found for product ID: {}", product.getProductId());
                ValidateUtil.evaluar(false, GlobalErrorEnum.CATEGORY_NOT_FOUND_ERC00008);
            }
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

    @Override
    public ResponseListPageableProduct filterProductsOut(RequestProductFilter filter) {
        log.info("Executing product filter in adapter with parameters: {}", filter);
        // Validaciones de negocio si son necesarias
        validateFilterRequest(filter);
        return productCategoryRepository.filterProducts(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseProductFilterOptions getProductFilterOptionsOut() {
        log.info("Getting product filter options");

        try {
            // Obtener categorías activas
            List<ProductCategoryEntity> categories = productCategoryRepository
                    .findAllByStateTrue()
                    .stream()
                    .filter(cat -> cat.getParentCategory() == null) // Solo categorías padre
                    .collect(Collectors.toList());

            List<ResponseCategoryOption> categoryOptions = categories.stream()
                    .map(category -> ResponseCategoryOption.builder()
                            .id(category.getProductCategoryId())
                            .name(category.getProductCategoryName())
                            .productCount(getProductCountByCategory(category.getProductCategoryId()))
                            .build())
                    .collect(Collectors.toList());

            // Obtener rango de precios
            ResponsePriceRange priceRange = getPriceRange();

            // Obtener variaciones disponibles
            List<ResponseVariationOption> variationOptions = getVariationOptions();

            return ResponseProductFilterOptions.builder()
                    .categories(categoryOptions)
                    .priceRange(priceRange)
                    .variations(variationOptions)
                    .build();

        } catch (Exception e) {
            log.error("Error getting product filter options: {}", e.getMessage());
            throw new RuntimeException("Error al obtener opciones de filtro", e);
        }
    }
    private int getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryIdAndStateTrue(categoryId);
    }

    private ResponsePriceRange getPriceRange() {
        List<Object[]> result = productItemRepository.findPriceRange();
        if (result == null || result.isEmpty()) {
            return ResponsePriceRange.builder()
                    .min(BigDecimal.ZERO)
                    .max(BigDecimal.ZERO)
                    .build();
        }
        Object[] priceRange = result.get(0);

        BigDecimal minPrice = priceRange[0] != null ? new BigDecimal(priceRange[0].toString()) : BigDecimal.ZERO;
        BigDecimal maxPrice = priceRange[1] != null ? new BigDecimal(priceRange[1].toString()) : BigDecimal.ZERO;

        return ResponsePriceRange.builder()
                .min(minPrice)
                .max(maxPrice)
                .build();

    }

    private List<ResponseVariationOption> getVariationOptions() {
        // Obtener todas las variaciones activas con sus opciones
        List<VariationEntity> variations = variationRepository.findAllByStateTrue();

        return variations.stream()
                .map(variation -> {
                    List<String> options = variationOptionRepository
                            .findAllByVariationEntityAndStateTrue(variation)
                            .stream()
                            .map(VariationOptionEntity::getVariationOptionValue)
                            .distinct()
                            .collect(Collectors.toList());

                    return ResponseVariationOption.builder()
                            .name(variation.getVariationName())
                            .options(options)
                            .build();
                })
                .collect(Collectors.toList());
    }
    private void validateFilterRequest(RequestProductFilter filter) {
        // Validar rangos de precio
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null) {
            if (filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
                throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
            }
        }

        // Validar rangos de descuento
        if (filter.getMinDiscountRate() != null && filter.getMaxDiscountRate() != null) {
            if (filter.getMinDiscountRate().compareTo(filter.getMaxDiscountRate()) > 0) {
                throw new IllegalArgumentException("La tasa de descuento mínima no puede ser mayor a la máxima");
            }
        }

        // Validar paginación
        if (filter.getPageNumber() < 0) {
            filter.setPageNumber(0);
        }

        if (filter.getPageSize() <= 0 || filter.getPageSize() > 100) {
            filter.setPageSize(10);
        }
    }
    private String saveImageInS3(MultipartFile imagen, Long productId ) {
        BucketParams bucketParams = buildBucketParams(productId, imagen);
        bucketUtil.addFile(bucketParams);
        //bucketUtil.setPublic(bucketParams, true);
        return bucketUtil.getUrl(bucketParams);
    }
    public BucketParams buildBucketParams(Long productId, MultipartFile imagen){
        String fileName = "product-" + productId + "-" + System.currentTimeMillis();

        // Para operaciones de eliminación (cuando imagen es null)
        if (imagen == null) {
            // Construir el path basado en el patrón de nombres que usamos
            String filePath = "products/" + fileName; // Sin extensión para eliminación
            return BucketParams.builder()
                    .bucketName(bucketName)
                    .filePath(filePath)
                    .build();
        } else {
            // Para operaciones de creación/actualización
            String originalFileName = imagen.getOriginalFilename();
            if (originalFileName != null && originalFileName.contains(".")) {
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                fileName += extension;
            }

            return BucketParams.builder()
                    .file(imagen)
                    .bucketName(bucketName)
                    .filePath("products/" + fileName)
                    .build();
        }
    }
    private void updateImageInS3(MultipartFile newImage, Long productId, ProductEntity productEntity, boolean deleteFile) {
        String currentImageUrl = productEntity.getProductImage();

        if (deleteFile && currentImageUrl != null && !currentImageUrl.isEmpty()) {
            // Eliminar imagen existente
            Constants.deleteOldImageFromS3(currentImageUrl, bucketUtil, bucketName);
            productEntity.setProductImage(null);
            productRepository.save(productEntity);
            log.info("Image deleted for product ID: {}", productId);
            return;
        }

        if (newImage != null && !newImage.isEmpty()) {
            // Si ya existe una imagen, eliminarla primero
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Constants.deleteOldImageFromS3(currentImageUrl, bucketUtil, bucketName);
                log.info("Old image replaced for product ID: {}", productId);
            }

            // Guardar nueva imagen
            String newImageUrl = saveImageInS3(newImage, productId);
            productEntity.setProductImage(newImageUrl);
            productRepository.save(productEntity);
            log.info("New image saved for product ID: {}", productId);
        }
    }

    private boolean productNameExistsByName(String productName){ return productRepository.existsByProductName(productName); }

    private ProductEntity getProductEntity(Long productId) {
        ProductEntity product = productRepository.findProductByProductIdWithStateTrue(productId).orElse(null);
        if (product == null) {
            log.error("Product not found with ID: {}", productId);
            ValidateUtil.evaluar(false, ProductsErrorEnum.PRODUCT_NOT_FOUND_ERP00001);
        }
        return product;
    }

}
