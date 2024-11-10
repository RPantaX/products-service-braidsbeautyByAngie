package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromotionMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public PromotionEntity mapPromotionDtoToEntity(PromotionDTO promotionDTO){
        return modelMapper.map(promotionDTO, PromotionEntity.class);
    }

    public PromotionDTO mapPromotionEntityToDto(PromotionEntity promotionEntity){
        return modelMapper.map(promotionEntity, PromotionDTO.class);
    }
    public List<PromotionDTO> mapPromotionListToDtoList(Set<PromotionEntity> promotionEntityList) {
        return promotionEntityList.stream()
                .map(this::mapPromotionEntityToDto)
                .collect(Collectors.toList());
    }
}
