package com.braidsbeautyByAngie.impl;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProductFilterOptions;
import com.braidsbeautyByAngie.ports.in.ProductServiceIn;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductServiceIn {

    private final ProductServiceOut productServiceOut;

    @Override
    public ProductDTO createProductIn(RequestProduct requestProduct) {
        return productServiceOut.createProductOut(requestProduct);
    }

    @Override
    public ResponseProduct findProductByIdIn(Long productId) {
        return productServiceOut.findProductByIdOut(productId);
    }

    @Override
    public ProductDTO updateProductIn(Long productId, RequestProduct requestProduct) {
        return productServiceOut.updateProductOut(productId, requestProduct);
    }

    @Override
    public ProductDTO deleteProductIn(Long productId) {
        return productServiceOut.deleteProductOut(productId);
    }

    @Override
    public ResponseListPageableProduct listProductPageableIn(int pageNumber, int pageSize, String orderBy, String sortDir) {
        return productServiceOut.listProductPageableOut(pageNumber, pageSize, orderBy, sortDir);
    }

    @Override
    public ResponseListPageableProduct filterProductsIn(RequestProductFilter filter) {
        return productServiceOut.filterProductsOut(filter);
    }

    @Override
    public ResponseProductFilterOptions getProductFilterOptionsIn() {
        return productServiceOut.getProductFilterOptionsOut();
    }

}
