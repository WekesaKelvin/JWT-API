package com.springjwt.repositories;

import com.springjwt.entities.Product;
import com.springjwt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCreatedBy(User createdBy);
}
