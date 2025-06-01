package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;
import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import com.braidsbeautyByAngie.mapper.ProductCategoryMapper;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionAdapter Unit Tests")
class PromotionAdapterTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @InjectMocks
    private PromotionAdapter promotionAdapter;

    private PromotionEntity promotionEntity;
    private PromotionDTO promotionDTO;
    private RequestPromotion requestPromotion;
    private ProductCategoryEntity categoryEntity;
    private ProductCategoryDTO categoryDTO;
    private Timestamp currentTimestamp;
    private Timestamp startDate;
    private Timestamp endDate;

    @BeforeEach
    void setUp() {
        currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        startDate = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        endDate = Timestamp.valueOf(LocalDateTime.now().plusDays(30));

        // Setup category entity
        categoryEntity = ProductCategoryEntity.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .state(true)
                .build();

        // Setup category DTO
        categoryDTO = ProductCategoryDTO.builder()
                .categoryId(1L)
                .categoryName("Electronics")
                .build();

        // Setup promotion entity
        promotionEntity = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDescription("Great summer discounts")
                .promotionDiscountRate(BigDecimal.valueOf(0.15))
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .productCategoryEntities(Set.of(categoryEntity))
                .state(true)
                .createdAt(currentTimestamp)
                .modifiedByUser("testUser")
                .build();

        // Setup promotion DTO
        promotionDTO = PromotionDTO.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDescription("Great summer discounts")
                .promotionDiscountRate(0.15)
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .build();

        // Setup request
        requestPromotion = RequestPromotion.builder()
                .promotionName("Summer Sale")
                .promotionDescription("Great summer discounts")
                .promotionDiscountRate(BigDecimal.valueOf(0.15))
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .build();
    }

    @Test
    @DisplayName("Should create promotion successfully when name does not exist")
    void createPromotionOut_WithValidData_ShouldReturnPromotionDTO() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.existsByPromotionName("Summer Sale")).thenReturn(false);
            when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(promotionEntity);
            when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);

            // When
            PromotionDTO result = promotionAdapter.createPromotionOut(requestPromotion);

            // Then
            assertNotNull(result);
            assertEquals("Summer Sale", result.getPromotionName());
            assertEquals("Great summer discounts", result.getPromotionDescription());
            assertEquals(0.15, result.getPromotionDiscountRate());
            assertEquals(startDate, result.getPromotionStartDate());
            assertEquals(endDate, result.getPromotionEndDate());
            assertEquals(1L, result.getPromotionId());

            verify(promotionRepository).existsByPromotionName("Summer Sale");
            verify(promotionRepository).save(any(PromotionEntity.class));
            verify(promotionMapper).mapPromotionEntityToDto(promotionEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when promotion name already exists")
    void createPromotionOut_WithExistingPromotionName_ShouldThrowException() {
        // Given
        when(promotionRepository.existsByPromotionName("Summer Sale")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(true), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Promotion already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                promotionAdapter.createPromotionOut(requestPromotion);
            });

            verify(promotionRepository).existsByPromotionName("Summer Sale");
            verify(promotionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should find promotion by ID successfully")
    void findPromotionByIdOut_WithValidId_ShouldReturnResponsePromotion() {
        // Given
        when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionEntity));
        when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);
        when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

        // When
        Optional<ResponsePromotion> result = promotionAdapter.findPromotionByIdOut(1L);

        // Then
        assertTrue(result.isPresent());
        ResponsePromotion responsePromotion = result.get();

        assertNotNull(responsePromotion.getPromotionDTO());
        assertEquals("Summer Sale", responsePromotion.getPromotionDTO().getPromotionName());
        assertEquals("Great summer discounts", responsePromotion.getPromotionDTO().getPromotionDescription());

        assertNotNull(responsePromotion.getCategoryDTOList());
        assertEquals(1, responsePromotion.getCategoryDTOList().size());
        assertEquals("Electronics", responsePromotion.getCategoryDTOList().get(0).getCategoryName());

        verify(promotionRepository).findPromotionByIdWithStateTrue(1L);
        verify(promotionMapper).mapPromotionEntityToDto(promotionEntity);
        verify(productCategoryMapper).mapCategoryEntityToDTO(categoryEntity);
    }

    @Test
    @DisplayName("Should throw exception when promotion not found by ID")
    void findPromotionByIdOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(promotionRepository.findPromotionByIdWithStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Promotion not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                promotionAdapter.findPromotionByIdOut(999L);
            });

            verify(promotionRepository).findPromotionByIdWithStateTrue(999L);
        }
    }

    @Test
    @DisplayName("Should find promotion with empty categories list")
    void findPromotionByIdOut_WithEmptyCategories_ShouldReturnPromotionWithEmptyList() {
        // Given
        PromotionEntity promotionWithoutCategories = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDescription("Great summer discounts")
                .promotionDiscountRate(BigDecimal.valueOf(0.15))
                .productCategoryEntities(Collections.emptySet()) // Empty categories
                .state(true)
                .build();

        when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionWithoutCategories));
        when(promotionMapper.mapPromotionEntityToDto(promotionWithoutCategories)).thenReturn(promotionDTO);

        // When
        Optional<ResponsePromotion> result = promotionAdapter.findPromotionByIdOut(1L);

        // Then
        assertTrue(result.isPresent());
        ResponsePromotion responsePromotion = result.get();

        assertNotNull(responsePromotion.getPromotionDTO());
        assertNotNull(responsePromotion.getCategoryDTOList());
        assertTrue(responsePromotion.getCategoryDTOList().isEmpty());

        verify(productCategoryMapper, never()).mapCategoryEntityToDTO(any());
    }

    @Test
    @DisplayName("Should update promotion successfully")
    void updatePromotionOut_WithValidData_ShouldReturnUpdatedPromotionDTO() {
        // Given
        RequestPromotion updateRequest = RequestPromotion.builder()
                .promotionName("Winter Sale")
                .promotionDescription("Amazing winter discounts")
                .promotionDiscountRate(BigDecimal.valueOf(0.20))
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .build();

        PromotionEntity updatedEntity = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Winter Sale")
                .promotionDescription("Amazing winter discounts")
                .promotionDiscountRate(BigDecimal.valueOf(0.20))
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .productCategoryEntities(new HashSet<>())
                .state(true)
                .build();

        PromotionDTO updatedDTO = PromotionDTO.builder()
                .promotionId(1L)
                .promotionName("Winter Sale")
                .promotionDescription("Amazing winter discounts")
                .promotionDiscountRate(0.20)
                .promotionStartDate(startDate)
                .promotionEndDate(endDate)
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionEntity));
            when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(updatedEntity);
            when(promotionMapper.mapPromotionEntityToDto(updatedEntity)).thenReturn(updatedDTO);

            // When
            PromotionDTO result = promotionAdapter.updatePromotionOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            assertEquals("Winter Sale", result.getPromotionName());
            assertEquals("Amazing winter discounts", result.getPromotionDescription());
            assertEquals(0.20, result.getPromotionDiscountRate());

            verify(promotionRepository).findPromotionByIdWithStateTrue(1L);
            verify(promotionRepository).save(any(PromotionEntity.class));
            verify(promotionMapper).mapPromotionEntityToDto(updatedEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent promotion")
    void updatePromotionOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(promotionRepository.findPromotionByIdWithStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Promotion not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                promotionAdapter.updatePromotionOut(999L, requestPromotion);
            });

            verify(promotionRepository).findPromotionByIdWithStateTrue(999L);
            verify(promotionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should delete promotion successfully (soft delete)")
    void deletePromotionOut_WithValidId_ShouldReturnDeletedPromotionDTO() {
        // Given
        PromotionEntity deletedEntity = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .state(false) // Soft deleted
                .deletedAt(currentTimestamp)
                .productCategoryEntities(new HashSet<>()) // Categories cleared
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionEntity));
            when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(deletedEntity);
            when(promotionMapper.mapPromotionEntityToDto(deletedEntity)).thenReturn(promotionDTO);

            // When
            PromotionDTO result = promotionAdapter.deletePromotionOut(1L);

            // Then
            assertNotNull(result);
            verify(promotionRepository).findPromotionByIdWithStateTrue(1L);
            verify(promotionRepository).save(any(PromotionEntity.class));
            verify(promotionMapper).mapPromotionEntityToDto(deletedEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent promotion")
    void deletePromotionOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(promotionRepository.findPromotionByIdWithStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(GlobalErrorEnum.class)))
                    .thenThrow(new RuntimeException("Promotion not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                promotionAdapter.deletePromotionOut(999L);
            });

            verify(promotionRepository).findPromotionByIdWithStateTrue(999L);
            verify(promotionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should list promotions with pagination successfully")
    void listPromotionByPageOut_WithValidParameters_ShouldReturnPageableResponse() {
        // Given
        List<PromotionEntity> promotionList = Arrays.asList(promotionEntity);
        Page<PromotionEntity> promotionPage = new PageImpl<>(promotionList, PageRequest.of(0, 10), 1);

        when(promotionRepository.findAllByStateTrueAmdPageable(any(Pageable.class))).thenReturn(promotionPage);
        when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);
        when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

        // When
        ResponseListPageablePromotion result = promotionAdapter.listPromotionByPageOut(0, 10, "promotionName", "ASC");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.isEnd());
        assertNotNull(result.getResponsePromotionList());
        assertEquals(1, result.getResponsePromotionList().size());

        ResponsePromotion responsePromotion = result.getResponsePromotionList().get(0);
        assertEquals("Summer Sale", responsePromotion.getPromotionDTO().getPromotionName());
        assertEquals(1, responsePromotion.getCategoryDTOList().size());

        verify(promotionRepository).findAllByStateTrueAmdPageable(any(Pageable.class));
    }

    @Test
    @DisplayName("Should list promotions with descending sort order")
    void listPromotionByPageOut_WithDescendingSort_ShouldReturnSortedResponse() {
        // Given
        List<PromotionEntity> promotionList = Arrays.asList(promotionEntity);
        Page<PromotionEntity> promotionPage = new PageImpl<>(promotionList,
                PageRequest.of(0, 10, Sort.by("promotionName").descending()), 1);

        when(promotionRepository.findAllByStateTrueAmdPageable(any(Pageable.class))).thenReturn(promotionPage);
        when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);
        when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);

        // When
        ResponseListPageablePromotion result = promotionAdapter.listPromotionByPageOut(0, 10, "promotionName", "DESC");

        // Then
        assertNotNull(result);
        verify(promotionRepository).findAllByStateTrueAmdPageable(
                argThat(pageable -> pageable.getSort().getOrderFor("promotionName").getDirection() == Sort.Direction.DESC)
        );
    }

    @Test
    @DisplayName("Should return null when no promotions found in pagination")
    void listPromotionByPageOut_WithEmptyResult_ShouldReturnNull() {
        // Given
        List<PromotionEntity> emptyList = Collections.emptyList();
        Page<PromotionEntity> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);

        when(promotionRepository.findAllByStateTrueAmdPageable(any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseListPageablePromotion result = promotionAdapter.listPromotionByPageOut(0, 10, "promotionName", "ASC");

        // Then
        assertNull(result); // According to the code, it returns null when empty
        verify(promotionRepository).findAllByStateTrueAmdPageable(any(Pageable.class));
    }

    @Test
    @DisplayName("Should list all promotions without pagination")
    void listPromotionOut_ShouldReturnAllPromotions() {
        // Given
        List<PromotionEntity> allPromotions = Arrays.asList(promotionEntity);
        List<PromotionDTO> promotionDTOList = Arrays.asList(promotionDTO);

        when(promotionRepository.findAllByStateTrue()).thenReturn(allPromotions);
        when(promotionMapper.mapPromotionEntityToDto(promotionEntity)).thenReturn(promotionDTO);

        // When
        List<PromotionDTO> result = promotionAdapter.listPromotionOut();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Summer Sale", result.get(0).getPromotionName());
        assertEquals("Great summer discounts", result.get(0).getPromotionDescription());

        verify(promotionRepository).findAllByStateTrue();
        verify(promotionMapper).mapPromotionEntityToDto(promotionEntity);
    }

    @Test
    @DisplayName("Should return empty list when no promotions exist")
    void listPromotionOut_WithNoPromotions_ShouldReturnEmptyList() {
        // Given
        when(promotionRepository.findAllByStateTrue()).thenReturn(Collections.emptyList());

        // When
        List<PromotionDTO> result = promotionAdapter.listPromotionOut();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(promotionRepository).findAllByStateTrue();
        verify(promotionMapper, never()).mapPromotionEntityToDto(any());
    }

    @Test
    @DisplayName("Should handle promotion with multiple categories")
    void findPromotionByIdOut_WithMultipleCategories_ShouldReturnAllCategories() {
        // Given
        ProductCategoryEntity category2 = ProductCategoryEntity.builder()
                .productCategoryId(2L)
                .productCategoryName("Books")
                .state(true)
                .build();

        ProductCategoryDTO categoryDTO2 = ProductCategoryDTO.builder()
                .categoryId(2L)
                .categoryName("Books")
                .build();

        PromotionEntity promotionWithMultipleCategories = PromotionEntity.builder()
                .promotionId(1L)
                .promotionName("Summer Sale")
                .promotionDescription("Great summer discounts")
                .productCategoryEntities(Set.of(categoryEntity, category2))
                .state(true)
                .build();

        when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionWithMultipleCategories));
        when(promotionMapper.mapPromotionEntityToDto(promotionWithMultipleCategories)).thenReturn(promotionDTO);
        when(productCategoryMapper.mapCategoryEntityToDTO(categoryEntity)).thenReturn(categoryDTO);
        when(productCategoryMapper.mapCategoryEntityToDTO(category2)).thenReturn(categoryDTO2);

        // When
        Optional<ResponsePromotion> result = promotionAdapter.findPromotionByIdOut(1L);

        // Then
        assertTrue(result.isPresent());
        ResponsePromotion responsePromotion = result.get();

        assertNotNull(responsePromotion.getCategoryDTOList());
        assertEquals(2, responsePromotion.getCategoryDTOList().size());

        List<String> categoryNames = responsePromotion.getCategoryDTOList().stream()
                .map(ProductCategoryDTO::getCategoryName)
                .toList();

        assertTrue(categoryNames.contains("Electronics"));
        assertTrue(categoryNames.contains("Books"));

        verify(productCategoryMapper, times(2)).mapCategoryEntityToDTO(any());
    }

    @Test
    @DisplayName("Should validate promotion entity modification during update")
    void updatePromotionOut_ShouldClearCategoriesAndUpdateFields() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionEntity));
            when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(invocation -> {
                PromotionEntity savedEntity = invocation.getArgument(0);

                // Verify that the entity was properly updated
                assertEquals("Summer Sale", savedEntity.getPromotionName());
                assertEquals("Great summer discounts", savedEntity.getPromotionDescription());
                assertEquals(BigDecimal.valueOf(0.15), savedEntity.getPromotionDiscountRate());
                assertEquals(startDate, savedEntity.getPromotionStartDate());
                assertEquals(endDate, savedEntity.getPromotionEndDate());
                assertEquals("testUser", savedEntity.getModifiedByUser());
                assertEquals(currentTimestamp, savedEntity.getModifiedAt());
                assertTrue(savedEntity.getProductCategoryEntities().isEmpty()); // Categories should be cleared

                return savedEntity;
            });
            when(promotionMapper.mapPromotionEntityToDto(any(PromotionEntity.class))).thenReturn(promotionDTO);

            // When
            PromotionDTO result = promotionAdapter.updatePromotionOut(1L, requestPromotion);

            // Then
            assertNotNull(result);
            verify(promotionRepository).save(any(PromotionEntity.class));
        }
    }

    @Test
    @DisplayName("Should validate promotion entity modification during deletion")
    void deletePromotionOut_ShouldClearCategoriesAndMarkAsDeleted() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.findPromotionByIdWithStateTrue(1L)).thenReturn(Optional.of(promotionEntity));
            when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(invocation -> {
                PromotionEntity savedEntity = invocation.getArgument(0);

                // Verify that the entity was properly marked as deleted
                assertEquals("testUser", savedEntity.getModifiedByUser());
                assertEquals(currentTimestamp, savedEntity.getDeletedAt());
                assertTrue(savedEntity.getProductCategoryEntities().isEmpty()); // Categories should be cleared
                assertFalse(savedEntity.getState()); // Should be marked as inactive

                return savedEntity;
            });
            when(promotionMapper.mapPromotionEntityToDto(any(PromotionEntity.class))).thenReturn(promotionDTO);

            // When
            PromotionDTO result = promotionAdapter.deletePromotionOut(1L);

            // Then
            assertNotNull(result);
            verify(promotionRepository).save(any(PromotionEntity.class));
        }
    }

    @Test
    @DisplayName("Should handle creation with all optional fields")
    void createPromotionOut_WithMinimalData_ShouldCreatePromotionSuccessfully() {
        // Given
        RequestPromotion minimalRequest = RequestPromotion.builder()
                .promotionName("Simple Sale")
                .promotionDescription(null) // Optional
                .promotionDiscountRate(null) // Optional
                .promotionStartDate(null) // Optional
                .promotionEndDate(null) // Optional
                .build();

        PromotionEntity minimalEntity = PromotionEntity.builder()
                .promotionId(2L)
                .promotionName("Simple Sale")
                .promotionDescription(null)
                .promotionDiscountRate(null)
                .promotionStartDate(null)
                .promotionEndDate(null)
                .state(true)
                .build();

        PromotionDTO minimalDTO = PromotionDTO.builder()
                .promotionId(2L)
                .promotionName("Simple Sale")
                .promotionDescription(null)
                .promotionDiscountRate(null)
                .promotionStartDate(null)
                .promotionEndDate(null)
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class)) {
            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            constantsMock.when(Constants::getUserInSession).thenReturn("testUser");

            when(promotionRepository.existsByPromotionName("Simple Sale")).thenReturn(false);
            when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(minimalEntity);
            when(promotionMapper.mapPromotionEntityToDto(minimalEntity)).thenReturn(minimalDTO);

            // When
            PromotionDTO result = promotionAdapter.createPromotionOut(minimalRequest);

            // Then
            assertNotNull(result);
            assertEquals("Simple Sale", result.getPromotionName());
            assertNull(result.getPromotionDescription());
            assertNull(result.getPromotionDiscountRate());
            assertNull(result.getPromotionStartDate());
            assertNull(result.getPromotionEndDate());

            verify(promotionRepository).save(any(PromotionEntity.class));
        }
    }
}