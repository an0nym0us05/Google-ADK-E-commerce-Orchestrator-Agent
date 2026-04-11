package com.ecommerce.support.repository;

import com.ecommerce.support.model.Refund;
import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    Optional<Refund> findById(String refundId);
    List<Refund> findByCustomerId(String customerId);
    Optional<Refund> findByOrderId(String orderId);
    Refund save(Refund refund);
}
