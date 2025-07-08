package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import com.braidsbeautyByAngie.mapper.ProductCategoryMapper;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
import com.braidsbeautyByAngie.ports.out.PromotionServiceOut;
import com.braidsbeautyByAngie.repository.PromotionRepository;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.GlobalErrorEnum;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionAdapter implements PromotionServiceOut {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public PromotionDTO createPromotionOut(RequestPromotion requestPromotion) {
        log.info("Attempting to create promotion with name: {}", requestPromotion.getPromotionName().toUpperCase());
        ValidateUtil.evaluar(!promotionExistByName(requestPromotion.getPromotionName()), GlobalErrorEnum.PROMOTION_ALREADY_EXISTS_ERPN00023);

        PromotionEntity promotionEntity = buildPromotionEntity(requestPromotion);
        PromotionEntity savedPromotion = promotionRepository.save(promotionEntity);

        log.info("Promotion '{}' created successfully with ID: {}", savedPromotion.getPromotionName().toUpperCase(), savedPromotion.getPromotionId());
        return promotionMapper.mapPromotionEntityToDto(savedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResponsePromotion> findPromotionByIdOut(Long promotionId) {
        log.info("Searching for promotion with ID: {}", promotionId);

        PromotionEntity promotionEntity = getPromotionEntity(promotionId).orElse(null);
        validateExistsPromotion(promotionEntity, promotionId);
        ResponsePromotion responsePromotion = buildResponsePromotion(promotionEntity);
        log.info("Promotion with ID {} found", promotionId);

        return Optional.of(responsePromotion);
    }

    @Override
    public PromotionDTO updatePromotionOut(Long promotionId, RequestPromotion requestPromotion) {
        log.info("Updating promotion with ID: {}", promotionId);

        PromotionEntity promotionEntity = getPromotionEntity(promotionId).orElse(null);
        validateExistsPromotion(promotionEntity, promotionId);
        ValidateUtil.evaluar(!promotionExistByName(requestPromotion.getPromotionName()), GlobalErrorEnum.PROMOTION_ALREADY_EXISTS_ERPN00023);
        updatePromotionEntity(promotionEntity, requestPromotion);
        PromotionEntity updatedPromotion = promotionRepository.save(promotionEntity);

        log.info("Promotion with ID {} updated successfully", updatedPromotion.getPromotionId());
        return promotionMapper.mapPromotionEntityToDto(updatedPromotion);
    }

    @Override
    public PromotionDTO deletePromotionOut(Long promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);

        PromotionEntity promotionEntity = getPromotionEntity(promotionId).orElse(null);
        validateExistsPromotion(promotionEntity, promotionId);
        markPromotionAsDeleted(promotionEntity);
        PromotionEntity deletedPromotion = promotionRepository.save(promotionEntity);

        log.info("Promotion with ID {} deleted successfully", deletedPromotion.getPromotionId());
        return promotionMapper.mapPromotionEntityToDto(deletedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseListPageablePromotion listPromotionByPageOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        log.info("Fetching promotions with pagination: pageNumber={}, pageSize={}, orderBy={}, sortDir={}", pageNumber, pageSize, orderBy, sortDir);

        Pageable pageable = createPageable(pageNumber, pageSize, orderBy, sortDir);

        Page<PromotionEntity> promotionsPage = promotionRepository.findAllByStateTrueAmdPageable(pageable);

        if (promotionsPage.isEmpty()) {
            log.info("No promotions found for given parameters");
            return null;
        }

        List<ResponsePromotion> responsePromotions = buildResponsePromotions(promotionsPage);
        log.info("Found {} promotions", promotionsPage.getTotalElements());

        return new ResponseListPageablePromotion(
                responsePromotions, promotionsPage.getNumber(),promotionsPage.getSize()
                , promotionsPage.getTotalPages(),promotionsPage.getTotalElements(), promotionsPage.isLast());
    }

    @Override
    public List<PromotionDTO> listPromotionOut() {
        log.info("Fetching all promotions");

        List<PromotionEntity> promotionEntities = promotionRepository.findAllByStateTrue();
        return promotionEntities.stream().map(promotionMapper::mapPromotionEntityToDto).toList();
    }

    @Override
    public Optional<PromotionDTO> findPromotionByNameOut(String promotionName) {
        String promotionNameUpperCase = promotionName.toUpperCase();
        log.info("Searching for promotion with name: {}", promotionNameUpperCase);
        PromotionEntity promotionEntity = promotionRepository.findByPromotionNameAndStateTrue(promotionNameUpperCase).orElse(null);
        if(promotionEntity == null) {
            log.error("Promotion with NAME {} not found", promotionNameUpperCase);
            ValidateUtil.requerido(null, GlobalErrorEnum.PROMOTION_NOT_FOUND_ERPN00022);
        }
        return Optional.of(promotionMapper.mapPromotionEntityToDto(promotionEntity));
    }

    // Helper methods for better organization and code readability
    private PromotionEntity buildPromotionEntity(RequestPromotion requestPromotion) {
        return PromotionEntity.builder()
                .promotionName(requestPromotion.getPromotionName().toUpperCase())
                .promotionDescription(requestPromotion.getPromotionDescription())
                .promotionDiscountRate(requestPromotion.getPromotionDiscountRate())
                .promotionStartDate(requestPromotion.getPromotionStartDate())
                .promotionEndDate(requestPromotion.getPromotionEndDate())
                .createdAt(Constants.getTimestamp())
                .modifiedByUser(Constants.getUserInSession())
                .state(Constants.STATUS_ACTIVE)
                .build();
    }

    private ResponsePromotion buildResponsePromotion(PromotionEntity promotionEntity) {
        List<ProductCategoryDTO> productCategoryDTOs = promotionEntity.getProductCategoryEntities().stream()
                .map(productCategoryMapper::mapCategoryEntityToDTO)
                .toList();

        return ResponsePromotion.builder()
                .promotionDTO(promotionMapper.mapPromotionEntityToDto(promotionEntity))
                .categoryDTOList(productCategoryDTOs)
                .build();
    }

    private List<ResponsePromotion> buildResponsePromotions(Page<PromotionEntity> promotionEntities) {
        return promotionEntities.getContent().stream().map(this::buildResponsePromotion).toList();
    }

    private void updatePromotionEntity(PromotionEntity promotionEntity, RequestPromotion requestPromotion) {
        promotionEntity.setPromotionName(requestPromotion.getPromotionName().toUpperCase());
        promotionEntity.setPromotionDescription(requestPromotion.getPromotionDescription());
        promotionEntity.setPromotionDiscountRate(requestPromotion.getPromotionDiscountRate());
        promotionEntity.setPromotionStartDate(requestPromotion.getPromotionStartDate());
        promotionEntity.setPromotionEndDate(requestPromotion.getPromotionEndDate());
        promotionEntity.setModifiedByUser(Constants.getUserInSession());
        promotionEntity.setModifiedAt(Constants.getTimestamp());
        promotionEntity.setProductCategoryEntities(new HashSet<>());
    }

    private void markPromotionAsDeleted(PromotionEntity promotionEntity) {
        promotionEntity.setModifiedByUser(Constants.getUserInSession());
        promotionEntity.setDeletedAt(Constants.getTimestamp());
        promotionEntity.setProductCategoryEntities(new HashSet<>());
        promotionEntity.setState(Constants.STATUS_INACTIVE);
    }

    private Pageable createPageable(int pageNumber, int pageSize, String orderBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private boolean promotionExistByName(String promotionName) {
        return promotionRepository.existsByPromotionName(promotionName.toUpperCase());
    }

    private Optional<PromotionEntity> getPromotionEntity(Long promotionId) {
        return promotionRepository.findPromotionByIdWithStateTrue(promotionId);
    }
    private void validateExistsPromotion (PromotionEntity promotionEntity, Long promotionId) {
        if(promotionEntity == null) {
            log.error("Promotion with ID {} not found", promotionId);
            ValidateUtil.requerido(null, GlobalErrorEnum.PROMOTION_NOT_FOUND_ERPN00022);
        }
    }
}
