package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Order;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class JdbcOrderRepositoryIT {

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

    private JdbcOrderRepository repository;

    @BeforeEach
    void setUp() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load();
        flyway.clean();
        flyway.migrate();
        repository = new JdbcOrderRepository(dataSource);
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        Optional<Order> result = repository.findById("ORD-001");
        assertTrue(result.isPresent());
        assertEquals("ORD-001", result.get().id());
        assertEquals("CUST-001", result.get().customerId());
        assertEquals("SHIPPED", result.get().status());
        assertEquals(129.99, result.get().total(), 0.001);
        assertEquals(2, result.get().items().size());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        Optional<Order> result = repository.findById("ORD-UNKNOWN");
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_nullId_returnsEmpty() {
        assertTrue(repository.findById(null).isEmpty());
    }

    @Test
    void findByCustomerId_returnsOnlyCustomerOrders() {
        List<Order> orders = repository.findByCustomerId("CUST-001");
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.customerId().equals("CUST-001")));
    }

    @Test
    void findByCustomerId_unknownCustomer_returnsEmpty() {
        List<Order> orders = repository.findByCustomerId("CUST-UNKNOWN");
        assertTrue(orders.isEmpty());
    }
}
