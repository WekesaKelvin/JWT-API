package com.springjwt.Service;

import com.springjwt.dto.ProductDTO;
import com.springjwt.entities.Product;
import com.springjwt.entities.User;
import com.springjwt.repositories.ProductRepository;
import com.springjwt.repositories.UserRepository;
import com.springjwt.services.Product.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole("ROLE_USER");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(mockUser.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findFirstByEmail(mockUser.getEmail())).thenReturn(mockUser);
    }

    @Test
    public void testCreateProduct_Success() {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Laptop");
        productDTO.setPrice(999.00);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setPrice(999.00);
        savedProduct.setCreatedBy(mockUser);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));

        String expectedMessage = String.format("Created product: %s by %s ", savedProduct.getName(), mockUser.getEmail());
        verify(jmsTemplate, times(1)).convertAndSend(eq("product-queue"), eq(expectedMessage));
    }

    @Test
    public void testGetAllProducts_Success() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(999.00);
        product1.setCreatedBy(mockUser);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(499.00);
        product2.setCreatedBy(mockUser);

        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findByCreatedBy(mockUser)).thenReturn(products);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(999.00, result.get(0).getPrice());
        assertEquals("Phone", result.get(1).getName());
        assertEquals(499.00, result.get(1).getPrice());
        verify(productRepository, times(1)).findByCreatedBy(mockUser);
        verify(productRepository, never()).findAll(); // Non-admin case
    }

    @Test
    public void testGetAllProducts_Admin_Success() {
        mockUser.setRole("ROLE_ADMIN"); // Switch to admin role

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(999.00);
        product1.setCreatedBy(mockUser);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Phone");
        product2.setPrice(499.00);
        product2.setCreatedBy(new User()); // Different user

        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
        verify(productRepository, never()).findByCreatedBy(any());
    }

    @Test
    public void testGetProductById_Success() {
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(999.00);
        product.setCreatedBy(mockUser);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    public void testGetProductById_NotFound() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    public void testUpdateProduct_Success() {
        Long id = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(id);
        existingProduct.setName("Old Laptop");
        existingProduct.setPrice(799.00);
        existingProduct.setCreatedBy(mockUser);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("New Laptop");
        productDTO.setPrice(999.00);

        Product updatedProduct = new Product();
        updatedProduct.setId(id);
        updatedProduct.setName("New Laptop");
        updatedProduct.setPrice(999.00);
        updatedProduct.setCreatedBy(mockUser);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO result = productService.updateProduct(id, productDTO);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("New Laptop", result.getName());
        assertEquals(999.00, result.getPrice());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() {
        Long id = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("New Laptop");
        productDTO.setPrice(999.00);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(id, productDTO);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Success() {
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(999.00);
        product.setCreatedBy(mockUser);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        productService.deleteProduct(id);

        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(id);
        });
        assertEquals("Product not found with id: " + id, exception.getMessage());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).delete(any(Product.class));
    }

}