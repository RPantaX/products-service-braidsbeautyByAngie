package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public ProductCategoryDTO mapCategoryEntityToDTO(ProductCategoryEntity productCategoryEntity){
        return modelMapper.map(productCategoryEntity, ProductCategoryDTO.class);
    }
    public ProductCategoryEntity mapDTOToCategoryEntity(ProductCategoryDTO productCategoryDTO){
        return modelMapper.map(productCategoryDTO, ProductCategoryEntity.class);
    }

}
