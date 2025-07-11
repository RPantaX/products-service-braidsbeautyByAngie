package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.constants.ProductsErrorEnum;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestVariationName;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.ProductItemMapper;
import com.braidsbeautyByAngie.repository.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
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
@DisplayName("ItemProductAdapter Unit Tests")
class ItemProductAdapterTest {

    @Mock
    private ProductItemMapper productItemMapper;

    @Mock
    private ProductItemRepository productItemRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VariationRepository variationRepository;

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @InjectMocks
    private ItemProductAdapter itemProductAdapter;

    private ProductItemEntity productItemEntity;
    private ProductItemDTO productItemDTO;
    private RequestItemProduct requestItemProduct;
    private ProductEntity productEntity;
    private ProductCategoryEntity categoryEntity;
    private VariationEntity variationEntity;
    private VariationOptionEntity variationOptionEntity;
    private RequestVariationName requestVariationName;
    private Timestamp currentTimestamp;

    @BeforeEach
    void setUp() {
        currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

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

        // Setup category entity
        categoryEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(true)
                .promotionEntities(new HashSet<>())
                .build();

        // Setup product entity
        productEntity = ProductEntity.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productCategoryEntity(categoryEntity)
                .state(true)
                .build();

        // Setup product item entity
        productItemEntity = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemSKU("SKU001")
                .productItemPrice(BigDecimal.valueOf(999.99))
                .productItemQuantityInStock(10)
                .productItemImage("image.jpg")
                .productEntity(productEntity)
                .variationOptionEntitySet(Set.of(variationOptionEntity))
                .state(true)
                .createdAt(currentTimestamp)
                .modifiedByUser("testUser")
                .build();

        // Setup DTOs
        productItemDTO = ProductItemDTO.builder()
                .productItemId(1L)
                .productItemSKU("SKU001")
                .productItemPrice(BigDecimal.valueOf(999.99))
                .productItemQuantityInStock(10)
                .productItemImage("image.jpg")
                .build();

        // Setup request objects
        requestVariationName = RequestVariationName.builder()
                .variationName("Color")
                .variationOptionValue("Red")
                .build();

        requestItemProduct = RequestItemProduct.builder()
                .productId(1L)
                .productItemSKU("SKU001")
                .productItemPrice(BigDecimal.valueOf(999.99))
                .productItemQuantityInStock(10)
                //.productItemImage("image.jpg")
                .requestVariations(Arrays.asList(requestVariationName))
                .build();
    }

