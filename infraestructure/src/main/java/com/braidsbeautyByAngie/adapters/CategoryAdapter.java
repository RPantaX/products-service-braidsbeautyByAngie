package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseSubCategory;
import com.braidsbeautyByAngie.entity.ProductCategoryEntity;
import com.braidsbeautyByAngie.entity.PromotionEntity;
import com.braidsbeautyByAngie.mapper.ProductCategoryMapper;
import com.braidsbeautyByAngie.mapper.ProductMapper;
import com.braidsbeautyByAngie.mapper.PromotionMapper;
import com.braidsbeautyByAngie.ports.out.CategoryServiceOut;
import com.braidsbeautyByAngie.repository.ProductCategoryRepository;


import com.braidsbeautyByAngie.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryServiceOut {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;
    private final PromotionRepository promotionRepository;
    @Override
    public ProductCategoryDTO createCategoryOut(RequestCategory requestCategory) {

        if (categoryNameExistsByName(requestCategory.getProductCategoryName()) ) throw new RuntimeException("The name of the category already exists");
        Set<PromotionEntity> promotionEntitySet = (Set<PromotionEntity>) promotionRepository.findAllById(requestCategory.getPromotionListId());
        ProductCategoryEntity productCategoryEntity = ProductCategoryEntity.builder()
                .productCategoryName(requestCategory.getProductCategoryName())
                .promotionEntities(promotionEntitySet)
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("prueba")
                .build();

        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryRepository.save(productCategoryEntity));
    }

    @Override
    public ProductCategoryDTO createSubCategoryOut(RequestSubCategory requestSubCategory) {
        if (categoryNameExistsByName(requestSubCategory.getProductSubCategoryName()) ) throw new RuntimeException("The name of the category already exists");
        Optional<ProductCategoryEntity> productCategoryParent = productCategoryRepository.findById(requestSubCategory.getProductCategoryParentId());
        ProductCategoryEntity productCategoryEntity = ProductCategoryEntity.builder()
                .parentCategory(productCategoryParent.get())
                .productCategoryName(requestSubCategory.getProductSubCategoryName())
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("prueba")
                .build();
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryRepository.save(productCategoryEntity));
    }

    @Override
    public Optional<ResponseCategory> findCategoryByIdOut(Long categoryId) {
        Optional<ProductCategoryEntity> productCategoryEntity = getProductCategoryEntity(categoryId);

        ProductCategoryDTO productCategoryDTO = productCategoryMapper.mapCategoryEntityToDTO(productCategoryEntity.get());
        List<ProductDTO> productDTOList = productMapper.mapProductEntityListToDtoList(productCategoryEntity.get().getProductEntities());
        Set<PromotionDTO> promotionDTOList = (Set<PromotionDTO>) promotionMapper.mapPromotionListToDtoList(productCategoryEntity.get().getPromotionEntities());

        // Mapear subcategorías
        List<ResponseSubCategory> responseSubCategoryList = productCategoryEntity.get().getSubCategories()
                .stream()
                .map(subCategoryEntity -> {
                    ProductCategoryDTO subCategoryDTO = productCategoryMapper.mapCategoryEntityToDTO(subCategoryEntity);
                    return ResponseSubCategory.builder()
                            .productCategoryDTO(subCategoryDTO)
                            .build();
                })
                .collect(Collectors.toList());

        ResponseCategory responseCategory = ResponseCategory.builder()
                .productCategoryDTO(productCategoryDTO)
                .responseSubCategoryList(responseSubCategoryList)
                .productDTOList(productDTOList)
                .promotionDTOList(promotionDTOList)
                .build();

        return Optional.ofNullable(responseCategory);
    }

    @Override
    public ProductCategoryDTO updateCategoryOut(RequestCategory requestCategory, Long categoryId) {

        Optional<ProductCategoryEntity> productCategorySaved = getProductCategoryEntity(categoryId);
        Set<PromotionEntity> promotionEntitySet = (Set<PromotionEntity>) promotionRepository.findAllById(requestCategory.getPromotionListId());
        productCategorySaved.get().setProductCategoryName(requestCategory.getProductCategoryName());
        productCategorySaved.get().setPromotionEntities(promotionEntitySet);
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryRepository.save(productCategorySaved.get()));
    }

    @Override
    public ProductCategoryDTO updateSubCategoryOut(RequestSubCategory requestSubCategory, Long categoryId) {

        Optional<ProductCategoryEntity> productCategoryParent = getProductCategoryEntity(requestSubCategory.getProductCategoryParentId());
        Optional<ProductCategoryEntity> productSubCategory = getProductCategoryEntity(requestSubCategory.getProductCategoryParentId());

        productSubCategory.get().setProductCategoryName(requestSubCategory.getProductSubCategoryName());
        productSubCategory.get().setParentCategory(productCategoryParent.get());
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryRepository.save(productSubCategory.get()));
    }

    @Override
    public ProductCategoryDTO deleteCategoryOut(Long categoryId) {
        Optional<ProductCategoryEntity> productCategorySaved = getProductCategoryEntity(categoryId);
        productCategorySaved.get().setState(Constants.STATUS_INACTIVE);
        productCategorySaved.get().setModifiedByUser("PRUEBA");
        productCategorySaved.get().setDeletedAt(Constants.getTimestamp());
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryRepository.save(productCategorySaved.get()));
    }

    @Override
    public ResponseListPageableCategory listCategoryPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<ProductCategoryEntity> page = productCategoryRepository.findAll(pageable);

        // Mapear cada ProductCategoryEntity a un ResponseCategory, incluyendo ProductDTO y PromotionDTO
        List<ResponseCategory> responseCategoryList = page.getContent().stream()
                .map(productCategoryEntity -> {
                    // Convertir la entidad de categoría en un DTO
                    ProductCategoryDTO productCategoryDTO = productCategoryMapper.mapCategoryEntityToDTO(productCategoryEntity);

                    // Convertir las entidades de producto y promoción a DTOs
                    List<ProductDTO> productDTOList = productMapper.mapProductEntityListToDtoList(productCategoryEntity.getProductEntities());
                    List<PromotionDTO> promotionDTOList = promotionMapper.mapPromotionListToDtoList(productCategoryEntity.getPromotionEntities());

                    // Crear y retornar el ResponseCategory
                    return ResponseCategory.builder()
                            .productCategoryDTO(productCategoryDTO)
                            .productDTOList(productDTOList)
                            .promotionDTOList((Set<PromotionDTO>) promotionDTOList)
                            .build();
                })
                .collect(Collectors.toList());

        // Crear y retornar el objeto paginado de respuesta
        return ResponseListPageableCategory.builder()
                .responseCategoryList(responseCategoryList)
                .pageNumber(page.getNumber())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .end(page.isLast())
                .build();
    }

    private boolean categoryNameExistsByName(String categoryName){
        return productCategoryRepository.existsByProductCategoryName(categoryName);
    }

    private boolean productCategoryExistsById(Long id){
        return productCategoryRepository.existsById(id);
    }

    private Optional<ProductCategoryEntity> getProductCategoryEntity(Long categoryId){
        if ( !productCategoryExistsById(categoryId) ) throw new RuntimeException("The category or subcategory does not exist.");
        return  productCategoryRepository.findById(categoryId);
    }
}
