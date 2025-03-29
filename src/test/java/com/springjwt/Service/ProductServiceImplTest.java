package com.springjwt.Service;

import com.springjwt.dto.ProductDTO;
import com.springjwt.entities.Product;
import com.springjwt.repositories.ProductRepository;
import com.springjwt.services.Product.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testCreateProduct_Success() {
        // Arrange
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Laptop");
        productDTO.setPrice(999.00);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setPrice(999.00);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDTO result = productService.createProduct(productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testGetAllProducts_Success() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(999.00);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(499.00);

        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(999.00, result.get(0).getPrice());
        assertEquals("Phone", result.get(1).getName());
        assertEquals(499.00, result.get(1).getPrice());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetProductById_Success() {
        // Arrange
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(999.00);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = productService.getProductById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    public void testGetProductById_NotFound() {
        // Arrange
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    public void testUpdateProduct_Success() {
        // Arrange
        Long id = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(id);
        existingProduct.setName("Old Laptop");
        existingProduct.setPrice(799.00);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("New Laptop");
        productDTO.setPrice(999.00);

        Product updatedProduct = new Product();
        updatedProduct.setId(id);
        updatedProduct.setName("New Laptop");
        updatedProduct.setPrice(999.00);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductDTO result = productService.updateProduct(id, productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("New Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() {
        // Arrange
        Long id = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("New Laptop");
        productDTO.setPrice(999.00);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(id, productDTO);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Success() {
        // Arrange
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(999.00);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(id);

        // Assert
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        // Arrange
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
