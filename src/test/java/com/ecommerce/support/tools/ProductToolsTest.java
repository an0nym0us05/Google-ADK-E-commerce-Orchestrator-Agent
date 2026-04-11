package com.ecommerce.support.tools;

import com.ecommerce.support.repository.mock.MockProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductToolsTest {

    private ProductTools tools;

    @BeforeEach
    void setUp() {
        tools = new ProductTools(new MockProductRepository());
    }

    @Test
    void getProductById_existingProduct_containsName() {
        String result = tools.getProductById("PROD-001");
        assertTrue(result.contains("PROD-001"));
        assertTrue(result.contains("Wireless Headphones Pro"));
    }

    @Test
    void getProductById_unknownProduct_returnsNotFound() {
        String result = tools.getProductById("PROD-999");
        assertTrue(result.toLowerCase().contains("not found"));
    }

    @Test
    void searchProducts_matchingKeyword_returnsResults() {
        String result = tools.searchProducts("headphone");
        assertTrue(result.contains("PROD-001") || result.contains("PROD-007"));
    }

    @Test
    void checkProductAvailability_inStock_indicatesAvailable() {
        String result = tools.checkProductAvailability("PROD-001");
        assertTrue(result.toLowerCase().contains("in stock") ||
                   result.toLowerCase().contains("available"));
    }

    @Test
    void checkProductAvailability_outOfStock_indicatesUnavailable() {
        // PROD-010 has stockQuantity = 0
        String result = tools.checkProductAvailability("PROD-010");
        assertTrue(result.toLowerCase().contains("out of stock") ||
                   result.toLowerCase().contains("unavailable"));
    }
}
