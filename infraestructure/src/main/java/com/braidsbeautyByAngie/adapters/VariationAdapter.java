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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class VariationAdapter implements VariationServiceOut {

    private final VariationMapper variationMapper;
    private final VariationRepository variationRepository;

    @Override
    public VariationDTO createVariationOut(RequestVariation requestVariation) {
        log.info("Starting creation of variation with name: {}", requestVariation.getVariationName());

        VariationEntity variationEntity = buildVariationEntity(requestVariation);
        VariationEntity variationSaved = saveVariationEntity(variationEntity);

        log.info("Variation created successfully with ID: {}", variationSaved.getVariationId());
        return variationMapper.mapVariationEntityToDto(variationSaved);
    }

    @Override
    public VariationDTO updateVariationOut(Long variationId, RequestVariation requestVariation) {
        log.info("Starting update for variation ID: {}", variationId);

        VariationEntity variationEntity = findVariationById(variationId);
        validateVariationName(requestVariation.getVariationName(), variationEntity);

        variationEntity.setVariationName(requestVariation.getVariationName());
        variationEntity.setModifiedByUser("TEST-UPDATED");
        variationEntity.setModifiedAt(Constants.getTimestamp());

        VariationEntity variationUpdated = saveVariationEntity(variationEntity);
        log.info("Variation updated successfully with ID: {}", variationUpdated.getVariationId());
        return variationMapper.mapVariationEntityToDto(variationUpdated);
    }

    @Override
    public VariationDTO deleteVariationOut(Long variationId) {
        log.info("Starting deletion of variation ID: {}", variationId);

        VariationEntity variationEntity = findVariationById(variationId);

        variationEntity.setState(Constants.STATUS_INACTIVE);
        variationEntity.setModifiedByUser("TEST-DELETED");
        variationEntity.setDeletedAt(Constants.getTimestamp());
        variationEntity.setVariationOptionEntities(null);

        VariationEntity variationDeleted = saveVariationEntity(variationEntity);
        log.info("Variation deleted successfully with ID: {}", variationDeleted.getVariationId());
        return variationMapper.mapVariationEntityToDto(variationDeleted);
    }

    @Override
    public VariationDTO findVariationByIdOut(Long variationId) {
        log.info("Fetching variation with ID: {}", variationId);
        VariationEntity variationEntity = findVariationById(variationId);
        log.info("Variation found with ID: {}", variationEntity.getVariationId());
        return variationMapper.mapVariationEntityToDto(variationEntity);
    }

    @Override
    public List<VariationDTO> listVariationOut() {
        log.info("Fetching all variations");
        List<VariationEntity> variationEntityList = variationRepository.findAllByStateTrue();
        log.info("Total variations fetched: {}", variationEntityList.size());
        return variationEntityList.stream().map(variationMapper::mapVariationEntityToDto).toList();
    }

    private VariationEntity buildVariationEntity(RequestVariation requestVariation) {
        return VariationEntity.builder()
                .variationName(requestVariation.getVariationName())
                .state(Constants.STATUS_ACTIVE)
                .createdAt(Constants.getTimestamp())
                .modifiedByUser("TEST")
                .build();
    }

    private VariationEntity saveVariationEntity(VariationEntity variationEntity) {
        try {
            return variationRepository.save(variationEntity);
        } catch (Exception e) {
            log.error("Error saving variation entity: {}", variationEntity.getVariationId(), e);
            throw new AppException("Error saving variation");
        }
    }

    private VariationEntity findVariationById(Long variationId) {
        return variationRepository.findByVariationIdAndStateTrue(variationId)
                .orElseThrow(() -> {
                    log.error("Variation not found with ID: {}", variationId);
                    return new AppExceptionNotFound("Variation not found");
                });
    }

    private void validateVariationName(String variationName, VariationEntity variationEntity) {
        if (!variationEntity.getVariationName().equals(variationName) && variationRepository.existsByVariationName(variationName)) {
            log.warn("Attempted to update variation to an existing name: {}", variationName);
            throw new AppException("The name of the variation already exists");
        }
    }
}
