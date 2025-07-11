package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.constants.ProductsErrorEnum;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.*;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.ProductMapper;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.GlobalErrorEnum;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ValidateUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductAdapter Unit Tests")
class ProductAdapterTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @InjectMocks
    private ProductAdapter productAdapter;

    private ProductEntity productEntity;
    private ProductDTO productDTO;
    private RequestProduct requestProduct;
    private ProductCategoryEntity categoryEntity;
    private ProductItemEntity productItemEntity;
    private VariationOptionEntity variationOptionEntity;
    private VariationEntity variationEntity;
    private PromotionEntity promotionEntity;
    private Timestamp currentTimestamp;

    @BeforeEach
    void setUp() {
        currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

        // Setup promotion entity
        promotionEntity = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDiscountRate(BigDecimal.valueOf(0.10))
                .state(true)
                .build();

        // Setup category entity
        categoryEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(true)
                .promotionEntities(Set.of(promotionEntity))
                .build();

        // Setup variation entities
        variationEntity = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .state(true)
                .build();

        variationOptionEntity = VariationOptionEntity.builder()
                .variationOptionId(1L)
                .variationOptionValue("Red")
                .variationEntity(variationEntity)
                .state(true)
                .build();

        // Setup product item entity
        productItemEntity = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemSKU("SKU001")
                .productItemPrice(BigDecimal.valueOf(999.99))
                .productItemQuantityInStock(10)
                .productItemImage("item_image.jpg")
                .variationOptionEntitySet(Set.of(variationOptionEntity))
                .state(true)
                .build();

        // Setup product entity
        productEntity = ProductEntity.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                .productImage("iphone15.jpg")
                .productCategoryEntity(categoryEntity)
                .productItemEntities(Arrays.asList(productItemEntity))
                .state(true)
                .createdAt(currentTimestamp)
                .modifiedByUser("testUser")
                .build();

        // Setup DTOs
        productDTO = ProductDTO.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                .productImage("iphone15.jpg")
                .build();

        // Setup request
        requestProduct = RequestProduct.builder()
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                //.productImage("iphone15.jpg")
                .productCategoryId(1L)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProductOut_WithValidData_ShouldReturnProductDTO() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.existsByProductName("iPhone 15")).thenReturn(false);
            when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(productMapper.mapProductEntityToDto(productEntity)).thenReturn(productDTO);

            // When
            ProductDTO result = productAdapter.createProductOut(requestProduct);

            // Then
            assertNotNull(result);
            assertEquals("iPhone 15", result.getProductName());
            assertEquals("Latest iPhone model", result.getProductDescription());
            assertEquals("iphone15.jpg", result.getProductImage());
            assertEquals(1L, result.getProductId());

            verify(productRepository).existsByProductName("iPhone 15");
            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productRepository).save(any(ProductEntity.class));
            verify(productMapper).mapProductEntityToDto(productEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when product name already exists")
    void createProductOut_WithExistingProductName_ShouldThrowException() {
        // Given
        when(productRepository.existsByProductName("iPhone 15")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.createProductOut(requestProduct);
            });

            verify(productRepository).existsByProductName("iPhone 15");
            verify(productCategoryRepository, never()).findProductCategoryIdAndStateTrue(anyLong());
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void createProductOut_WithInvalidCategoryId_ShouldThrowException() {
        // Given
        when(productRepository.existsByProductName("iPhone 15")).thenReturn(false);
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.createProductOut(requestProduct);
            });

            verify(productRepository).existsByProductName("iPhone 15");
            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should find product by ID successfully")
    void findProductByIdOut_WithValidId_ShouldReturnResponseProduct() {
        // Given - Simulating the exact query result from findProductDetailWithCategoryById
        Object[] mockResult = {
                1L, "iPhone 15", "Latest iPhone model", "iphone15.jpg", // Product data (columns 0-3)
                1L, "SKU001", 10, "item_image.jpg", BigDecimal.valueOf(999.99), // Product item data (columns 4-8)
                "Color", "Red" // Variation data (columns 9-10)
        };
        List<Object[]> results = Arrays.asList(new Object[][]{ mockResult });

        PromotionDTO promotionDTO = PromotionDTO.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDiscountRate(0.10)
                .build();

        when(productRepository.findProductDetailWithCategoryById(1L)).thenReturn(results);
        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
        when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);

        // When
        ResponseProduct result = productAdapter.findProductByIdOut(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals("iPhone 15", result.getProductName());
        assertEquals("Latest iPhone model", result.getProductDescription());
        assertEquals("iphone15.jpg", result.getProductImage());

        // Verify category response
        assertNotNull(result.getResponseCategory());
        assertEquals(1L, result.getResponseCategory().getProductCategoryId());
        assertEquals("Electronics", result.getResponseCategory().getProductCategoryName());
        assertEquals(1, result.getResponseCategory().getPromotionDTOList().size());

        // Verify product item details
        assertNotNull(result.getResponseProductItemDetails());
        assertEquals(1, result.getResponseProductItemDetails().size());

        ResponseProductItemDetaill itemDetail = result.getResponseProductItemDetails().get(0);
        assertEquals(1L, itemDetail.getProductItemId());
        assertEquals("SKU001", itemDetail.getProductItemSKU());
        assertEquals(10, itemDetail.getProductItemQuantityInStock());
        assertEquals("item_image.jpg", itemDetail.getProductItemImage());
        assertEquals(BigDecimal.valueOf(999.99), itemDetail.getProductItemPrice());

        // Verify variations
        assertEquals(1, itemDetail.getVariations().size());
        assertEquals("Color", itemDetail.getVariations().get(0).getVariationName());
        assertEquals("Red", itemDetail.getVariations().get(0).getOptions());

        verify(productRepository).findProductDetailWithCategoryById(1L);
        verify(productRepository).findProductByProductIdWithStateTrue(1L);
        verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void findProductByIdOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.findProductDetailWithCategoryById(999L)).thenReturn(Collections.emptyList());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.findProductByIdOut(999L);
            });

            verify(productRepository).findProductDetailWithCategoryById(999L);
            verify(productRepository, never()).findProductByProductIdWithStateTrue(anyLong());
        }
    }

    @Test
    @DisplayName("Should throw exception when product entity not found after query")
    void findProductByIdOut_WithNullProductEntity_ShouldThrowException() {
        // Given
        Object[] mockResult = {1L, "iPhone 15", "Latest iPhone model", "iphone15.jpg", 1L, "SKU001", 10, "item_image.jpg", BigDecimal.valueOf(999.99), "Color", "Red"};
        List<Object[]> results = Arrays.asList(new Object[][]{ mockResult });

        when(productRepository.findProductDetailWithCategoryById(1L)).thenReturn(results);
        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.findProductByIdOut(1L);
            });

            verify(productRepository).findProductByProductIdWithStateTrue(1L);
        }
    }

    @Test
    @DisplayName("Should throw exception when category entity is null")
    void findProductByIdOut_WithNullCategoryEntity_ShouldThrowException() {
        // Given
        Object[] mockResult = {1L, "iPhone 15", "Latest iPhone model", "iphone15.jpg", 1L, "SKU001", 10, "item_image.jpg", BigDecimal.valueOf(999.99), "Color", "Red"};
        List<Object[]> results = Arrays.asList(new Object[][]{ mockResult });

        ProductEntity productWithoutCategory = ProductEntity.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productCategoryEntity(null) // No category
                .build();

        when(productRepository.findProductDetailWithCategoryById(1L)).thenReturn(results);
        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productWithoutCategory));

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.findProductByIdOut(1L);
            });
        }
    }

    @Test
    @DisplayName("Should throw exception when category not found in repository")
    void findProductByIdOut_WithCategoryNotFoundInRepo_ShouldThrowException() {
        // Given
        Object[] mockResult = {1L, "iPhone 15", "Latest iPhone model", "iphone15.jpg", 1L, "SKU001", 10, "item_image.jpg", BigDecimal.valueOf(999.99), "Color", "Red"};
        List<Object[]> results = Arrays.asList(new Object[][]{ mockResult });

        when(productRepository.findProductDetailWithCategoryById(1L)).thenReturn(results);
        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.findProductByIdOut(1L);
            });

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
        }
    }

    @Test
    @DisplayName("Should update product successfully with same name")
    void updateProductOut_WithSameName_ShouldReturnUpdatedProductDTO() {
        // Given
        RequestProduct updateRequest = RequestProduct.builder()
                .productName("iPhone 15") // Same name
                .productDescription("Updated description")
                //.productImage("updated_image.jpg")
                .productCategoryId(1L)
                .build();

        ProductEntity updatedEntity = ProductEntity.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Updated description")
                .productImage("updated_image.jpg")
                .productCategoryEntity(categoryEntity)
                .build();

        ProductDTO updatedDTO = ProductDTO.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Updated description")
                .productImage("updated_image.jpg")
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productCategoryRepository.existByProductCategoryIdAndStateTrue(1L)).thenReturn(true);
            when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(updatedEntity);
            when(productMapper.mapProductEntityToDto(updatedEntity)).thenReturn(updatedDTO);

            // When
            ProductDTO result = productAdapter.updateProductOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            assertEquals("iPhone 15", result.getProductName());
            assertEquals("Updated description", result.getProductDescription());
            assertEquals("updated_image.jpg", result.getProductImage());

            verify(productRepository).findProductByProductIdWithStateTrue(1L);
            verify(productCategoryRepository).existByProductCategoryIdAndStateTrue(1L);
            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productRepository).save(any(ProductEntity.class));
            verify(productMapper).mapProductEntityToDto(updatedEntity);
            // Should not check for existing name since it's the same (case insensitive)
            verify(productRepository, never()).existsByProductName(anyString());
        }
    }

    @Test
    @DisplayName("Should update product successfully with different name")
    void updateProductOut_WithDifferentName_ShouldReturnUpdatedProductDTO() {
        // Given
        RequestProduct updateRequest = RequestProduct.builder()
                .productName("iPhone 16") // Different name
                .productDescription("Updated description")
                //.productImage("updated_image.jpg")
                .productCategoryId(1L)
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productRepository.existsByProductName("iPhone 16")).thenReturn(false);
            when(productCategoryRepository.existByProductCategoryIdAndStateTrue(1L)).thenReturn(true);
            when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(productMapper.mapProductEntityToDto(productEntity)).thenReturn(productDTO);

            // When
            ProductDTO result = productAdapter.updateProductOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(productRepository).existsByProductName("iPhone 16");
            verify(productRepository).save(any(ProductEntity.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when updating to existing product name")
    void updateProductOut_WithExistingDifferentName_ShouldThrowException() {
        // Given
        RequestProduct updateRequest = RequestProduct.builder()
                .productName("Samsung Galaxy") // Different existing name
                .productDescription("Updated description")
                //.productImage("updated_image.jpg")
                .productCategoryId(1L)
                .build();

        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
        when(productRepository.existsByProductName("Samsung Galaxy")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.updateProductOut(1L, updateRequest);
            });

            verify(productRepository).existsByProductName("Samsung Galaxy");
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProductOut_WithInvalidProductId_ShouldThrowException() {
        // Given
        when(productRepository.findProductByProductIdWithStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.updateProductOut(999L, requestProduct);
            });

            verify(productRepository).findProductByProductIdWithStateTrue(999L);
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should update product without changing category when categoryId is null")
    void updateProductOut_WithNullCategoryId_ShouldNotUpdateCategory() {
        // Given
        RequestProduct updateRequest = RequestProduct.builder()
                .productName("iPhone 15")
                .productDescription("Updated description")
                //.productImage("updated_image.jpg")
                .productCategoryId(null) // Null category ID
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(productMapper.mapProductEntityToDto(productEntity)).thenReturn(productDTO);

            // When
            ProductDTO result = productAdapter.updateProductOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(productCategoryRepository, never()).existByProductCategoryIdAndStateTrue(anyLong());
            verify(productCategoryRepository, never()).findProductCategoryIdAndStateTrue(anyLong());
        }
    }

    @Test
    @DisplayName("Should throw exception when category not found during update")
    void updateProductOut_WithInvalidCategoryUpdate_ShouldThrowException() {
        // Given
        RequestProduct updateRequest = RequestProduct.builder()
                .productName("iPhone 15")
                .productDescription("Updated description")
                //.productImage("updated_image.jpg")
                .productCategoryId(999L) // Invalid category
                .build();

        when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
        when(productCategoryRepository.existByProductCategoryIdAndStateTrue(999L)).thenReturn(true);
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.updateProductOut(1L, updateRequest);
            });

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(999L);
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should delete product successfully (soft delete)")
    void deleteProductOut_WithValidId_ShouldReturnDeletedProductDTO() {
        // Given
        List<ProductItemEntity> productItems = Arrays.asList(productItemEntity);
        productEntity.setProductItemEntities(productItems);

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productItemRepository.saveAll(anyList())).thenReturn(productItems);
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(productMapper.mapProductEntityToDto(productEntity)).thenReturn(productDTO);

            // When
            ProductDTO result = productAdapter.deleteProductOut(1L);

            // Then
            assertNotNull(result);
            verify(productRepository).findProductByProductIdWithStateTrue(1L);
            verify(productItemRepository).saveAll(anyList());
            verify(productRepository).save(any(ProductEntity.class));
            verify(productMapper).mapProductEntityToDto(productEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProductOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.findProductByProductIdWithStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                productAdapter.deleteProductOut(999L);
            });

            verify(productRepository).findProductByProductIdWithStateTrue(999L);
            verify(productItemRepository, never()).saveAll(anyList());
            verify(productRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when product item deletion fails")
    void deleteProductOut_WithProductItemDeletionFailure_ShouldThrowException() {
        // Given
        List<ProductItemEntity> productItems = Arrays.asList(productItemEntity);
        productEntity.setProductItemEntities(productItems);

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productEntity));
            when(productItemRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

            try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
                validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                        .thenThrow(new RuntimeException("Product deletion failed"));

                // When & Then
                assertThrows(RuntimeException.class, () -> {
                    productAdapter.deleteProductOut(1L);
                });

                verify(productItemRepository).saveAll(anyList());
                verify(productRepository, never()).save(any());
            }
        }
    }

    @Test
    @DisplayName("Should handle empty product items list during deletion")
    void deleteProductOut_WithEmptyProductItems_ShouldCompleteSuccessfully() {
        // Given
        ProductEntity productWithoutItems = ProductEntity.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productItemEntities(Collections.emptyList()) // Empty list
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.findProductByProductIdWithStateTrue(1L)).thenReturn(Optional.of(productWithoutItems));
            when(productItemRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productWithoutItems);
            when(productMapper.mapProductEntityToDto(productWithoutItems)).thenReturn(productDTO);

            // When
            ProductDTO result = productAdapter.deleteProductOut(1L);

            // Then
            assertNotNull(result);
            verify(productItemRepository).saveAll(Collections.emptyList());
            verify(productRepository).save(any(ProductEntity.class));
        }
    }

}