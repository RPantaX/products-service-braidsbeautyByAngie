package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.VariationOptionDTO;
import com.braidsbeautyByAngie.entity.ProductEntity;
import com.braidsbeautyByAngie.entity.VariationEntity;
import com.braidsbeautyByAngie.entity.VariationOptionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VariationOptionMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public VariationOptionDTO mapVariationOptionEntityToDto(VariationOptionEntity variationOptionEntity) {
        return modelMapper.map(variationOptionEntity, VariationOptionDTO.class);
    }

    public VariationEntity mapVariationOptionDtoToVariationEntity(VariationOptionDTO variationOptionDTO) {
        return modelMapper.map(variationOptionDTO, VariationEntity.class);
    }
    public List<VariationOptionDTO> mapProductEntityListToDtoList(Set<VariationOptionEntity> variationOptionEntities) {
        return  variationOptionEntities.stream()
                .map(this::mapVariationOptionEntityToDto)
                .collect(Collectors.toList());
    }
}
