package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;
import com.braidsbeautyByAngie.entity.VariationEntity;
import com.braidsbeautyByAngie.mapper.VariationMapper;
import com.braidsbeautyByAngie.ports.out.VariationServiceOut;
import com.braidsbeautyByAngie.repository.VariationRepository;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class VariationAdapter implements VariationServiceOut {

    private final VariationMapper variationMapper;

    private final VariationRepository variationRepository;

    @Override
    public VariationDTO createVariationOut(RequestVariation requestVariation) {

        VariationEntity variationEntity = VariationEntity.builder()
                .variationName(requestVariation.getVariationName())
                .state(Constants.STATUS_ACTIVE)
                .createdAt(Constants.getTimestamp())
                .modifiedByUser("TEST")
                .build();
        VariationEntity variationSaved = variationRepository.save(variationEntity);
        return variationMapper.mapVariationEntityToDto(variationSaved);
    }

    @Override
    public VariationDTO updateVariationOut(Long variationId, RequestVariation requestVariation) {

        VariationEntity variationEntity = variationRepository.findByVariationIdAndStateTrue(variationId).orElseThrow(() -> new AppExceptionNotFound("Variation not found"));

        if(!variationEntity.getVariationName().equals(requestVariation.getVariationName()) && variationRepository.existsByVariationName(requestVariation.getVariationName())){
                throw new AppException("The name of the variation already exists");
        }

        variationEntity.setVariationName(requestVariation.getVariationName());
        variationEntity.setModifiedByUser("TEST-UPDATED");
        variationEntity.setModifiedAt(Constants.getTimestamp());

        VariationEntity variationUpdated = variationRepository.save(variationEntity);
        return variationMapper.mapVariationEntityToDto(variationUpdated);
    }

    @Override
    public VariationDTO deleteVariationOut(Long variationId) {

        VariationEntity variationEntity = variationRepository.findByVariationIdAndStateTrue(variationId).orElseThrow(() -> new AppExceptionNotFound("Variation not found"));

        variationEntity.setState(Constants.STATUS_INACTIVE);
        variationEntity.setModifiedByUser("TEST-DELETED");
        variationEntity.setDeletedAt(Constants.getTimestamp());
        variationEntity.setVariationOptionEntities(null);
        return variationMapper.mapVariationEntityToDto(variationEntity);
    }

    @Override
    public VariationDTO findVariationByIdOut(Long variationId) {
        VariationEntity variationEntity = variationRepository.findByVariationIdAndStateTrue(variationId).orElseThrow(() -> new AppExceptionNotFound("Variation not found"));
        return variationMapper.mapVariationEntityToDto(variationEntity);
    }

    @Override
    public List<VariationDTO> listVariationOut() {
        List<VariationEntity> variationEntityList = variationRepository.findAllByStateTrue();
        return variationEntityList.stream().map(variationMapper::mapVariationEntityToDto).toList();
    }
}
