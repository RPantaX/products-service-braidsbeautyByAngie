package com.braidsbeautyByAngie.aggregates.constants;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.TypeException;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.GenericError;

public enum ProductsErrorEnum implements GenericError {
    // General Errors
    //PRODUCTS
    PRODUCT_NOT_FOUND_ERP00001("ERP00001", "Product Not Found", "The requested product does not exist.", TypeException.E),
    PRODUCT_ALREADY_EXISTS_ERP00002("ERP00002", "Product Already Exists", "A product with the same name already exists.", TypeException.E),
    PRODUCT_CREATION_FAILED_ERP00003("ERP00003", "Product Creation Failed", "Failed to create the product due to an internal error.", TypeException.E),
    PRODUCT_UPDATE_FAILED_ERP00004("ERP00004", "Product Update Failed", "Failed to update the product due to an internal error.", TypeException.E),
    PRODUCT_DELETION_FAILED_ERP00005("ERP00005", "Product Deletion Failed", "Failed to delete the product due to an internal error.", TypeException.E),
    PRODUCT_LISTING_FAILED_ERP00006("ERP00006", "Product Listing Failed", "Failed to retrieve the list of products due to an internal error.", TypeException.E),
    PRODUCT_INVALID_DATA_ERP00007("ERP00007", "Invalid Product Data", "The provided product data is invalid or incomplete.", TypeException.E),

    //ITEM PRODUCTS
    ITEM_PRODUCT_NOT_FOUND_ERI00001("ERI00001", "Item Product Not Found", "The requested item product does not exist.", TypeException.E),
    ITEM_PRODUCT_ALREADY_EXISTS_ERI00002("ERI00002", "Item Product Already Exists", "An item product with the same identifier already exists.", TypeException.E),
    ITEM_PRODUCT_CREATION_FAILED_ERI00003("ERI00003", "Item Product Creation Failed", "Failed to create the item product due to an internal error.", TypeException.E),
    ITEM_PRODUCT_UPDATE_FAILED_ERI00004("ERI00004", "Item Product Update Failed", "Failed to update the item product due to an internal error.", TypeException.E),
    ITEM_PRODUCT_DELETION_FAILED_ERI00005("ERI00005", "Item Product Deletion Failed", "Failed to delete the item product due to an internal error.", TypeException.E),
    ITEM_PRODUCT_LISTING_FAILED_ERI00006("ERI00006", "Item Product Listing Failed", "Failed to retrieve the list of item products due to an internal error.", TypeException.E),
    ITEM_PRODUCT_INVALID_DATA_ERI00007("ERI00007", "Invalid Item Product Data", "The provided item product data is invalid or incomplete.", TypeException.E),

    //VARIATIONS
    VARIATION_NOT_FOUND_ERP00029("ERV00029", "Variation Not Found", "The requested variation does not exist.", TypeException.E),
    VARIATION_ALREADY_EXISTS_ERP00030("ERV00030", "Variation Already Exists", "A variation with the same identifier already exists.", TypeException.E),
    VARIATION_CREATION_FAILED_ERP00031("ERV00031", "Variation Creation Failed", "Failed to create the variation due to an internal error.", TypeException.E),
    VARIATION_UPDATE_FAILED_ERP00032("ERV00032", "Variation Update Failed", "Failed to update the variation due to an internal error.", TypeException.E),
    VARIATION_DELETION_FAILED_ERP00033("ERV00033", "Variation Deletion Failed", "Failed to delete the variation due to an internal error.", TypeException.E),
    VARIATION_LISTING_FAILED_ERP00034("ERV00034", "Variation Listing Failed", "Failed to retrieve the list of variations due to an internal error.", TypeException.E),
    VARIATION_INVALID_DATA_ERP00035("ERV00035", "Invalid Variation Data", "The provided variation data is invalid or incomplete.", TypeException.E),

    //VARIATION OPTIONS
    VARIATION_OPTION_NOT_FOUND_ERP00036("ERVO00036", "Variation Option Not Found", "The requested variation option does not exist.", TypeException.E),
    VARIATION_OPTION_ALREADY_EXISTS_ERP00037("ERVO00037", "Variation Option Already Exists", "A variation option with the same identifier already exists.", TypeException.E),
    VARIATION_OPTION_CREATION_FAILED_ERP00038("ERVO00038", "Variation Option Creation Failed", "Failed to create the variation option due to an internal error.", TypeException.E),
    VARIATION_OPTION_UPDATE_FAILED_ERP00039("ERVO0039", "Variation Option Update Failed", "Failed to update the variation option due to an internal error.", TypeException.E),
    VARIATION_OPTION_DELETION_FAILED_ERP00040("ERVO0040", "Variation Option Deletion Failed", "Failed to delete the variation option due to an internal error.", TypeException.E),
    VARIATION_OPTION_LISTING_FAILED_ERP00041("ERVO0041", "Variation Option Listing Failed", "Failed to retrieve the list of variation options due to an internal error.", TypeException.E),
    VARIATION_OPTION_INVALID_DATA_ERP00042("ERVO0042", "Invalid Variation Option Data", "The provided variation option data is invalid or incomplete.", TypeException.E),



    //WARNING Errors
    //PRODUCTS

    PRODUCT_NAME_REQUIRED_WI00001("WI00001", "Product Name Required", "The product name is required and cannot be empty.", TypeException.W),
    PRODUCT_DESCRIPTION_REQUIRED_WI00002("WI00002", "Product Description Required", "The product description is required and cannot be empty.", TypeException.W),
    //ITEM PRODUCTS

    ITEM_PRODUCT_ITEM_SKU_REQUIRED_WI00003("WI00003", "Item Product SKU Required", "The item product SKU is required and cannot be empty.", TypeException.W),
    ITEM_PRODUCT_ITEM_QUANTITY_REQUIRED_WI00004("WI00004", "Item Product Quantity Required", "The item product quantity is required and cannot be empty.", TypeException.W),
    ITEM_PRODUCT_ITEM_PRICE_REQUIRED_WI00005("WI00005", "Item Product Price Required", "The item product price is required and cannot be empty.", TypeException.W),

    //VARIATIONS
    VARIATION_NAME_REQUIRED_WV00043("WV00043", "Variation Name Required", "The variation name is required and cannot be empty.", TypeException.W),
    VARIATION_REQUIRED_WV00045("WV00045", "Variation Required", "The variation is required and cannot be empty.", TypeException.W),
    VARIATION_OPTIONS_REQUIRED_WV00044("WV00044", "Variation Options Required", "At least one variation option is required.", TypeException.W),

       ;
    private ProductsErrorEnum(String code, String title, String message, TypeException type) {
        this.code = code;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    private final String code;
    private final String title;
    private final String message;
    private final TypeException type;


    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public TypeException getType() {
        return type;
    }
}
