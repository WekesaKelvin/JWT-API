package com.springjwt.services.Product;

import com.springjwt.dto.ProductDTO;
import com.springjwt.entities.Product;
import com.springjwt.entities.User;
import com.springjwt.repositories.ProductRepository;
import com.springjwt.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        User currentUser = getCurrentUser();
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setCreatedBy(currentUser);
        Product savedProduct = productRepository.save(product);

        String message = String.format("Created product: %s by %s ", savedProduct.getName(), currentUser.getEmail());
        jmsTemplate.convertAndSend("product-queue", message);
        logger.info("Sent message to product-queue: {}", message);

        return mapToDTO(savedProduct);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        User currentUser = getCurrentUser();
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        return productRepository.findByCreatedBy(currentUser).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        User currentUser = getCurrentUser();
        if (!"ROLE_ADMIN".equals(currentUser.getRole()) && !product.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only view your own products");
        }
        return mapToDTO(product);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        User currentUser = getCurrentUser();
        if (!"ROLE_ADMIN".equals(currentUser.getRole()) && !product.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only edit your own products");
        }
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        User currentUser = getCurrentUser();
        if (!"ROLE_ADMIN".equals(currentUser.getRole()) && !product.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only delete your own products");
        }
        productRepository.delete(product);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        return dto;
    }
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findFirstByEmail(email);
    }
}