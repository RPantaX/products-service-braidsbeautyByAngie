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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryServiceOut {

    private final ProductCategoryRepository productCategoryRepository;
    private final PromotionRepository promotionRepository;

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;

    private static final Logger logger = LoggerFactory.getLogger(CategoryAdapter.class);

    @Override
    @Transactional
    public ProductCategoryDTO createCategoryOut(RequestCategory requestCategory) {

        logger.info("Creating category with name: {}", requestCategory.getProductCategoryName());
        if (categoryNameExistsByName(requestCategory.getProductCategoryName()) ) throw new RuntimeException("The name of the category already exists");


        ProductCategoryEntity productCategoryEntity = ProductCategoryEntity.builder()
                .productCategoryName(requestCategory.getProductCategoryName())
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("prueba")
                .build();
        //validation if promotions exists
        if (!requestCategory.getPromotionListId().isEmpty()) {
            Set<PromotionEntity> promotionEntitySet = new HashSet<>(promotionRepository.findAllByPromotionIdAndStateTrue(requestCategory.getPromotionListId()));
            productCategoryEntity.setPromotionEntities(promotionEntitySet);
        }
        ProductCategoryEntity productCategory = productCategoryRepository.save(productCategoryEntity);
        logger.info("Category '{}' created successfully with ID: {}",productCategory.getProductCategoryName(),productCategory.getProductCategoryId());
        return productCategoryMapper.mapCategoryEntityToDTO(productCategory);
    }

    @Override
    public ProductCategoryDTO createSubCategoryOut(RequestSubCategory requestSubCategory) {
        logger.info("Creating subcategory with name: {}", requestSubCategory.getProductSubCategoryName());
        if (categoryNameExistsByName(requestSubCategory.getProductSubCategoryName()) ) throw new RuntimeException("The name of the category already exists");
        Optional<ProductCategoryEntity> productCategoryParent = productCategoryRepository.findById(requestSubCategory.getProductCategoryParentId());
        ProductCategoryEntity productCategoryEntity = ProductCategoryEntity.builder()
                .parentCategory(productCategoryParent.get())
                .productCategoryName(requestSubCategory.getProductSubCategoryName())
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("prueba")
                .build();
        ProductCategoryEntity productSubCategorySaved = productCategoryRepository.save(productCategoryEntity);
        logger.info("SubCategory '{}' created successfully with ID: {}",productSubCategorySaved.getProductCategoryName(),productSubCategorySaved.getProductCategoryId());
        return productCategoryMapper.mapCategoryEntityToDTO(productSubCategorySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResponseCategory> findCategoryByIdOut(Long categoryId) {
        logger.info("Searching for Category with ID: {}", categoryId);
        Optional<ProductCategoryEntity> productCategoryEntity = getProductCategoryEntity(categoryId);

        ProductCategoryDTO productCategoryDTO = productCategoryMapper.mapCategoryEntityToDTO(productCategoryEntity.get());
        List<ProductDTO> productDTOList = productMapper.mapProductEntityListToDtoList(productCategoryEntity.get().getProductEntities());
        Set<PromotionDTO> promotionDTOList = new HashSet<>(promotionMapper.mapPromotionListToDtoList(productCategoryEntity.get().getPromotionEntities()));

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
        logger.info("Category with ID {} found", categoryId);
        return Optional.ofNullable(responseCategory);
    }

    @Override
    @Transactional
    public ProductCategoryDTO updateCategoryOut(RequestCategory requestCategory, Long categoryId) {
        logger.info("Searching for update category with ID: {}", categoryId);
        Optional<ProductCategoryEntity> productCategorySaved = getProductCategoryEntity(categoryId);
        Set<PromotionEntity> promotionEntitySet = new HashSet<>(promotionRepository.findAllByPromotionIdAndStateTrue(requestCategory.getPromotionListId()));
        productCategorySaved.get().setProductCategoryName(requestCategory.getProductCategoryName());
        productCategorySaved.get().setPromotionEntities(promotionEntitySet);

        ProductCategoryEntity productCategoryUpdated = productCategoryRepository.save(productCategorySaved.get());
        logger.info("Category updated with ID: {}",productCategoryUpdated.getProductCategoryId());
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryUpdated);
    }

    @Override
    public ProductCategoryDTO updateSubCategoryOut(RequestSubCategory requestSubCategory, Long categoryId) {
        logger.info("Searching for update subcategory with ID: {}", categoryId);
        Optional<ProductCategoryEntity> productSubCategory = getProductCategoryEntity(categoryId);
        productSubCategory.get().setProductCategoryName(requestSubCategory.getProductSubCategoryName());

        ProductCategoryEntity productCategoryUpdated = productCategoryRepository.save(productSubCategory.get());
        logger.info("subcategory updated with ID: {}", productCategoryUpdated.getProductCategoryId());

        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryUpdated);
    }

    @Override
    public ProductCategoryDTO deleteCategoryOut(Long categoryId) {
        logger.info("Searching category for delete with ID: {}", categoryId);
        Optional<ProductCategoryEntity> productCategorySaved = getProductCategoryEntity(categoryId);
        productCategorySaved.get().setState(Constants.STATUS_INACTIVE);
        productCategorySaved.get().setModifiedByUser("PRUEBA");
        productCategorySaved.get().setDeletedAt(Constants.getTimestamp());

        ProductCategoryEntity productCategoryDeleted = productCategoryRepository.save(productCategorySaved.get());
        logger.info("Category deleted with ID: {}", categoryId);
        return productCategoryMapper.mapCategoryEntityToDTO(productCategoryDeleted);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseListPageableCategory listCategoryPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        logger.info("Searching all categories with the following parameters: {}", Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));

        if (productCategoryRepository.findAll().isEmpty()) return null;
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<ProductCategoryEntity> page = productCategoryRepository.findAllCategoriesPageableAndStatusTrue(pageable);

        // Mapear cada ProductCategoryEntity a un ResponseCategory, incluyendo ProductDTO y PromotionDTO
        List<ResponseCategory> responseCategoryList = page.getContent().stream()
                .map(productCategoryEntity -> {
                    // Convertir la entidad de categoría en un DTO
                    ProductCategoryDTO productCategoryDTO = productCategoryMapper.mapCategoryEntityToDTO(productCategoryEntity);

                    // Convertir las entidades de producto y promoción a DTOs
                    List<ProductDTO> productDTOList = productMapper.mapProductEntityListToDtoList(productCategoryEntity.getProductEntities());
                    List<PromotionDTO> promotionDTOList = promotionMapper.mapPromotionListToDtoList(productCategoryEntity.getPromotionEntities());

                    // Mapear subcategorías a ResponseSubCategory
                    List<ResponseSubCategory> responseSubCategoryList = productCategoryEntity.getSubCategories().stream()
                            .map(subCategoryEntity -> ResponseSubCategory.builder()
                                    .productCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(subCategoryEntity))
                                    .build())
                            .collect(Collectors.toList());

                    // Crear y retornar el ResponseCategory
                    return ResponseCategory.builder()
                            .productCategoryDTO(productCategoryDTO)
                            .productDTOList(productDTOList)
                            .responseSubCategoryList(responseSubCategoryList)
                            .promotionDTOList(promotionDTOList.stream().collect(Collectors.toSet()))
                            .build();
                })
                .collect(Collectors.toList());
        logger.info("Categories found with the following parameters: {}", Constants.parametersForLogger(pageNumber, pageSize, orderBy, sortDir));
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
        return productCategoryRepository.existByProductCategoryIdAndStateTrue(id);
    }

    private Optional<ProductCategoryEntity> getProductCategoryEntity(Long categoryId){
        if ( !productCategoryExistsById(categoryId) ) throw new RuntimeException("The category or subcategory does not exist.");
        return  productCategoryRepository.findProductCategoryIdAndStateTrue(categoryId);
    }
}
