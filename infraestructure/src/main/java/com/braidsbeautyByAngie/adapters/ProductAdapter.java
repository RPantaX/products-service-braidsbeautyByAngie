package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.ports.out.ProductServiceOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductAdapter implements ProductServiceOut {
    @Override
    public ProductDTO createProductOut(RequestProduct requestProduct) {
        return null;
    }

    @Override
    public Optional<ResponseProduct> findProductByIdOut(String productId) {
        return Optional.empty();
    }

    @Override
    public ProductDTO updateProductOut(String productId, RequestProduct requestProduct) {
        return null;
    }

    @Override
    public ProductDTO deleteProductOut(String productId) {
        return null;
    }

    @Override
    public ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        return null;
    }
}
