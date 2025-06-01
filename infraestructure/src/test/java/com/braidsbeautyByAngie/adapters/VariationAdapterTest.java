package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.ProductsErrorEnum;
import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.dto.VariationOptionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;
import com.braidsbeautyByAngie.entity.VariationEntity;
import com.braidsbeautyByAngie.entity.VariationOptionEntity;
import com.braidsbeautyByAngie.mapper.VariationMapper;
import com.braidsbeautyByAngie.repository.VariationRepository;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.Constants;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ValidateUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VariationAdapter Unit Tests")
class VariationAdapterTest {

    @Mock
    private VariationMapper variationMapper;

    @Mock
    private VariationRepository variationRepository;

    @InjectMocks
    private VariationAdapter variationAdapter;

    private VariationEntity variationEntity;
    private VariationDTO variationDTO;
    private RequestVariation requestVariation;
    private VariationOptionEntity variationOptionEntity;
    private VariationOptionDTO variationOptionDTO;
    private Timestamp currentTimestamp;

    @BeforeEach
    void setUp() {
        currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

        // Setup variation option entity
        variationOptionEntity = VariationOptionEntity.builder()
                .variationOptionId(1L)
                .variationOptionValue("Red")
                .state(true)
                .build();

        // Setup variation option DTO
        variationOptionDTO = VariationOptionDTO.builder()
                .variationOptionId(1L)
                .variationOptionValue("Red")
                .build();

        // Setup variation entity
        variationEntity = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Arrays.asList(variationOptionEntity))
                .state(true)
                .createdAt(currentTimestamp)
                .modifiedByUser("testUser")
                .build();

