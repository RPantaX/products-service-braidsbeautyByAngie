package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.entity.ProductItemEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductItemMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public ProductItemDTO mapProductItemEntityToDto(ProductItemEntity productItemEntity){
        return modelMapper.map(productItemEntity, ProductItemDTO.class);
    }
    public ProductItemEntity mapProductItemEntity(ProductItemDTO productItemDTO){
        return modelMapper.map(productItemDTO, ProductItemEntity.class);
    }
}
