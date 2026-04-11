package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockProductRepositoryTest {

    private MockProductRepository repo;

    @BeforeEach
    void setUp() {
        repo = new MockProductRepository();
    }

    @Test
    void findById_existingProduct_returnsProduct() {
        Optional<Product> result = repo.findById("PROD-001");
        assertTrue(result.isPresent());
        assertEquals("PROD-001", result.get().id());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("PROD-999").isEmpty());
    }

    @Test
    void findAll_returns10Products() {
        List<Product> all = repo.findAll();
        assertEquals(10, all.size());
    }

    @Test
    void searchByKeyword_matchesNameCaseInsensitive() {
        List<Product> results = repo.searchByKeyword("headphone");
        assertFalse(results.isEmpty());
        results.forEach(p ->
            assertTrue(p.name().toLowerCase().contains("headphone") ||
                       p.description().toLowerCase().contains("headphone")));
    }

    @Test
    void searchByKeyword_noMatch_returnsEmpty() {
        List<Product> results = repo.searchByKeyword("xyznotexist");
        assertTrue(results.isEmpty());
    }
}
