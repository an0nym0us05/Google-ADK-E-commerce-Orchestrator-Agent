package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Product;
import com.ecommerce.support.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

public class MockProductRepository implements ProductRepository {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public MockProductRepository() {
        seed();
    }

    private void seed() {
        List.of(
            new Product("PROD-001", "Wireless Headphones Pro", "Premium noise-cancelling wireless headphones", 89.99, 15, "Electronics"),
            new Product("PROD-002", "USB-C Cable 2m", "Braided USB-C to USB-C fast charging cable", 12.99, 100, "Accessories"),
            new Product("PROD-003", "Running Shoes X1", "Lightweight breathable running shoes", 79.99, 30, "Footwear"),
            new Product("PROD-004", "Laptop Stand Adjustable", "Aluminium adjustable laptop stand", 39.99, 50, "Accessories"),
            new Product("PROD-005", "Mechanical Keyboard TKL", "Tenkeyless mechanical keyboard with blue switches", 69.99, 20, "Electronics"),
            new Product("PROD-006", "Smartwatch Series 3", "Fitness tracking smartwatch with heart rate monitor", 199.99, 8, "Electronics"),
            new Product("PROD-007", "BassBoost Headphones X1", "Over-ear bass-boosted wireless headphones", 59.99, 8, "Electronics"),
            new Product("PROD-008", "Coffee Maker Drip 12-Cup", "Programmable drip coffee maker", 49.99, 25, "Kitchen"),
            new Product("PROD-009", "Premium Coffee Beans 1kg", "Single-origin Arabica roasted coffee beans", 18.99, 60, "Kitchen"),
            new Product("PROD-010", "Portable Bluetooth Speaker", "Waterproof portable Bluetooth speaker 20W", 44.99, 0, "Electronics")
        ).forEach(p -> products.put(p.id(), p));
    }

    @Override
    public Optional<Product> findById(String productId) {
        if (productId == null || productId.isBlank()) return Optional.empty();
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<Product> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        String lower = keyword.toLowerCase();
        return products.values().stream()
            .filter(p -> p.name().toLowerCase().contains(lower) ||
                         p.description().toLowerCase().contains(lower) ||
                         p.category().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }
}
