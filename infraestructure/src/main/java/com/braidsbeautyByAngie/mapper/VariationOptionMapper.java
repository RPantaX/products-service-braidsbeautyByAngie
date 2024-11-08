package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.VariationOptionDTO;
import com.braidsbeautyByAngie.entity.VariationEntity;
import com.braidsbeautyByAngie.entity.VariationOptionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class VariationOptionMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public VariationOptionDTO mapVariationOptionEntityToDto(VariationOptionEntity variationOptionEntity) {
        return modelMapper.map(variationOptionEntity, VariationOptionDTO.class);
    }

    public VariationEntity mapVariationOptionDtoToVariationEntity(VariationOptionDTO variationOptionDTO) {
        return modelMapper.map(variationOptionDTO, VariationEntity.class);
    }
}
