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
import com.braidsbeautyByAngie.ports.out.PromotionServiceOut;
import com.braidsbeautyByAngie.repository.PromotionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionAdapter implements PromotionServiceOut {

    private final PromotionRepository promotionRepository;

    private final PromotionMapper promotionMapper;
    private final ProductCategoryMapper productCategoryMapper;

    private static final Logger logger = LoggerFactory.getLogger(PromotionAdapter.class);

    @Override
    public PromotionDTO createPromotionOut(RequestPromotion requestPromotion) {

        logger.info("Creating promotion with name: {}", requestPromotion.getPromotionName());
        if(promotionExistByName(requestPromotion.getPromotionName()))  throw new RuntimeException("The name of the promotion already exists");
        PromotionEntity promotionEntity = PromotionEntity.builder()
                .promotionName(requestPromotion.getPromotionName())
                .promotionDescription(requestPromotion.getPromotionDescription())
                .promotionDiscountRate(requestPromotion.getPromotionDiscountRate())
                .promotionStartDate(requestPromotion.getPromotionStartDate())
                .promotionEndDate(requestPromotion.getPromotionEndDate())
                .createdAt(Constants.getTimestamp())
                .modifiedByUser("TEST")
                .state(Constants.STATUS_ACTIVE)
                .build();
        PromotionEntity promotionSaved = promotionRepository.save(promotionEntity);
        logger.info("Promotion '{}' created successfully with ID: {}",promotionSaved.getPromotionName(),promotionSaved.getPromotionId());
        return promotionMapper.mapPromotionEntityToDto(promotionSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResponsePromotion> findPromotionByIdOut(Long promotionId) {
        logger.info("Searching for promotion with ID: {}", promotionId);
        PromotionEntity promotionEntity = getPromotionEntity(promotionId).get();

        List<ProductCategoryEntity> productCategoryEntityList = promotionEntity.getProductCategoryEntities().stream().toList();
        List<ProductCategoryDTO> productCategoryDTOList = productCategoryEntityList.stream().map(productCategoryMapper::mapCategoryEntityToDTO).toList();
        ResponsePromotion responsePromotion = ResponsePromotion.builder()
                .promotionDTO(promotionMapper.mapPromotionEntityToDto(promotionEntity))
                .productCategoryDTOList(productCategoryDTOList)
                .build();
        logger.info("Promotion with ID {} found", promotionId);
        return Optional.of(responsePromotion);
    }

    @Override
    public PromotionDTO updatePromotionOut(Long promotionId, RequestPromotion requestPromotion) {
        logger.info("Searching for update promotion with ID: {}", promotionId);
        PromotionEntity promotionEntity = getPromotionEntity(promotionId).get();
        promotionEntity.setPromotionName(requestPromotion.getPromotionName());
        promotionEntity.setPromotionDescription(requestPromotion.getPromotionDescription());
        promotionEntity.setPromotionDiscountRate(requestPromotion.getPromotionDiscountRate());
        promotionEntity.setPromotionStartDate(requestPromotion.getPromotionStartDate());
        promotionEntity.setPromotionEndDate(requestPromotion.getPromotionEndDate());
        promotionEntity.setModifiedByUser("TEST");
        promotionEntity.setModifiedAt(Constants.getTimestamp());

        PromotionEntity promotionEntityUpdated = promotionRepository.save(promotionEntity);
        logger.info("promotion updated with ID: {}", promotionEntityUpdated.getPromotionId());
        return promotionMapper.mapPromotionEntityToDto(promotionEntityUpdated);
    }

    @Override
    public PromotionDTO deletePromotionOut(Long promotionId) {
        logger.info("Searching promotion for delete with ID: {}", promotionId);
        Set<ProductCategoryEntity> productCategoryEntitySet = new HashSet<>();
        PromotionEntity promotionEntityOptional = getPromotionEntity(promotionId).get();
        promotionEntityOptional.setModifiedByUser("TEST");
        promotionEntityOptional.setDeletedAt(Constants.getTimestamp());
        promotionEntityOptional.setProductCategoryEntities(productCategoryEntitySet);
        promotionEntityOptional.setState(Constants.STATUS_INACTIVE);
        PromotionEntity promotionDeleted = promotionRepository.save(promotionEntityOptional);
        logger.info("Product deleted with ID: {}", promotionId);
        return promotionMapper.mapPromotionEntityToDto(promotionDeleted);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseListPageablePromotion listPromotionByPageOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        logger.info("Searching all promotions with the following parameters: {}", Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        if (promotionRepository.findAllByStateTrueAmdPageable(pageable).isEmpty()) return null;

        Page<PromotionEntity> promotionEntityPage = promotionRepository.findAllByStateTrueAmdPageable(pageable);

        List<ResponsePromotion> responsePromotionList = promotionEntityPage.getContent().stream().map(promotionEntity -> {
            // Convertir cada PromotionEntity a ResponsePromotion
            List<ProductCategoryDTO> productCategoryDTOList = promotionEntity.getProductCategoryEntities().stream()
                    .map(productCategoryMapper::mapCategoryEntityToDTO)
                    .toList();

            return ResponsePromotion.builder()
                    .promotionDTO(promotionMapper.mapPromotionEntityToDto(promotionEntity))
                    .productCategoryDTOList(productCategoryDTOList)
                    .build();
        }).toList();
        logger.info("Promotions found with the following parameters: {}", Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));
        return ResponseListPageablePromotion.builder()
                .responsePromotionList(responsePromotionList)
                .pageNumber(promotionEntityPage.getNumber())
                .totalElements(promotionEntityPage.getTotalElements())
                .totalPages(promotionEntityPage.getTotalPages())
                .pageSize(promotionEntityPage.getSize())
                .end(promotionEntityPage.isLast())
                .build();
    }

    private boolean promotionExistByName(String promotionName) {
        return promotionRepository.existsByPromotionName(promotionName);
    }
    private boolean promotionExistById(Long promotionId) {
        return promotionRepository.existsByPromotionIdAndStateTrue(promotionId);
    }
    private Optional<PromotionEntity> getPromotionEntity(Long promotionId) {
        if (!promotionExistById(promotionId)) throw new RuntimeException("The promotion does not exist.");
        return promotionRepository.findPromotionByIdWithStateTrue(promotionId);
    }
}