    @Test
    @DisplayName("Should create item product successfully with variations")
    void createItemProductOut_WithValidData_ShouldReturnProductItemDTO() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productItemRepository.existsByProductItemSKU("SKU001")).thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
            when(variationRepository.findByVariationName("Color")).thenReturn(Optional.of(variationEntity));
            when(variationOptionRepository.existsByVariationOptionValue("Red")).thenReturn(false);
            when(variationOptionRepository.save(any(VariationOptionEntity.class))).thenReturn(variationOptionEntity);
            when(productItemRepository.save(any(ProductItemEntity.class))).thenReturn(productItemEntity);
            when(productItemMapper.mapProductItemEntityToDto(productItemEntity)).thenReturn(productItemDTO);

            // When
            ProductItemDTO result = itemProductAdapter.createItemProductOut(requestItemProduct);

            // Then
            assertNotNull(result);
            assertEquals("SKU001", result.getProductItemSKU());
            assertEquals(BigDecimal.valueOf(999.99), result.getProductItemPrice());
            assertEquals(10, result.getProductItemQuantityInStock());

            verify(productItemRepository).existsByProductItemSKU("SKU001");
            verify(productRepository).findById(1L);
            verify(variationRepository).findByVariationName("Color");
            verify(productItemRepository).save(any(ProductItemEntity.class));
            verify(productItemMapper).mapProductItemEntityToDto(productItemEntity);
        }
    }

    @Test
    @DisplayName("Should update item product without variations successfully")
    void updateItemProductOut_WithoutVariations_ShouldReturnUpdatedProductItemDTO() {
        // Given
        RequestItemProduct updateRequestWithoutVariations = RequestItemProduct.builder()
                .productItemSKU("SKU004")
                .productItemPrice(BigDecimal.valueOf(899.99))
                .productItemQuantityInStock(8)
                //.productItemImage("updated_image.jpg")
                .requestVariations(Collections.emptyList())
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.existsById(1L)).thenReturn(true);
            when(productItemRepository.save(any(ProductItemEntity.class))).thenReturn(productItemEntity);
            when(productItemMapper.mapProductItemEntityToDto(productItemEntity)).thenReturn(productItemDTO);

            // When
            ProductItemDTO result = itemProductAdapter.updateItemProductOut(1L, updateRequestWithoutVariations);

            // Then
            assertNotNull(result);
            verify(productRepository).existsById(1L);
            verify(productItemRepository).save(any(ProductItemEntity.class));
            verify(productItemMapper).mapProductItemEntityToDto(productItemEntity);
            // Should not interact with variation repositories when no variations provided
            verify(variationRepository, never()).findByVariationName(anyString());
            verify(variationOptionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle multiple promotions correctly in reserve product")
    void reserveProductOut_WithMultiplePromotions_ShouldApplyAllDiscounts() {
        // Given
        Product desiredProduct = Product.builder()
                .productId(1L)
                .quantity(1)
                .build();

        List<Product> desiredProducts = Arrays.asList(desiredProduct);

        PromotionEntity promotion1 = PromotionEntity.builder()
                .promotionId(1L)
                .promotionDiscountRate(BigDecimal.valueOf(0.10)) // 10% discount
                .build();

        PromotionEntity promotion2 = PromotionEntity.builder()
                .promotionId(2L)
                .promotionDiscountRate(BigDecimal.valueOf(0.05)) // 5% discount
                .build();

        categoryEntity.setPromotionEntities(Set.of(promotion1, promotion2));

        when(productItemRepository.findByProductItemIdAndStateTrue(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(productItemEntity));

        // When
        List<Product> result = itemProductAdapter.reserveProductOut(1L, desiredProducts);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Product resultProduct = result.get(0);

        // Total discount should be 15% (0.10 + 0.05)
        // Price should be 999.99 - (999.99 * 0.15) = 849.9915
        BigDecimal expectedPrice = BigDecimal.valueOf(999.99).subtract(
                BigDecimal.valueOf(999.99).multiply(BigDecimal.valueOf(0.15))
        );
        assertEquals(expectedPrice, resultProduct.getPrice());
    }

    @Test
    @DisplayName("Should handle stock update correctly during reservation")
    void reserveProductOut_ShouldUpdateStockCorrectly() {
        // Given
        Product desiredProduct = Product.builder()
                .productId(1L)
                .quantity(3)
                .build();

        List<Product> desiredProducts = Arrays.asList(desiredProduct);

        when(productItemRepository.findByProductItemIdAndStateTrue(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProductItemEntity> entities = invocation.getArgument(0);
            // Verify that stock was reduced correctly
            ProductItemEntity entity = entities.get(0);
            assertEquals(7, entity.getProductItemQuantityInStock()); // 10 - 3 = 7
            return entities;
        });

        // When
        List<Product> result = itemProductAdapter.reserveProductOut(1L, desiredProducts);

        // Then
        assertNotNull(result);
        verify(productItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle stock restoration correctly during cancellation")
    void cancelProductReservationOut_ShouldRestoreStockCorrectly() {
        // Given
        Product productToCancel = Product.builder()
                .productId(1L)
                .quantity(3)
                .build();

        List<Product> productsToCancel = Arrays.asList(productToCancel);

        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProductItemEntity> entities = invocation.getArgument(0);
            // Verify that stock was increased correctly
            ProductItemEntity entity = entities.get(0);
            assertEquals(13, entity.getProductItemQuantityInStock()); // 10 + 3 = 13
            return entities;
        });

        // When
        itemProductAdapter.cancelProductReservationOut(1L, productsToCancel);

        // Then
        verify(productItemRepository).saveAll(anyList());
    }
    @Test
    @DisplayName("Should validate product item exists before deletion")
    void deleteItemProductOut_WhenProductItemNotExists_ShouldThrowException() {
        // Given
        when(productItemRepository.existsById(999L)).thenReturn(false);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product item not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.deleteItemProductOut(999L);
            });

            verify(productItemRepository).existsById(999L);
            verify(productItemRepository, never()).findById(anyLong());
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should handle category validation in product item detail building")
    void buildProductItemDetail_WhenCategoryNotFound_ShouldThrowException() {
        // Given
        Object[] mockResult = {1L, "SKU001", 10, "image.jpg", BigDecimal.valueOf(999.99), "Color", "Red"};
        List<Object[]> results = Arrays.asList(new Object[][]{ mockResult });

        when(productItemRepository.findProductItemWithVariations(1L)).thenReturn(results);
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.findItemProductByIdOut(1L);
            });

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
        }
    }

    @Test
    @DisplayName("Should handle empty promotion set correctly")
    void reserveProductOut_WithEmptyPromotions_ShouldReturnOriginalPrice() {
        // Given
        Product desiredProduct = Product.builder()
                .productId(1L)
                .quantity(1)
                .build();

        List<Product> desiredProducts = Arrays.asList(desiredProduct);

        // Set empty promotions
        categoryEntity.setPromotionEntities(Collections.emptySet());

        when(productItemRepository.findByProductItemIdAndStateTrue(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(productItemEntity));

        // When
        List<Product> result = itemProductAdapter.reserveProductOut(1L, desiredProducts);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Product resultProduct = result.get(0);

        // Price should remain original since no promotions
        assertEquals(BigDecimal.valueOf(999.99), resultProduct.getPrice());
    }

    @Test
    @DisplayName("Should throw exception when product item SKU already exists")
    void createItemProductOut_WithExistingSKU_ShouldThrowException() {
        // Given
        when(productItemRepository.existsByProductItemSKU("SKU001")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Item product already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.createItemProductOut(requestItemProduct);
            });

            verify(productItemRepository).existsByProductItemSKU("SKU001");
            verify(productRepository, never()).findById(anyLong());
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void createItemProductOut_WithInvalidProductId_ShouldThrowException() {
        // Given
        when(productItemRepository.existsByProductItemSKU("SKU001")).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.createItemProductOut(requestItemProduct);
            });

            verify(productRepository).findById(1L);
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when variation not found")
    void createItemProductOut_WithInvalidVariation_ShouldThrowException() {
        // Given
        when(productItemRepository.existsByProductItemSKU("SKU001")).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(variationRepository.findByVariationName("Color")).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Variation not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.createItemProductOut(requestItemProduct);
            });

            verify(variationRepository).findByVariationName("Color");
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should create item product with existing variation option")
    void createItemProductOut_WithExistingVariationOption_ShouldUseExistingOption() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productItemRepository.existsByProductItemSKU("SKU001")).thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
            when(variationRepository.findByVariationName("Color")).thenReturn(Optional.of(variationEntity));
            when(variationOptionRepository.existsByVariationOptionValue("Red")).thenReturn(true);
            when(variationOptionRepository.findByVariationOptionValue("Red")).thenReturn(Optional.of(variationOptionEntity));
            when(productItemRepository.save(any(ProductItemEntity.class))).thenReturn(productItemEntity);
            when(productItemMapper.mapProductItemEntityToDto(productItemEntity)).thenReturn(productItemDTO);

            // When
            ProductItemDTO result = itemProductAdapter.createItemProductOut(requestItemProduct);

            // Then
            assertNotNull(result);
            verify(variationOptionRepository, never()).save(any(VariationOptionEntity.class));
            verify(variationOptionRepository).findByVariationOptionValue("Red");
        }
    }


    @Test
    @DisplayName("Should throw exception when product item not found by ID")
    void findItemProductByIdOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productItemRepository.findProductItemWithVariations(999L)).thenReturn(Collections.emptyList());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product item not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.findItemProductByIdOut(999L);
            });

            verify(productItemRepository).findProductItemWithVariations(999L);
        }
    }

    @Test
    @DisplayName("Should update item product successfully")
    void updateItemProductOut_WithValidData_ShouldReturnUpdatedProductItemDTO() {
        // Given
        RequestItemProduct updateRequest = RequestItemProduct.builder()
                .productItemSKU("SKU002")
                .productItemPrice(BigDecimal.valueOf(1099.99))
                .productItemQuantityInStock(15)
                //.productItemImage("new_image.jpg")
                .requestVariations(Arrays.asList(requestVariationName))
                .build();

        ProductItemEntity updatedEntity = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemSKU("SKU002")
                .productItemPrice(BigDecimal.valueOf(1099.99))
                .productItemQuantityInStock(15)
                .productItemImage("new_image.jpg")
                .build();

        ProductItemDTO updatedDTO = ProductItemDTO.builder()
                .productItemId(1L)
                .productItemSKU("SKU002")
                .productItemPrice(BigDecimal.valueOf(1099.99))
                .productItemQuantityInStock(15)
                .productItemImage("new_image.jpg")
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productRepository.existsById(1L)).thenReturn(true);
            when(variationRepository.findByVariationName("Color")).thenReturn(Optional.of(variationEntity));
            when(variationOptionRepository.existsByVariationOptionValue("Red")).thenReturn(true);
            when(variationOptionRepository.findByVariationOptionValue("Red")).thenReturn(Optional.of(variationOptionEntity));
            when(productItemRepository.save(any(ProductItemEntity.class))).thenReturn(updatedEntity);
            when(productItemMapper.mapProductItemEntityToDto(updatedEntity)).thenReturn(updatedDTO);

            // When
            ProductItemDTO result = itemProductAdapter.updateItemProductOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            assertEquals("SKU002", result.getProductItemSKU());
            assertEquals(BigDecimal.valueOf(1099.99), result.getProductItemPrice());
            assertEquals(15, result.getProductItemQuantityInStock());

            verify(productRepository).existsById(1L);
            verify(productItemRepository).save(any(ProductItemEntity.class));
            verify(productItemMapper).mapProductItemEntityToDto(updatedEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product item")
    void updateItemProductOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product item not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.updateItemProductOut(999L, requestItemProduct);
            });

            verify(productRepository).existsById(999L);
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should delete item product successfully (soft delete)")
    void deleteItemProductOut_WithValidId_ShouldReturnDeletedProductItemDTO() {
        // Given
        ProductItemEntity deletedEntity = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemSKU("SKU001")
                .state(false) // Soft deleted
                .deletedAt(currentTimestamp)
                .productEntity(null) // Set to null in delete operation
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productItemRepository.existsById(1L)).thenReturn(true);
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
            when(productItemRepository.save(any(ProductItemEntity.class))).thenReturn(deletedEntity);
            when(productItemMapper.mapProductItemEntityToDto(deletedEntity)).thenReturn(productItemDTO);

            // When
            ProductItemDTO result = itemProductAdapter.deleteItemProductOut(1L);

            // Then
            assertNotNull(result);
            verify(productItemRepository).findById(1L);
            verify(productItemRepository).save(any(ProductItemEntity.class));
            verify(productItemMapper).mapProductItemEntityToDto(deletedEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product item")
    void deleteItemProductOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productItemRepository.existsById(999L)).thenReturn(false);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Product item not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.deleteItemProductOut(999L);
            });

            verify(productItemRepository).existsById(999L);
            verify(productItemRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should reserve products successfully without promotions")
    void reserveProductOut_WithValidProducts_ShouldReturnProductsWithPrices() {
        // Given
        Product desiredProduct = Product.builder()
                .productId(1L)
                .quantity(2)
                .build();

        List<Product> desiredProducts = Arrays.asList(desiredProduct);

        ProductItemEntity updatedProductItem = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemQuantityInStock(8) // 10 - 2
                .productItemPrice(BigDecimal.valueOf(999.99))
                .productEntity(productEntity)
                .build();

        when(productItemRepository.findByProductItemIdAndStateTrue(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(updatedProductItem));

        // When
        List<Product> result = itemProductAdapter.reserveProductOut(1L, desiredProducts);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Product resultProduct = result.get(0);
        assertEquals(1L, resultProduct.getProductId());
        assertEquals(2, resultProduct.getQuantity());
        assertEquals(BigDecimal.valueOf(999.99), resultProduct.getPrice());
        assertEquals("iPhone 15", resultProduct.getProductName());

        verify(productItemRepository).findByProductItemIdAndStateTrue(1L);
        verify(productItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reserve products successfully with promotions")
    void reserveProductOut_WithPromotions_ShouldReturnDiscountedPrices() {
        // Given
        Product desiredProduct = Product.builder()
                .productId(1L)
                .quantity(1)
                .build();

        List<Product> desiredProducts = Arrays.asList(desiredProduct);

        PromotionEntity promotion = PromotionEntity.builder()
                .promotionId(1L)
                .promotionDiscountRate(BigDecimal.valueOf(0.10)) // 10% discount
                .build();

        categoryEntity.setPromotionEntities(Set.of(promotion));

        when(productItemRepository.findByProductItemIdAndStateTrue(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(productItemEntity));

        // When
        List<Product> result = itemProductAdapter.reserveProductOut(1L, desiredProducts);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Product resultProduct = result.get(0);
        assertEquals(1L, resultProduct.getProductId());
        assertEquals(1, resultProduct.getQuantity());
        // Price should be 999.99 - (999.99 * 0.10) = 899.991
        assertEquals(BigDecimal.valueOf(899.991), resultProduct.getPrice());
        assertEquals("iPhone 15", resultProduct.getProductName());
    }

    @Test
    @DisplayName("Should cancel product reservation successfully")
    void cancelProductReservationOut_WithValidProducts_ShouldRestoreStock() {
        // Given
        Product productToCancel = Product.builder()
                .productId(1L)
                .quantity(2)
                .build();

        List<Product> productsToCancel = Arrays.asList(productToCancel);

        ProductItemEntity updatedProductItem = ProductItemEntity.builder()
                .productItemId(1L)
                .productItemQuantityInStock(12) // 10 + 2
                .build();

        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItemEntity));
        when(productItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(updatedProductItem));

        // When
        itemProductAdapter.cancelProductReservationOut(1L, productsToCancel);

        // Then
        verify(productItemRepository).findById(1L);
        verify(productItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when listing with empty IDs list")
    void listItemProductsByIdsOut_WithEmptyIds_ShouldThrowException() {
        // Given
        List<Long> emptyIds = Collections.emptyList();

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("No product item IDs provided"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.listItemProductsByIdsOut(emptyIds);
            });

            verify(productItemRepository, never()).findProductItemsWithVariations(anyList());
        }
    }

    @Test
    @DisplayName("Should throw exception when listing with null IDs list")
    void listItemProductsByIdsOut_WithNullIds_ShouldThrowException() {
        // Given
        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("No product item IDs provided"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.listItemProductsByIdsOut(null);
            });

            verify(productItemRepository, never()).findProductItemsWithVariations(anyList());
        }
    }

    @Test
    @DisplayName("Should throw exception when no products found for provided IDs")
    void listItemProductsByIdsOut_WithNoResults_ShouldThrowException() {
        // Given
        List<Long> itemProductIds = Arrays.asList(999L, 998L);

        when(productItemRepository.findProductItemsWithVariations(itemProductIds)).thenReturn(Collections.emptyList());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("No product items found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                itemProductAdapter.listItemProductsByIdsOut(itemProductIds);
            });

            verify(productItemRepository).findProductItemsWithVariations(itemProductIds);
        }
    }

}