package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PromotionMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    PromotionEntity mapPromotionDtoToEntity(PromotionDTO promotionDTO){
        return modelMapper.map(promotionDTO, PromotionEntity.class);
    }

    PromotionDTO mapPromotionEntityToDto(PromotionEntity promotionEntity){
        return modelMapper.map(promotionEntity, PromotionDTO.class);
    }
}
