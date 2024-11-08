package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    ProductEntity mapProductEntityToDto(ProductEntity productEntity) {
        return modelMapper.map(productEntity, ProductEntity.class);
    }
    ProductDTO mapDtoToProductEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, ProductDTO.class);
    }
}
