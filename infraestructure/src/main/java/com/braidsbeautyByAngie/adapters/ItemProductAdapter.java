package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestVariationName;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseSubCategory;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseVariation;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseVariationFinal;
import com.braidsbeautyByAngie.entity.*;
import com.braidsbeautyByAngie.mapper.*;
import com.braidsbeautyByAngie.ports.out.ItemProductServiceOut;
import com.braidsbeautyByAngie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemProductAdapter implements ItemProductServiceOut {
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final PromotionMapper promotionMapper;
    private final VariationMapper variationMapper;
    private final VariationOptionMapper variationOptionMapper;
    private final ProductItemMapper productItemMapper;

    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;
    private final VariationRepository variationRepository;
    private final VariationOptionRepository variationOptionRepository;


    @Transactional
    @Override
    public ProductItemDTO createItemProductOut(RequestItemProduct requestItemProduct) {
        if (productItemExistsBySKU(requestItemProduct.getProductItemSKU())) throw new RuntimeException("The sku already exists.");
        if(!productRepository.existsById(requestItemProduct.getProductId())) throw new RuntimeException("The product doesn't  exists.");
        ProductEntity productEntity = productRepository.findById(requestItemProduct.getProductId()).get();
        //varation
        Set<VariationOptionEntity> variationEntitiesSaved = saveVariations(requestItemProduct.getVariationNames());

        ProductItemEntity productItemEntity1 = ProductItemEntity.builder()
                .variationOptionEntitySet(variationEntitiesSaved)
                .productEntity(productEntity)
                .productItemSKU(requestItemProduct.getProductItemSKU())
                .productItemImage(requestItemProduct.getProductItemImage())
                .productItemPrice(requestItemProduct.getProductItemPrice())
                .productItemQuantityInStock(requestItemProduct.getProductItemQuantityInStock())
                .createdAt(Constants.getTimestamp())
                .modifiedByUser("TEST")
                .state(Constants.STATUS_ACTIVE)
                .build();
        return productItemMapper.mapProductItemEntityToDto(productItemRepository.save(productItemEntity1));
    }

    @Override
    public Optional<ResponseItemProduct> findItemProductByIdOut(Long itemProductId) {
        ProductItemEntity productItemEntity = getProductItemById(itemProductId).get();

        // Construir subcategorías y promociones para la categoría del producto
        List<ResponseSubCategory> subCategoryList = productItemEntity.getProductEntity().getProductCategoryEntity().getSubCategories()
                .stream()
                .map(subCat -> {
                    ResponseSubCategory responseSubCategory = new ResponseSubCategory();
                    responseSubCategory.setProductCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(subCat));
                    return responseSubCategory;
                }).toList();

        List<PromotionDTO> promotionDTOList = promotionMapper.mapPromotionListToDtoList(
                productItemEntity.getProductEntity().getProductCategoryEntity().getPromotionEntities());

        ResponseCategory responseCategory = ResponseCategory.builder()
                .responseSubCategoryList(subCategoryList)
                .productCategoryDTO(productCategoryMapper.mapCategoryEntityToDTO(productItemEntity.getProductEntity().getProductCategoryEntity()))
                .promotionDTOList(Set.copyOf(promotionDTOList))
                .build();

        // Construir opciones de variación y variaciones finales para el producto
        Set<VariationOptionEntity> variationOptionEntitySet = productItemEntity.getVariationOptionEntitySet();

        // Agrupar opciones de variación por cada variación
        Map<VariationEntity, List<VariationOptionEntity>> groupedVariations = variationOptionEntitySet.stream()
                .collect(Collectors.groupingBy(VariationOptionEntity::getVariationEntity));

        // Convertir cada grupo en una ResponseVariation con sus ResponseVariationFinals
        List<ResponseVariation> responseVariationList = groupedVariations.entrySet().stream()
                .map(entry -> {
                    VariationEntity variationEntity = entry.getKey();
                    List<VariationOptionEntity> optionEntities = entry.getValue();

                    // Mapear cada VariationOptionEntity a un ResponseVariationFinal
                    List<ResponseVariationFinal> responseVariationFinalList = optionEntities.stream()
                            .map(optionEntity -> ResponseVariationFinal.builder()
                                    .variationDTO(variationMapper.mapVariationEntityToDto(variationEntity))
                                    .variationOptionDTOList(List.of(variationOptionMapper.mapVariationOptionEntityToDto(optionEntity)))
                                    .build())
                            .toList();

                    // Crear ResponseVariation con la lista de ResponseVariationFinals
                    return ResponseVariation.builder()
                            .responseVariationFinals(responseVariationFinalList)
                            .build();
                })
                .toList();

        ResponseItemProduct responseItemProduct = ResponseItemProduct.builder()
                .productDTO(productMapper.mapProductEntityToDto(productItemEntity.getProductEntity()))
                .responseVariationList(responseVariationList)
                .responseCategory(responseCategory)
                .build();
        return Optional.ofNullable(responseItemProduct);
    }
    @Transactional
    @Override
    public ProductItemDTO updateItemProductOut(Long itemProductId, RequestItemProduct requestItemProduct) {
        //todo: mejorar la actualizacion.
        if(!productRepository.existsById(itemProductId)) throw new RuntimeException("The product doesn't  exists.");
        //varation
        Set<VariationOptionEntity> variationEntitiesSaved = updateVariations(requestItemProduct.getVariationNames());

        ProductItemEntity productItemEntity1 = ProductItemEntity.builder()
                .variationOptionEntitySet(variationEntitiesSaved)
                .productItemSKU(requestItemProduct.getProductItemSKU())
                .productItemImage(requestItemProduct.getProductItemImage())
                .productItemPrice(requestItemProduct.getProductItemPrice())
                .productItemQuantityInStock(requestItemProduct.getProductItemQuantityInStock())
                .modifiedAt(Constants.getTimestamp())
                .modifiedByUser("TEST")
                .build();

        return productItemMapper.mapProductItemEntityToDto(productItemEntity1);
    }

    @Override
    public ProductItemDTO deleteItemProductOut(Long itemProductId) {
        Optional<ProductItemEntity> productItemEntityOptional = getProductItemById(itemProductId);
        productItemEntityOptional.get().setDeletedAt(Constants.getTimestamp());
        productItemEntityOptional.get().setState(Constants.STATUS_INACTIVE);
        productItemEntityOptional.get().setModifiedByUser("TEST");

        return productItemMapper.mapProductItemEntityToDto(productItemRepository.save(productItemEntityOptional.get()));
    }
    private boolean itemProductExistsById(Long itemProductId) {
        return productItemRepository.existsById(itemProductId);
    }
    private Optional<ProductItemEntity> getProductItemById(Long itemProductId) {
        if (!itemProductExistsById(itemProductId)) throw new RuntimeException("The itemProduct does not exist.");
        return productItemRepository.findById(itemProductId);
    }
    private boolean productItemExistsBySKU(String sku) {
        return productItemRepository.existsByProductItemSKU(sku);
    }
    private Set<VariationOptionEntity> saveVariations(List<RequestVariationName> requestVariationNames) {
        return requestVariationNames.stream()
                .flatMap(variationName -> {
                    // Crear entidad de variación
                    VariationEntity variationEntity = VariationEntity.builder()
                            .variationName(variationName.getVariationName())
                            .state(Constants.STATUS_ACTIVE)
                            .createdAt(Constants.getTimestamp())
                            .modifiedByUser("TEST")
                            .build();

                    // Crear entidades de opción de variación y asociarlas a la variación
                    List<VariationOptionEntity> variationOptionEntities = variationName.getVariationOptionValues().stream()
                            .map(optionValue -> VariationOptionEntity.builder()
                                    .variationOptionValue(optionValue)
                                    .state(Constants.STATUS_ACTIVE)
                                    .createdAt(Constants.getTimestamp())
                                    .modifiedByUser("TEST")
                                    .build())
                            .collect(Collectors.toList());

                    // Guardar las opciones de variación y la variación
                    variationEntity.setVariationOptionEntities(variationOptionRepository.saveAll(variationOptionEntities));
                    variationRepository.save(variationEntity);

                    // Retornar el stream de entidades de opción de variación
                    return variationOptionEntities.stream();
                })
                .collect(Collectors.toSet());
    }
    private Set<VariationOptionEntity> updateVariations(List<RequestVariationName> requestVariationNames) {
        return requestVariationNames.stream()
                .flatMap(variationName -> {
                    // Crear entidad de variación
                    VariationEntity variationEntity = VariationEntity.builder()
                            .variationName(variationName.getVariationName())
                            .state(Constants.STATUS_ACTIVE)
                            .modifiedAt(Constants.getTimestamp())
                            .modifiedByUser("TEST")
                            .build();

                    // Crear entidades de opción de variación y asociarlas a la variación
                    List<VariationOptionEntity> variationOptionEntities = variationName.getVariationOptionValues().stream()
                            .map(optionValue -> {
                                if(variationOptionRepository.existsByVariationOptionValue(optionValue)){
                                    return VariationOptionEntity.builder()
                                            .variationOptionValue(optionValue)
                                            .state(Constants.STATUS_ACTIVE)
                                            .modifiedAt(Constants.getTimestamp())
                                            .modifiedByUser("TEST")
                                            .build();
                                }
                                return  VariationOptionEntity.builder()
                                        .variationOptionValue(optionValue)
                                        .state(Constants.STATUS_ACTIVE)
                                        .createdAt(Constants.getTimestamp())
                                        .modifiedByUser("TEST")
                                        .build();
                            })
                            .collect(Collectors.toList());

                    // Guardar las opciones de variación y la variación
                    variationEntity.setVariationOptionEntities(variationOptionRepository.saveAll(variationOptionEntities));
                    variationRepository.save(variationEntity);

                    // Retornar el stream de entidades de opción de variación
                    return variationOptionEntities.stream();
                })
                .collect(Collectors.toSet());
    }
}
