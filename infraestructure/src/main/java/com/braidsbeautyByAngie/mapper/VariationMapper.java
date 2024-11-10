package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.entity.VariationEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class VariationMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public VariationEntity mapVariationDtoToEntity(VariationDTO variationDTO) {
        return modelMapper.map(variationDTO, VariationEntity.class);
    }

    public VariationDTO mapVariationEntityToDto(VariationEntity variationEntity) {
        return modelMapper.map(variationEntity, VariationDTO.class);
    }
}
