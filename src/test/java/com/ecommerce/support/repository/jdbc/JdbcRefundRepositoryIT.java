package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Refund;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class JdbcRefundRepositoryIT {

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

    private JdbcRefundRepository repository;

    @BeforeEach
    void setUp() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load();
        flyway.clean();
        flyway.migrate();
        repository = new JdbcRefundRepository(dataSource);
    }

    @Test
    void findById_existingRefund_returnsRefund() {
        Optional<Refund> result = repository.findById("REF-001");
        assertTrue(result.isPresent());
        assertEquals("REF-001", result.get().id());
        assertEquals("ORD-002", result.get().orderId());
        assertEquals("CUST-001", result.get().customerId());
        assertEquals("COMPLETED", result.get().status());
    }

    @Test
    void findById_unknownRefund_returnsEmpty() {
        assertTrue(repository.findById("REF-UNKNOWN").isEmpty());
    }

    @Test
    void findByCustomerId_returnsCustomerRefunds() {
        List<Refund> results = repository.findByCustomerId("CUST-001");
        assertEquals(1, results.size());
        assertEquals("CUST-001", results.get(0).customerId());
    }

    @Test
    void findByOrderId_existingRefund_returnsRefund() {
        Optional<Refund> result = repository.findByOrderId("ORD-002");
        assertTrue(result.isPresent());
        assertEquals("REF-001", result.get().id());
    }

    @Test
    void findByOrderId_unknownOrder_returnsEmpty() {
        assertTrue(repository.findByOrderId("ORD-UNKNOWN").isEmpty());
    }

    @Test
    void save_newRefund_canBeRetrieved() {
        Refund newRefund = new Refund(
            "REF-NEW", "ORD-001", "CUST-001", "PENDING",
            "Item not delivered", LocalDate.of(2026, 4, 18)
        );
        Refund saved = repository.save(newRefund);
        assertEquals("REF-NEW", saved.id());

        Optional<Refund> retrieved = repository.findById("REF-NEW");
        assertTrue(retrieved.isPresent());
        assertEquals("PENDING", retrieved.get().status());
    }

    @Test
    void save_existingRefund_updatesStatus() {
        Refund updated = new Refund(
            "REF-003", "ORD-005", "CUST-003", "COMPLETED",
            "Resolved after review", LocalDate.of(2026, 2, 17)
        );
        repository.save(updated);

        Optional<Refund> retrieved = repository.findById("REF-003");
        assertTrue(retrieved.isPresent());
        assertEquals("COMPLETED", retrieved.get().status());
    }
}
