package com.ecommerce.support.repository;

import com.ecommerce.support.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(String orderId);
    List<Order> findByCustomerId(String customerId);
}
