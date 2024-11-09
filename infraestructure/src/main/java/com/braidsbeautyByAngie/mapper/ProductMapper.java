package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public ProductDTO mapProductEntityToDto(ProductEntity productEntity) {
        return modelMapper.map(productEntity, ProductDTO.class);
    }
    public ProductEntity mapDtoToProductEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, ProductEntity.class);
    }

    public List<ProductDTO> mapProductEntityListToDtoList(List<ProductEntity> productEntityList) {
        return productEntityList.stream()
                .map(this::mapProductEntityToDto)
                .collect(Collectors.toList());
    }
}
