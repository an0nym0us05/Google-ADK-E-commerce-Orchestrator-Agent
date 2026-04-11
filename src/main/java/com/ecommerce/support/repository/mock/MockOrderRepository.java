package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Order;
import com.ecommerce.support.repository.OrderRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MockOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new LinkedHashMap<>();

    public MockOrderRepository() {
        seed();
    }

    private void seed() {
        List.of(
            new Order("ORD-001", "CUST-001", "SHIPPED",
                List.of("Wireless Headphones", "USB-C Cable"),
                129.99, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 4, 14)),
            new Order("ORD-002", "CUST-001", "DELIVERED",
                List.of("Running Shoes"),
                89.99, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 8)),
            new Order("ORD-003", "CUST-002", "PROCESSING",
                List.of("Laptop Stand", "Keyboard"),
                74.50, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 17)),
            new Order("ORD-004", "CUST-002", "CANCELLED",
                List.of("Smartwatch"),
                199.00, LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 27)),
            new Order("ORD-005", "CUST-003", "DELIVERED",
                List.of("Coffee Maker", "Coffee Beans"),
                55.00, LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 15))
        ).forEach(o -> orders.put(o.id(), o));
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null || orderId.isBlank()) return Optional.empty();
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) return List.of();
        return orders.values().stream()
            .filter(o -> o.customerId().equals(customerId))
            .collect(Collectors.toList());
    }
}
