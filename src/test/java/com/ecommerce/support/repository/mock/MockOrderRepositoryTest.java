package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockOrderRepositoryTest {

    private MockOrderRepository repo;

    @BeforeEach
    void setUp() {
        repo = new MockOrderRepository();
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        Optional<Order> result = repo.findById("ORD-001");
        assertTrue(result.isPresent());
        assertEquals("ORD-001", result.get().id());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        Optional<Order> result = repo.findById("ORD-999");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCustomerId_returnsOnlyCustomerOrders() {
        List<Order> orders = repo.findByCustomerId("CUST-001");
        assertFalse(orders.isEmpty());
        orders.forEach(o -> assertEquals("CUST-001", o.customerId()));
    }
}
