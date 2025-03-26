package com.springjwt.Controller;

import com.springjwt.controllers.ProductController;
import com.springjwt.dto.ProductDTO;
import com.springjwt.services.Product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testCreateProduct_Success() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Laptop");
        productDTO.setPrice(999.00);

        ProductDTO createdProduct = new ProductDTO();
        createdProduct.setId(1L);
        createdProduct.setName("Laptop");
        createdProduct.setPrice(999.00);

        when(productService.createProduct(productDTO)).thenReturn(createdProduct);

        ResponseEntity<ProductDTO> response = productController.createProduct(productDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdProduct, response.getBody());
        verify(productService, times(1)).createProduct(productDTO);
    }

    @Test
    public void testGetAllProducts_Success() {
        // Arrange
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(999.00);

        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(499.00);

        List<ProductDTO> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(products, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testGetProductById_Success() {

        Long id = 1L;
        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(999.00);

        when(productService.getProductById(id)).thenReturn(product);

        ResponseEntity<ProductDTO> response = productController.getProductById(id);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(product, response.getBody());
        verify(productService, times(1)).getProductById(id);
    }

    @Test
    public void testGetProductById_NotFound() {

        Long id = 1L;
        when(productService.getProductById(id)).thenThrow(new RuntimeException("Product not found with id: " + id));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productController.getProductById(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productService, times(1)).getProductById(id);
    }

    @Test
    public void testUpdateProduct_Success() {

        Long id = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Laptop");
        productDTO.setPrice(1299.00);

        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setId(id);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setPrice(1299.00);

        when(productService.updateProduct(id, productDTO)).thenReturn(updatedProduct);

        ResponseEntity<ProductDTO> response = productController.updateProduct(id, productDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProduct, response.getBody());
        verify(productService, times(1)).updateProduct(id, productDTO);
    }

    @Test
    public void testUpdateProduct_NotFound() {

        Long id = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Laptop");
        productDTO.setPrice(1299.00);

        when(productService.updateProduct(id, productDTO))
                .thenThrow(new RuntimeException("Product not found with id: " + id));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productController.updateProduct(id, productDTO);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productService, times(1)).updateProduct(id, productDTO);
    }

    @Test
    public void testDeleteProduct_Success() {

        Long id = 1L;
        doNothing().when(productService).deleteProduct(id);

        ResponseEntity<Void> response = productController.deleteProduct(id);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService, times(1)).deleteProduct(id);
    }

    @Test
    public void testDeleteProduct_NotFound() {

        Long id = 1L;
        doThrow(new RuntimeException("Product not found with id: " + id))
                .when(productService).deleteProduct(id);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productController.deleteProduct(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productService, times(1)).deleteProduct(id);
    }
}