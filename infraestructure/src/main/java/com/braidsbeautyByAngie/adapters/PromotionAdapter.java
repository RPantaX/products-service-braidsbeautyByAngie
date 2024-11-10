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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionAdapter implements PromotionServiceOut {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public PromotionDTO createPromotionOut(RequestPromotion requestPromotion) {
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
        return promotionMapper.mapPromotionEntityToDto(promotionEntity);
    }

    @Override
    public Optional<ResponsePromotion> findPromotionByIdOut(Long promotionId) {
        PromotionEntity promotionEntity = getPromotionEntity(promotionId).get();

        List<ProductCategoryEntity> productCategoryEntityList = promotionEntity.getProductCategoryEntities().stream().toList();
        List<ProductCategoryDTO> productCategoryDTOList = productCategoryEntityList.stream().map(productCategoryMapper::mapCategoryEntityToDTO).toList();
        ResponsePromotion responsePromotion = ResponsePromotion.builder()
                .promotionDTO(promotionMapper.mapPromotionEntityToDto(promotionEntity))
                .productCategoryDTOList(productCategoryDTOList)
                .build();
        return Optional.of(responsePromotion);
    }

    @Override
    public PromotionDTO updatePromotionOut(Long promotionId, RequestPromotion requestPromotion) {
        PromotionEntity promotionEntity = getPromotionEntity(promotionId).get();
        promotionEntity.setPromotionName(requestPromotion.getPromotionName());
        promotionEntity.setPromotionDescription(requestPromotion.getPromotionDescription());
        promotionEntity.setPromotionDiscountRate(requestPromotion.getPromotionDiscountRate());
        promotionEntity.setPromotionStartDate(requestPromotion.getPromotionStartDate());
        promotionEntity.setPromotionEndDate(requestPromotion.getPromotionEndDate());
        promotionEntity.setModifiedByUser("TEST");
        promotionEntity.setModifiedAt(Constants.getTimestamp());
        return promotionMapper.mapPromotionEntityToDto(promotionRepository.save(promotionEntity));
    }

    @Override
    public PromotionDTO deletePromotionOut(Long promotionId) {
        PromotionEntity promotionEntityOptional = getPromotionEntity(promotionId).get();
        promotionEntityOptional.setModifiedByUser("TEST");
        promotionEntityOptional.setDeletedAt(Constants.getTimestamp());
        promotionEntityOptional.setState(Constants.STATUS_INACTIVE);
        return promotionMapper.mapPromotionEntityToDto(promotionEntityOptional);
    }

    @Override
    public ResponseListPageablePromotion listPromotionByPageOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<PromotionEntity> promotionEntityPage = promotionRepository.findAll(pageable);

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
        return promotionRepository.existsById(promotionId);
    }
    private Optional<PromotionEntity> getPromotionEntity(Long promotionId) {
        if (!promotionExistById(promotionId)) throw new RuntimeException("The promotion does not exist.");
        return promotionRepository.findById(promotionId);
    }
}
