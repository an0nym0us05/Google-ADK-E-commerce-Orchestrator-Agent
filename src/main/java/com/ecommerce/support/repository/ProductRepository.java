package com.ecommerce.support.repository;

import com.ecommerce.support.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(String productId);
    List<Product> findAll();
    List<Product> searchByKeyword(String keyword);
}
