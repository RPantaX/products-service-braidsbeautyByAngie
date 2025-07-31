package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.ports.in.ProductServiceIn;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
/*
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Pure Unit Tests")
class ProductControllerUnitTest {

    @Mock
    private ProductServiceIn productServiceIn;

    @InjectMocks
    private ProductController productController;

    private RequestProduct requestProduct;
    private ProductDTO productDTO;
    private ResponseProduct responseProduct;
    private ResponseListPageableProduct responseListPageableProduct;

    @BeforeEach
    void setUp() {
        // Setup request product
        requestProduct = RequestProduct.builder()
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                //.productImage("iphone15.jpg")
                .productCategoryId(1L)
                .build();

        // Setup product DTO
        productDTO = ProductDTO.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                .productImage("iphone15.jpg")
                .build();

        // Setup response product
        responseProduct = ResponseProduct.builder()
                .productId(1L)
                .productName("iPhone 15")
                .productDescription("Latest iPhone model")
                .productImage("iphone15.jpg")
                .build();

        // Setup pageable response
        responseListPageableProduct = ResponseListPageableProduct.builder()
                .responseProductList(Arrays.asList(responseProduct))
                .pageNumber(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .end(true)
                .build();
    }

    @Test
    @DisplayName("Should list products with pagination successfully")
    void listProductPageableList_WithValidParameters_ShouldReturnProductList() {
        // Given
        when(productServiceIn.listProductPageableIn(0, 10, "createdAt", "0"))
                .thenReturn(responseListPageableProduct);

        // When
        ResponseEntity<ApiResponse> response = productController.listProductPageableList(0, 10, "createdAt", "0");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("List of products retrieved successfully", response.getBody().getMessage());
        assertEquals("200", response.getBody().getCode());
        assertNotNull(response.getBody().getData());

        // Verify service interaction
        verify(productServiceIn).listProductPageableIn(0, 10, "createdAt", "0");
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void listProductById_WithValidId_ShouldReturnProduct() {
        // Given
        when(productServiceIn.findProductByIdIn(1L)).thenReturn(responseProduct);

        // When
        ResponseEntity<ApiResponse> response = productController.listProductById(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("Product retrieved successfully", response.getBody().getMessage());
        assertEquals("200", response.getBody().getCode());
        assertEquals(responseProduct, response.getBody().getData());

        verify(productServiceIn).findProductByIdIn(1L);
    }

    @Test
    @DisplayName("Should create product successfully")
    void saveProduct_WithValidData_ShouldCreateProduct() {
        // Given
        when(productServiceIn.createProductIn(any(RequestProduct.class))).thenReturn(productDTO);

        // When
        ResponseEntity<ApiResponse> response = productController.saveProduct(requestProduct);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("product saved", response.getBody().getMessage());
        assertEquals("201", response.getBody().getCode());
        assertEquals(productDTO, response.getBody().getData());

        verify(productServiceIn).createProductIn(requestProduct);
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_WithValidData_ShouldUpdateProduct() {
        // Given
        when(productServiceIn.updateProductIn(eq(1L), any(RequestProduct.class))).thenReturn(productDTO);

        // When
        ResponseEntity<ApiResponse> response = productController.updateProduct(1L, requestProduct);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("Product updated", response.getBody().getMessage());
        assertEquals("201", response.getBody().getCode());
        assertEquals(productDTO, response.getBody().getData());

        verify(productServiceIn).updateProductIn(1L, requestProduct);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_WithValidId_ShouldDeleteProduct() {
        // Given
        when(productServiceIn.deleteProductIn(1L)).thenReturn(productDTO);

        // When
        ResponseEntity<ApiResponse> response = productController.deleteProduct(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("Product deleted", response.getBody().getMessage());
        assertEquals("200", response.getBody().getCode());
        assertEquals(productDTO, response.getBody().getData());

        verify(productServiceIn).deleteProductIn(1L);
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void listProductById_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        when(productServiceIn.findProductByIdIn(999L))
                .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.listProductById(999L);
        });

        verify(productServiceIn).findProductByIdIn(999L);
    }

    @Test
    @DisplayName("Should call service with correct default parameters")
    void listProductPageableList_WithDefaultParameters_ShouldUseDefaults() {
        // Given
        when(productServiceIn.listProductPageableIn(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(responseListPageableProduct);

        // When - Using default values that would come from @RequestParam defaults
        ResponseEntity<ApiResponse> response = productController.listProductPageableList(0, 10, "createdAt", "0");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(productServiceIn).listProductPageableIn(0, 10, "createdAt", "0");
    }

    @Test
    @DisplayName("Should handle null response from service")
    void listProductById_WhenServiceReturnsNull_ShouldHandleGracefully() {
        // Given
        when(productServiceIn.findProductByIdIn(1L)).thenReturn(null);

        // When
        ResponseEntity<ApiResponse> response = productController.listProductById(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getError());
        assertEquals("Product retrieved successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());

        verify(productServiceIn).findProductByIdIn(1L);
    }

    @Test
    @DisplayName("Should create ApiResponse with correct structure for list operation")
    void listProductPageableList_ShouldReturnCorrectApiResponseStructure() {
        // Given
        when(productServiceIn.listProductPageableIn(0, 10, "createdAt", "0"))
                .thenReturn(responseListPageableProduct);

        // When
        ResponseEntity<ApiResponse> response = productController.listProductPageableList(0, 10, "createdAt", "0");

        // Then
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.getError());
        assertEquals("List of products retrieved successfully", apiResponse.getMessage());
        assertEquals("200", apiResponse.getCode());
        assertNotNull(apiResponse.getData());
        assertNotNull(apiResponse.getDate());

        // Verify the data is the expected type
        assertTrue(apiResponse.getData() instanceof ResponseListPageableProduct);
        ResponseListPageableProduct data = (ResponseListPageableProduct) apiResponse.getData();
        assertEquals(1, data.getResponseProductList().size());
        assertEquals("iPhone 15", data.getResponseProductList().get(0).getProductName());
    }

    @Test
    @DisplayName("Should create ApiResponse with correct structure for create operation")
    void saveProduct_ShouldReturnCorrectApiResponseStructure() {
        // Given
        when(productServiceIn.createProductIn(any(RequestProduct.class))).thenReturn(productDTO);

        // When
        ResponseEntity<ApiResponse> response = productController.saveProduct(requestProduct);

        // Then
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.getError());
        assertEquals("product saved", apiResponse.getMessage());
        assertEquals("201", apiResponse.getCode());
        assertNotNull(apiResponse.getData());
        assertNotNull(apiResponse.getDate());

        // Verify the data is the expected type
        assertTrue(apiResponse.getData() instanceof ProductDTO);
        ProductDTO data = (ProductDTO) apiResponse.getData();
        assertEquals(1L, data.getProductId());
        assertEquals("iPhone 15", data.getProductName());
    }
}*/