        // Setup variation DTO
        variationDTO = VariationDTO.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Arrays.asList(variationOptionDTO))
                .build();

        // Setup request
        requestVariation = new RequestVariation();
        requestVariation.setVariationName("Color");
    }

    @Test
    @DisplayName("Should create variation successfully")
    void createVariationOut_WithValidData_ShouldReturnVariationDTO() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.save(any(VariationEntity.class))).thenReturn(variationEntity);
            when(variationMapper.mapVariationEntityToDto(variationEntity)).thenReturn(variationDTO);

            // When
            VariationDTO result = variationAdapter.createVariationOut(requestVariation);

            // Then
            assertNotNull(result);
            assertEquals("Color", result.getVariationName());
            assertEquals(1L, result.getVariationId());
            assertNotNull(result.getVariationOptionEntities());
            assertEquals(1, result.getVariationOptionEntities().size());
            assertEquals("Red", result.getVariationOptionEntities().get(0).getVariationOptionValue());

            verify(variationRepository).save(any(VariationEntity.class));
            verify(variationMapper).mapVariationEntityToDto(variationEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when saving variation fails")
    void createVariationOut_WithSaveError_ShouldThrowException() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.save(any(VariationEntity.class)))
                    .thenThrow(new RuntimeException("Database error"));

            try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
                validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(ProductsErrorEnum.class)))
                        .thenThrow(new RuntimeException("Variation creation failed"));

                // When & Then
                assertThrows(RuntimeException.class, () -> {
                    variationAdapter.createVariationOut(requestVariation);
                });

                verify(variationRepository).save(any(VariationEntity.class));
                verify(variationMapper, never()).mapVariationEntityToDto(any());
            }
        }
    }

    @Test
    @DisplayName("Should update variation successfully with same name")
    void updateVariationOut_WithSameName_ShouldReturnUpdatedVariationDTO() {
        // Given
        RequestVariation updateRequest = new RequestVariation();
        updateRequest.setVariationName("Color"); // Same name

        VariationEntity updatedEntity = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .state(true)
                .modifiedAt(currentTimestamp)
                .modifiedByUser("testUser")
                .build();

        VariationDTO updatedDTO = VariationDTO.builder()
                .variationId(1L)
                .variationName("Color")
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.save(any(VariationEntity.class))).thenReturn(updatedEntity);
            when(variationMapper.mapVariationEntityToDto(updatedEntity)).thenReturn(updatedDTO);

            // When
            VariationDTO result = variationAdapter.updateVariationOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            assertEquals("Color", result.getVariationName());
            assertEquals(1L, result.getVariationId());

            verify(variationRepository).findByVariationIdAndStateTrue(1L);
            verify(variationRepository).save(any(VariationEntity.class));
            verify(variationMapper).mapVariationEntityToDto(updatedEntity);
            // Should not check for existing name since it's the same
            verify(variationRepository, never()).existsByVariationName(anyString());
        }
    }

    @Test
    @DisplayName("Should update variation successfully with different name")
    void updateVariationOut_WithDifferentName_ShouldReturnUpdatedVariationDTO() {
        // Given
        RequestVariation updateRequest = new RequestVariation();
        updateRequest.setVariationName("Size"); // Different name

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.existsByVariationName("Size")).thenReturn(false);
            when(variationRepository.save(any(VariationEntity.class))).thenReturn(variationEntity);
            when(variationMapper.mapVariationEntityToDto(variationEntity)).thenReturn(variationDTO);

            // When
            VariationDTO result = variationAdapter.updateVariationOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(variationRepository).existsByVariationName("Size");
            verify(variationRepository).save(any(VariationEntity.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when updating to existing variation name")
    void updateVariationOut_WithExistingDifferentName_ShouldThrowException() {
        // Given
        RequestVariation updateRequest = new RequestVariation();
        updateRequest.setVariationName("Size"); // Different existing name

        when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
        when(variationRepository.existsByVariationName("Size")).thenReturn(true);

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.evaluar(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Variation already exists"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                variationAdapter.updateVariationOut(1L, updateRequest);
            });

            verify(variationRepository).existsByVariationName("Size");
            verify(variationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent variation")
    void updateVariationOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(variationRepository.findByVariationIdAndStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Variation not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                variationAdapter.updateVariationOut(999L, requestVariation);
            });

            verify(variationRepository).findByVariationIdAndStateTrue(999L);
            verify(variationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should delete variation successfully (soft delete)")
    void deleteVariationOut_WithValidId_ShouldReturnDeletedVariationDTO() {
        // Given
        VariationEntity deletedEntity = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .state(false) // Soft deleted
                .deletedAt(currentTimestamp)
                .modifiedByUser("testUser")
                .variationOptionEntities(null) // Options cleared
                .build();

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.save(any(VariationEntity.class))).thenReturn(deletedEntity);
            when(variationMapper.mapVariationEntityToDto(deletedEntity)).thenReturn(variationDTO);

            // When
            VariationDTO result = variationAdapter.deleteVariationOut(1L);

            // Then
            assertNotNull(result);
            verify(variationRepository).findByVariationIdAndStateTrue(1L);
            verify(variationRepository).save(any(VariationEntity.class));
            verify(variationMapper).mapVariationEntityToDto(deletedEntity);
        }
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent variation")
    void deleteVariationOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(variationRepository.findByVariationIdAndStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Variation not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                variationAdapter.deleteVariationOut(999L);
            });

            verify(variationRepository).findByVariationIdAndStateTrue(999L);
            verify(variationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should throw exception when saving deleted variation fails")
    void deleteVariationOut_WithSaveError_ShouldThrowException() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.save(any(VariationEntity.class)))
                    .thenThrow(new RuntimeException("Database error"));

            try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
                validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(ProductsErrorEnum.class)))
                        .thenThrow(new RuntimeException("Variation creation failed"));

                // When & Then
                assertThrows(RuntimeException.class, () -> {
                    variationAdapter.deleteVariationOut(1L);
                });

                verify(variationRepository).save(any(VariationEntity.class));
                verify(variationMapper, never()).mapVariationEntityToDto(any());
            }
        }
    }

    @Test
    @DisplayName("Should find variation by ID successfully")
    void findVariationByIdOut_WithValidId_ShouldReturnVariationDTO() {
        // Given
        when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
        when(variationMapper.mapVariationEntityToDto(variationEntity)).thenReturn(variationDTO);

        // When
        VariationDTO result = variationAdapter.findVariationByIdOut(1L);

        // Then
        assertNotNull(result);
        assertEquals("Color", result.getVariationName());
        assertEquals(1L, result.getVariationId());
        assertNotNull(result.getVariationOptionEntities());
        assertEquals(1, result.getVariationOptionEntities().size());
        assertEquals("Red", result.getVariationOptionEntities().get(0).getVariationOptionValue());

        verify(variationRepository).findByVariationIdAndStateTrue(1L);
        verify(variationMapper).mapVariationEntityToDto(variationEntity);
    }

    @Test
    @DisplayName("Should throw exception when variation not found by ID")
    void findVariationByIdOut_WithInvalidId_ShouldThrowException() {
        // Given
        when(variationRepository.findByVariationIdAndStateTrue(999L)).thenReturn(Optional.empty());

        try (MockedStatic<ValidateUtil> validateUtilMock = mockStatic(ValidateUtil.class)) {
            validateUtilMock.when(() -> ValidateUtil.requerido(eq(false), any(ProductsErrorEnum.class)))
                    .thenThrow(new RuntimeException("Variation not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                variationAdapter.findVariationByIdOut(999L);
            });

            verify(variationRepository).findByVariationIdAndStateTrue(999L);
            verify(variationMapper, never()).mapVariationEntityToDto(any());
        }
    }

    @Test
    @DisplayName("Should list all variations successfully")
    void listVariationOut_ShouldReturnAllVariations() {
        // Given
        VariationEntity variation2 = VariationEntity.builder()
                .variationId(2L)
                .variationName("Size")
                .variationOptionEntities(Collections.emptyList())
                .state(true)
                .build();

        VariationDTO variationDTO2 = VariationDTO.builder()
                .variationId(2L)
                .variationName("Size")
                .variationOptionEntities(Collections.emptyList())
                .build();

        List<VariationEntity> allVariations = Arrays.asList(variationEntity, variation2);

        when(variationRepository.findAllVariationsWithOptions()).thenReturn(allVariations);
        when(variationMapper.mapVariationEntityToDto(variationEntity)).thenReturn(variationDTO);
        when(variationMapper.mapVariationEntityToDto(variation2)).thenReturn(variationDTO2);

        // When
        List<VariationDTO> result = variationAdapter.listVariationOut();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("Color", result.get(0).getVariationName());
        assertEquals(1, result.get(0).getVariationOptionEntities().size());

        assertEquals("Size", result.get(1).getVariationName());
        assertTrue(result.get(1).getVariationOptionEntities().isEmpty());

        verify(variationRepository).findAllVariationsWithOptions();
        verify(variationMapper, times(2)).mapVariationEntityToDto(any());
    }

    @Test
    @DisplayName("Should return empty list when no variations exist")
    void listVariationOut_WithNoVariations_ShouldReturnEmptyList() {
        // Given
        when(variationRepository.findAllVariationsWithOptions()).thenReturn(Collections.emptyList());

        // When
        List<VariationDTO> result = variationAdapter.listVariationOut();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(variationRepository).findAllVariationsWithOptions();
        verify(variationMapper, never()).mapVariationEntityToDto(any());
    }

    @Test
    @DisplayName("Should handle variation with multiple options")
    void findVariationByIdOut_WithMultipleOptions_ShouldReturnAllOptions() {
        // Given
        VariationOptionEntity option2 = VariationOptionEntity.builder()
                .variationOptionId(2L)
                .variationOptionValue("Blue")
                .state(true)
                .build();

        VariationOptionDTO optionDTO2 = VariationOptionDTO.builder()
                .variationOptionId(2L)
                .variationOptionValue("Blue")
                .build();

        VariationEntity variationWithMultipleOptions = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Arrays.asList(variationOptionEntity, option2))
                .state(true)
                .build();

        VariationDTO variationDTOWithMultipleOptions = VariationDTO.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Arrays.asList(variationOptionDTO, optionDTO2))
                .build();

        when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationWithMultipleOptions));
        when(variationMapper.mapVariationEntityToDto(variationWithMultipleOptions)).thenReturn(variationDTOWithMultipleOptions);

        // When
        VariationDTO result = variationAdapter.findVariationByIdOut(1L);

        // Then
        assertNotNull(result);
        assertEquals("Color", result.getVariationName());
        assertEquals(2, result.getVariationOptionEntities().size());

        List<String> optionValues = result.getVariationOptionEntities().stream()
                .map(VariationOptionDTO::getVariationOptionValue)
                .toList();

        assertTrue(optionValues.contains("Red"));
        assertTrue(optionValues.contains("Blue"));
    }

    @Test
    @DisplayName("Should handle variation with no options")
    void findVariationByIdOut_WithNoOptions_ShouldReturnVariationWithEmptyOptions() {
        // Given
        VariationEntity variationWithoutOptions = VariationEntity.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Collections.emptyList())
                .state(true)
                .build();

        VariationDTO variationDTOWithoutOptions = VariationDTO.builder()
                .variationId(1L)
                .variationName("Color")
                .variationOptionEntities(Collections.emptyList())
                .build();

        when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationWithoutOptions));
        when(variationMapper.mapVariationEntityToDto(variationWithoutOptions)).thenReturn(variationDTOWithoutOptions);

        // When
        VariationDTO result = variationAdapter.findVariationByIdOut(1L);

        // Then
        assertNotNull(result);
        assertEquals("Color", result.getVariationName());
        assertNotNull(result.getVariationOptionEntities());
        assertTrue(result.getVariationOptionEntities().isEmpty());
    }

    @Test
    @DisplayName("Should validate entity modification during deletion")
    void deleteVariationOut_ShouldClearOptionsAndMarkAsDeleted() {
        // Given
        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.save(any(VariationEntity.class))).thenAnswer(invocation -> {
                VariationEntity savedEntity = invocation.getArgument(0);

                // Verify that the entity was properly marked as deleted
                assertFalse(savedEntity.getState()); // Should be marked as inactive
                assertEquals("testUser", savedEntity.getModifiedByUser());
                assertEquals(currentTimestamp, savedEntity.getDeletedAt());
                assertNull(savedEntity.getVariationOptionEntities()); // Options should be cleared

                return savedEntity;
            });
            when(variationMapper.mapVariationEntityToDto(any(VariationEntity.class))).thenReturn(variationDTO);

            // When
            VariationDTO result = variationAdapter.deleteVariationOut(1L);

            // Then
            assertNotNull(result);
            verify(variationRepository).save(any(VariationEntity.class));
        }
    }

    @Test
    @DisplayName("Should validate entity modification during update")
    void updateVariationOut_ShouldUpdateFieldsCorrectly() {
        // Given
        RequestVariation updateRequest = new RequestVariation();
        updateRequest.setVariationName("Updated Color");

        try (MockedStatic<Constants> constantsMock = mockStatic(Constants.class);
             MockedStatic<com.braidsbeautyByAngie.aggregates.constants.Constants> localConstantsMock =
                     mockStatic(com.braidsbeautyByAngie.aggregates.constants.Constants.class)) {

            constantsMock.when(Constants::getTimestamp).thenReturn(currentTimestamp);
            localConstantsMock.when(com.braidsbeautyByAngie.aggregates.constants.Constants::getUserInSession)
                    .thenReturn("testUser");

            when(variationRepository.findByVariationIdAndStateTrue(1L)).thenReturn(Optional.of(variationEntity));
            when(variationRepository.existsByVariationName("Updated Color")).thenReturn(false);
            when(variationRepository.save(any(VariationEntity.class))).thenAnswer(invocation -> {
                VariationEntity savedEntity = invocation.getArgument(0);

                // Verify that the entity was properly updated
                assertEquals("Updated Color", savedEntity.getVariationName());
                assertEquals("testUser", savedEntity.getModifiedByUser());
                assertEquals(currentTimestamp, savedEntity.getModifiedAt());

                return savedEntity;
            });
            when(variationMapper.mapVariationEntityToDto(any(VariationEntity.class))).thenReturn(variationDTO);

            // When
            VariationDTO result = variationAdapter.updateVariationOut(1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(variationRepository).save(any(VariationEntity.class));
        }
    }
}