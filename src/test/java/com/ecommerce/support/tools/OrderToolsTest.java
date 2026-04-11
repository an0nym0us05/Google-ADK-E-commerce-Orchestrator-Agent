package com.ecommerce.support.tools;

import com.ecommerce.support.repository.mock.MockOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderToolsTest {

    private OrderTools tools;

    @BeforeEach
    void setUp() {
        tools = new OrderTools(new MockOrderRepository());
    }

    @Test
    void getOrderById_existingOrder_containsId() {
        String result = tools.getOrderById("ORD-001");
        assertTrue(result.contains("ORD-001"));
        assertTrue(result.contains("SHIPPED"));
    }

    @Test
    void getOrderById_unknownOrder_returnsNotFound() {
        String result = tools.getOrderById("ORD-999");
        assertTrue(result.toLowerCase().contains("not found"));
    }

    @Test
    void listOrdersByCustomer_existingCustomer_containsOrders() {
        String result = tools.listOrdersByCustomer("CUST-001");
        assertTrue(result.contains("ORD-001"));
        assertTrue(result.contains("ORD-002"));
    }

    @Test
    void trackOrder_shippedOrder_containsDeliveryInfo() {
        String result = tools.trackOrder("ORD-001");
        assertTrue(result.contains("ORD-001"));
        assertTrue(result.toLowerCase().contains("shipped") ||
                   result.toLowerCase().contains("deliver"));
    }
}
