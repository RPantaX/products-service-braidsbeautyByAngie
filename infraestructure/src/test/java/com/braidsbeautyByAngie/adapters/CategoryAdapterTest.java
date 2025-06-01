package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;
import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import com.braidsbeautyByAngie.mapper.ProductCategoryMapper;
import com.braidsbeautyByAngie.mapper.ProductMapper;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
import com.braidsbeautyByAngie.repository.ProductCategoryRepository;
import com.braidsbeautyByAngie.repository.PromotionRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryAdapter Unit Tests")
class CategoryAdapterTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private CategoryAdapter categoryAdapter;

    private ProductCategoryEntity categoryEntity;
    private ProductCategoryDTO categoryDTO;
    private RequestCategory requestCategory;
    private RequestSubCategory requestSubCategory;
    private PromotionEntity promotionEntity;
    private Timestamp currentTimestamp;

    @BeforeEach
    void setUp() {
        currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

        // Setup test entities
        categoryEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(true)
                .createdAt(currentTimestamp)
                .modifiedByUser("testUser")
                .promotionEntities(new HashSet<>())
                .productEntities(new ArrayList<>())
                .subCategories(new ArrayList<>())
                .build();

        categoryDTO = ProductCategoryDTO.builder()
                .categoryId(1L)
                .categoryName("Electronics")
                .build();

        promotionEntity = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .state(true)
                .build();

        requestCategory = RequestCategory.builder()
                .categoryName("Electronics")
                .promotionListId(Arrays.asList(1L, 2L))
                .build();

        requestSubCategory = RequestSubCategory.builder()
                .productSubCategoryName("Smartphones")
                .productCategoryParentId(1L)
                .build();
    }

    @Test
    @DisplayName("Should create category successfully with promotions")
    void createCategoryOut_WithPromotions_ShouldReturnCategoryDTO() {
        // Given
        List<PromotionEntity> promotions = Arrays.asList(promotionEntity);

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productCategoryRepository.existsByProductCategoryName("Electronics")).thenReturn(false);
            when(promotionRepository.findAllByPromotionIdAndStateTrue(anyList())).thenReturn(promotions);
            when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(categoryEntity);
            when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

            // When
            ProductCategoryDTO result = categoryAdapter.createCategoryOut(requestCategory);

            // Then
            assertNotNull(result);
            assertEquals("Electronics", result.getCategoryName());
            assertEquals(1L, result.getCategoryId());

            verify(productCategoryRepository).existsByProductCategoryName("Electronics");
            verify(promotionRepository).findAllByPromotionIdAndStateTrue(requestCategory.getPromotionListId());
            verify(productCategoryRepository).save(any(ProductCategoryEntity.class));
            verify(productCategoryMapper).mapCategoryEntityToDTO(categoryEntity);
        }
    }

    @Test
    @DisplayName("Should create category successfully without promotions")
    void createCategoryOut_WithoutPromotions_ShouldReturnCategoryDTO() {
        // Given
        RequestCategory requestWithoutPromotions = RequestCategory.builder()
                .categoryName("Electronics")
                .promotionListId(Collections.emptyList())
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productCategoryRepository.existsByProductCategoryName("Electronics")).thenReturn(false);
            when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(categoryEntity);
            when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

            // When
            ProductCategoryDTO result = categoryAdapter.createCategoryOut(requestWithoutPromotions);

            // Then
            assertNotNull(result);
            assertEquals("Electronics", result.getCategoryName());

            verify(productCategoryRepository, never()).findAllByPromotionIdAndStateTrue(anyList());
        }
    }

    @Test
    @DisplayName("Should throw exception when category name already exists")
    void createCategoryOut_WithExistingCategoryName_ShouldThrowException() {
        // Given
        when(productCategoryRepository.existsByProductCategoryName("Electronics")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(true), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                categoryAdapter.createCategoryOut(requestCategory);
            });

            verify(productCategoryRepository).existsByProductCategoryName("Electronics");
            verify(productCategoryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should create subcategory successfully")
    void createSubCategoryOut_WithValidData_ShouldReturnCategoryDTO() {
        // Given
        ProductCategoryEntity parentCategory = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(true)
                .build();

        ProductCategoryEntity subCategoryEntity = ProductCategoryEntity.builder()
                .productCategoryId(2L)
                .productCategoryName("Smartphones")
                .parentCategory(parentCategory)
                .state(true)
                .build();

        ProductCategoryDTO subCategoryDTO = ProductCategoryDTO.builder()
                .categoryId(2L)
                .categoryName("Smartphones")
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productCategoryRepository.existsByProductCategoryName("Smartphones")).thenReturn(false);
            when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(parentCategory));
            when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(subCategoryEntity);
            when(productCategoryMapper.mapCategoryEntityToDTO(subCategoryEntity)).thenReturn(subCategoryDTO);

            // When
            ProductCategoryDTO result = categoryAdapter.createSubCategoryOut(requestSubCategory);

            // Then
            assertNotNull(result);
            assertEquals("Smartphones", result.getCategoryName());
            assertEquals(2L, result.getCategoryId());

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productCategoryRepository).save(any(ProductCategoryEntity.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when parent category not found for subcategory")
    void createSubCategoryOut_WithInvalidParentId_ShouldThrowException() {
        // Given
        when(productCategoryRepository.existsByProductCategoryName("Smartphones")).thenReturn(false);
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                categoryAdapter.createSubCategoryOut(requestSubCategory);
            });

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productCategoryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should find category by ID successfully")
    void findCategoryByIdOut_WithValidId_ShouldReturnResponseCategory() {
        // Given
        List<ProductDTO> productDTOList = Arrays.asList(
                ProductDTO.builder().productId(1L).productName("iPhone").build()
        );
        Set<PromotionDTO> promotionDTOList = Set.of(
                PromotionDTO.builder().promotionId(1L).promotionName("Summer Sale").build()
        );

        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
        when(productMapper.mapProductEntityListToDtoList(anyList())).thenReturn(productDTOList);
        when(promotionMapper.mapPromotionListToDtoList(anySet())).thenReturn(new ArrayList<>(promotionDTOList));

        // When
        Optional<ResponseCategory> result = categoryAdapter.findCategoryByIdOut(1L);

        // Then
        assertTrue(result.isPresent());
        ResponseCategory responseCategory = result.get();
        assertEquals(1L, responseCategory.getProductCategoryId());
        assertEquals("Electronics", responseCategory.getProductCategoryName());
        assertNotNull(responseCategory.getProductDTOList());
        assertNotNull(responseCategory.getPromotionDTOList());

        verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
    }

    @Test
    @DisplayName("Should throw exception when category not found by ID")
    void findCategoryByIdOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(productCategoryRepository.findProductCategoryIdAndStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(null), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Category not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                categoryAdapter.findCategoryByIdOut(999L);
            });

            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(999L);
        }
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategoryOut_WithValidData_ShouldReturnUpdatedCategoryDTO() {
        // Given
        RequestCategory updateRequest = RequestCategory.builder()
                .categoryName("Updated Electronics")
                .promotionListId(Arrays.asList(1L))
                .build();

        ProductCategoryEntity updatedEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Updated Electronics")
                .state(true)
                .build();

        ProductCategoryDTO updatedDTO = ProductCategoryDTO.builder()
                .categoryId(1L)
                .categoryName("Updated Electronics")
                .build();

        when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
        when(promotionRepository.findAllByPromotionIdAndStateTrue(Arrays.asList(1L)))
                .thenReturn(Arrays.asList(promotionEntity));
        when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(updatedEntity);
        when(productCategoryMapper.mapCategoryEntityToDTO(updatedEntity)).thenReturn(updatedDTO);

        // When
        ProductCategoryDTO result = categoryAdapter.updateCategoryOut(updateRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Updated Electronics", result.getCategoryName());

        verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
        verify(productCategoryRepository).save(any(ProductCategoryEntity.class));
    }

    @Test
    @DisplayName("Should delete category successfully (soft delete)")
    void deleteCategoryOut_WithValidId_ShouldReturnDeletedCategoryDTO() {
        // Given
        ProductCategoryEntity deletedEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(false) // Soft deleted
                .deletedAt(currentTimestamp)
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productCategoryRepository.findProductCategoryIdAndStateTrue(1L)).thenReturn(Optional.of(categoryEntity));
            when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(deletedEntity);
            when(productCategoryMapper.mapCategoryEntityToDTO(deletedEntity)).thenReturn(categoryDTO);

            // When
            ProductCategoryDTO result = categoryAdapter.deleteCategoryOut(1L);

            // Then
            assertNotNull(result);
            verify(productCategoryRepository).findProductCategoryIdAndStateTrue(1L);
            verify(productCategoryRepository).save(any(ProductCategoryEntity.class));
        }
    }

    @Test
    @DisplayName("Should list categories with pagination successfully")
    void listCategoryPageableOut_WithValidParameters_ShouldReturnPageableResponse() {
        // Given
        List<ProductCategoryEntity> categoryList = Arrays.asList(categoryEntity);
        Page<ProductCategoryEntity> categoryPage = new PageImpl<>(categoryList, PageRequest.of(0, 10), 1);

        List<ProductDTO> productDTOList = Arrays.asList(
                ProductDTO.builder().productId(1L).productName("iPhone").build()
        );
        Set<PromotionDTO> promotionDTOList = new HashSet<>();

        when(productCategoryRepository.findAllCategoriesPageableAndStatusTrue(any(Pageable.class)))
                .thenReturn(categoryPage);
        when(productMapper.mapProductEntityListToDtoList(anyList())).thenReturn(productDTOList);
        when(promotionMapper.mapPromotionListToDtoList(anySet())).thenReturn(new ArrayList<>(promotionDTOList));

        // When
        ResponseListPageableCategory result = categoryAdapter.listCategoryPageableOut(0, 10, "productCategoryName", "ASC");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.isEnd());
        assertNotNull(result.getResponseCategoryList());
        assertEquals(1, result.getResponseCategoryList().size());

        verify(productCategoryRepository).findAllCategoriesPageableAndStatusTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("Should list categories with descending sort order")
    void listCategoryPageableOut_WithDescendingSort_ShouldReturnSortedResponse() {
        // Given
        List<ProductCategoryEntity> categoryList = Arrays.asList(categoryEntity);
        Page<ProductCategoryEntity> categoryPage = new PageImpl<>(categoryList,
                PageRequest.of(0, 10, Sort.by("productCategoryName").descending()), 1);

        when(productCategoryRepository.findAllCategoriesPageableAndStatusTrue(any(Pageable.class)))
                .thenReturn(categoryPage);
        when(productMapper.mapProductEntityListToDtoList(anyList())).thenReturn(new ArrayList<>());
        when(promotionMapper.mapPromotionListToDtoList(anySet())).thenReturn(new ArrayList<>());

        // When
        ResponseListPageableCategory result = categoryAdapter.listCategoryPageableOut(0, 10, "productCategoryName", "DESC");

        // Then
        assertNotNull(result);
        verify(productCategoryRepository).findAllCategoriesPageableAndStatusTrue(
                argThat(pageable -> pageable.getSort().getOrderFor("productCategoryName").getDirection() == Sort.Direction.DESC)
        );
    }

    @Test
    @DisplayName("Should list all categories without pagination")
    void listCategoryOut_ShouldReturnAllCategories() {
        // Given
        List<ProductCategoryEntity> allCategories = Arrays.asList(categoryEntity);

        when(productCategoryRepository.findAll()).thenReturn(allCategories);
        when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

        // When
        List<ProductCategoryDTO> result = categoryAdapter.listCategoryOut();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategoryName());

        verify(productCategoryRepository).findAll();
        verify(productCategoryMapper).mapCategoryEntityToDTO(categoryEntity);
    }

    @Test
    @DisplayName("Should handle empty promotion list when creating category")
    void createCategoryOut_WithEmptyPromotionList_ShouldCreateCategoryWithoutPromotions() {
        // Given
        RequestCategory requestWithEmptyPromotions = RequestCategory.builder()
                .categoryName("Electronics")
                .promotionListId(Collections.emptyList())
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(productCategoryRepository.existsByProductCategoryName("Electronics")).thenReturn(false);
            when(productCategoryRepository.save(any(ProductCategoryEntity.class))).thenReturn(categoryEntity);
            when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

            // When
            ProductCategoryDTO result = categoryAdapter.createCategoryOut(requestWithEmptyPromotions);

            // Then
            assertNotNull(result);
            verify(promotionRepository, never()).findAllByPromotionIdAndStateTrue(anyList());
        }
    }

    @Test
    @DisplayName("Should handle empty categories list in pagination")
    void listCategoryPageableOut_WithEmptyResult_ShouldReturnEmptyPageableResponse() {
        // Given
        List<ProductCategoryEntity> emptyList = Collections.emptyList();
        Page<ProductCategoryEntity> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);

        when(productCategoryRepository.findAllCategoriesPageableAndStatusTrue(any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseListPageableCategory result = categoryAdapter.listCategoryPageableOut(0, 10, "productCategoryName", "ASC");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEnd());
        assertTrue(result.getResponseCategoryList().isEmpty());
    }
}