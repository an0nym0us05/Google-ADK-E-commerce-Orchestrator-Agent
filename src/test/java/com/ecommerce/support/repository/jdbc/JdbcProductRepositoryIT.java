package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class JdbcProductRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");

    static HikariDataSource dataSource;

    @BeforeAll
    static void setUpDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setMaximumPoolSize(2);
        dataSource = new HikariDataSource(config);
    }

    @AfterAll
    static void tearDownDataSource() {
        dataSource.close();
    }

    private JdbcProductRepository repository;

    @BeforeEach
    void setUp() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load();
        flyway.clean();
        flyway.migrate();
        repository = new JdbcProductRepository(dataSource);
    }

    @Test
    void findById_existingProduct_returnsProduct() {
        Optional<Product> result = repository.findById("PROD-001");
        assertTrue(result.isPresent());
        assertEquals("PROD-001", result.get().id());
        assertEquals("Wireless Headphones Pro", result.get().name());
        assertEquals("Electronics", result.get().category());
        assertEquals(89.99, result.get().price(), 0.001);
        assertEquals(15, result.get().stockQuantity());
    }

    @Test
    void findById_unknownProduct_returnsEmpty() {
        assertTrue(repository.findById("PROD-UNKNOWN").isEmpty());
    }

    @Test
    void findAll_returns10Products() {
        List<Product> all = repository.findAll();
        assertEquals(10, all.size());
    }

    @Test
    void searchByKeyword_matchesNameCaseInsensitive() {
        List<Product> results = repository.searchByKeyword("headphones");
        assertEquals(2, results.size());
    }

    @Test
    void searchByKeyword_matchesCategory() {
        List<Product> results = repository.searchByKeyword("Kitchen");
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.category().equals("Kitchen")));
    }

    @Test
    void searchByKeyword_noMatch_returnsEmpty() {
        List<Product> results = repository.searchByKeyword("zzznomatch");
        assertTrue(results.isEmpty());
    }
}
