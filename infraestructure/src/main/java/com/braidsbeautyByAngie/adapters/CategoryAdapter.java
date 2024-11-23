package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryPageable;
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
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
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

@Service
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryServiceOut {
    private static final Logger logger = LoggerFactory.getLogger(CategoryAdapter.class);

    private final ProductCategoryRepository productCategoryRepository;
    private final PromotionRepository promotionRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;

    /**
     * Creates a new category and associates promotions if provided.
     */
    @Override
    @Transactional
    public ProductCategoryDTO createCategoryOut(RequestCategory requestCategory) {
        logger.info("Attempting to create category: {}", requestCategory.getProductCategoryName());
        validateCategoryName(requestCategory.getProductCategoryName());

        ProductCategoryEntity categoryEntity = buildCategoryEntity(requestCategory);
        ProductCategoryEntity savedCategory = productCategoryRepository.save(categoryEntity);
        logger.info("Category created successfully: ID={}, Name={}", savedCategory.getProductCategoryId(), savedCategory.getProductCategoryName());

        return productCategoryMapper.mapCategoryEntityToDTO(savedCategory);
    }

    /**
     * Creates a subcategory linked to an existing parent category.
     */
    @Override
    public ProductCategoryDTO createSubCategoryOut(RequestSubCategory requestSubCategory) {
        logger.info("Attempting to create subcategory: {}", requestSubCategory.getProductSubCategoryName());
        validateCategoryName(requestSubCategory.getProductSubCategoryName());

        ProductCategoryEntity parentCategory = fetchCategoryById(requestSubCategory.getProductCategoryParentId());
        ProductCategoryEntity subCategoryEntity = buildSubCategoryEntity(requestSubCategory, parentCategory);
        ProductCategoryEntity savedSubCategory = productCategoryRepository.save(subCategoryEntity);

        logger.info("Subcategory created successfully: ID={}, Name={}", savedSubCategory.getProductCategoryId(), savedSubCategory.getProductCategoryName());
        return productCategoryMapper.mapCategoryEntityToDTO(savedSubCategory);
    }

    /**
     * Retrieves a category by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ResponseCategory> findCategoryByIdOut(Long categoryId) {
        logger.info("Fetching category by ID: {}", categoryId);
        ProductCategoryEntity categoryEntity = fetchCategoryById(categoryId);

        ResponseCategory responseCategory = buildResponseCategory(categoryEntity);
        logger.info("Category found: ID={}, Name={}", categoryEntity.getProductCategoryId(), categoryEntity.getProductCategoryName());
        return Optional.of(responseCategory);
    }

    /**
     * Updates an existing category's details.
     */
    @Override
    @Transactional
    public ProductCategoryDTO updateCategoryOut(RequestCategory requestCategory, Long categoryId) {
        logger.info("Updating category: ID={}", categoryId);
        ProductCategoryEntity existingCategory = fetchCategoryById(categoryId);

        updateCategoryEntity(existingCategory, requestCategory);
        ProductCategoryEntity updatedCategory = productCategoryRepository.save(existingCategory);

        logger.info("Category updated successfully: ID={}, Name={}", updatedCategory.getProductCategoryId(), updatedCategory.getProductCategoryName());
        return productCategoryMapper.mapCategoryEntityToDTO(updatedCategory);
    }

    /**
     * Soft deletes a category by updating its state.
     */
    @Override
    public ProductCategoryDTO deleteCategoryOut(Long categoryId) {
        logger.info("Deleting category: ID={}", categoryId);
        ProductCategoryEntity existingCategory = fetchCategoryById(categoryId);

        deactivateCategory(existingCategory);
        ProductCategoryEntity deletedCategory = productCategoryRepository.save(existingCategory);

        logger.info("Category deleted successfully: ID={}", deletedCategory.getProductCategoryId());
        return productCategoryMapper.mapCategoryEntityToDTO(deletedCategory);
    }

    /**
     * Lists all categories in a pageable format.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseListPageableCategory listCategoryPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        logger.info("Listing categories with pagination: page={}, size={}, orderBy={}, sortDir={}", pageNumber, pageSize, orderBy, sortDir);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ProductCategoryEntity> categoryPage = productCategoryRepository.findAllCategoriesPageableAndStatusTrue(pageable);
        List<ResponseCategoryPageable> categoryList = categoryPage.getContent().stream()
                .map(this::buildResponseCategoryPageable)
                .toList();

        logger.info("Categories retrieved: totalPages={}, totalElements={}", categoryPage.getTotalPages(), categoryPage.getTotalElements());
        return buildResponseListPageableCategory(categoryPage, categoryList);
    }

    /**
     * Lists all categories without pagination.
     */
    @Override
    public List<ProductCategoryDTO> listCategoryOut() {
        logger.info("Fetching all categories");
        return productCategoryRepository.findAll().stream()
                .map(productCategoryMapper::mapCategoryEntityToDTO)
                .toList();
    }

    // ---------- Private Helper Methods ----------

    private void validateCategoryName(String categoryName) {
        if (Boolean.TRUE.equals(productCategoryRepository.existsByProductCategoryName(categoryName))) {
            logger.error("Category name '{}' already exists", categoryName);
            throw new AppException("The name of the category already exists");
        }
    }

    private ProductCategoryEntity fetchCategoryById(Long categoryId) {
        return productCategoryRepository.findProductCategoryIdAndStateTrue(categoryId)
                .orElseThrow(() -> new AppExceptionNotFound("Category not found: ID=" + categoryId));
    }

    private ProductCategoryEntity buildCategoryEntity(RequestCategory requestCategory) {
        Set<PromotionEntity> promotions = requestCategory.getPromotionListId().isEmpty()
                ? new HashSet<>()
                : new HashSet<>(promotionRepository.findAllByPromotionIdAndStateTrue(requestCategory.getPromotionListId()));

        return ProductCategoryEntity.builder()
                .productCategoryName(requestCategory.getProductCategoryName())
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .promotionEntities(promotions)
                .modifiedByUser("prueba")
                .build();
    }

    private ProductCategoryEntity buildSubCategoryEntity(RequestSubCategory requestSubCategory, ProductCategoryEntity parentCategory) {
        return ProductCategoryEntity.builder()
                .parentCategory(parentCategory)
                .productCategoryName(requestSubCategory.getProductSubCategoryName())
                .createdAt(Constants.getTimestamp())
                .state(Constants.STATUS_ACTIVE)
                .modifiedByUser("prueba")
                .build();
    }

    private void updateCategoryEntity(ProductCategoryEntity categoryEntity, RequestCategory requestCategory) {
        Set<PromotionEntity> promotions = requestCategory.getPromotionListId().isEmpty()
                ? new HashSet<>()
                : new HashSet<>(promotionRepository.findAllByPromotionIdAndStateTrue(requestCategory.getPromotionListId()));

        categoryEntity.setProductCategoryName(requestCategory.getProductCategoryName());
        categoryEntity.setPromotionEntities(promotions);
    }

    private void deactivateCategory(ProductCategoryEntity categoryEntity) {
        categoryEntity.setState(Constants.STATUS_INACTIVE);
        categoryEntity.setDeletedAt(Constants.getTimestamp());
        categoryEntity.setModifiedByUser("PRUEBA");
    }

    private ResponseCategory buildResponseCategory(ProductCategoryEntity categoryEntity) {
        return ResponseCategory.builder()
                .productCategoryId(categoryEntity.getProductCategoryId())
                .productCategoryName(categoryEntity.getProductCategoryName())
                .productDTOList(productMapper.mapProductEntityListToDtoList(categoryEntity.getProductEntities()))
                .promotionDTOList(new HashSet<>(promotionMapper.mapPromotionListToDtoList(categoryEntity.getPromotionEntities())))
                .build();
    }

    private ResponseCategoryPageable buildResponseCategoryPageable(ProductCategoryEntity categoryEntity) {
        return ResponseCategoryPageable.builder()
                .productCategoryId(categoryEntity.getProductCategoryId())
                .productCategoryName(categoryEntity.getProductCategoryName())
                .productDTOList(productMapper.mapProductEntityListToDtoList(categoryEntity.getProductEntities()))
                .responseSubCategoryList(categoryEntity.getSubCategories().stream()
                        .map(subCategory -> ResponseSubCategory.builder()
                                .productCategoryId(subCategory.getProductCategoryId())
                                .productCategoryName(subCategory.getProductCategoryName())
                                .build())
                        .toList())
                .promotionDTOList(new HashSet<>(promotionMapper.mapPromotionListToDtoList(categoryEntity.getPromotionEntities())))
                .build();
    }

    private ResponseListPageableCategory buildResponseListPageableCategory(Page<ProductCategoryEntity> categoryPage, List<ResponseCategoryPageable> categoryList) {
        return ResponseListPageableCategory.builder()
                .pageNumber(categoryPage.getNumber())
                .end(categoryPage.isLast())
                .pageSize(categoryPage.getSize())
                .totalPages(categoryPage.getTotalPages())
                .totalElements(categoryPage.getTotalElements())
                .responseCategoryList(categoryList)
                .build();
    }
}
