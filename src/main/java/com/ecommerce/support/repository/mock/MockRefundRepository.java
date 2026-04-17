package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Refund;
import com.ecommerce.support.repository.RefundRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MockRefundRepository implements RefundRepository {

    private final Map<String, Refund> refunds = new LinkedHashMap<>();

    public MockRefundRepository() {
        seed();
    }

    private void seed() {
        List.of(
            new Refund("REF-001", "ORD-002", "CUST-001",
                "COMPLETED", "Item damaged on arrival", LocalDate.of(2026, 3, 10)),
            new Refund("REF-002", "ORD-004", "CUST-002",
                "COMPLETED", "Changed my mind", LocalDate.of(2026, 3, 22)),
            new Refund("REF-003", "ORD-005", "CUST-003",
                "PENDING", "Wrong item received", LocalDate.of(2026, 2, 17))
        ).forEach(r -> refunds.put(r.id(), r));
    }

    @Override
    public Optional<Refund> findById(String refundId) {
        if (refundId == null || refundId.isBlank()) return Optional.empty();
        return Optional.ofNullable(refunds.get(refundId));
    }

    @Override
    public List<Refund> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) return List.of();
        return refunds.values().stream()
            .filter(r -> r.customerId().equals(customerId))
            .toList();
    }

    @Override
    public Optional<Refund> findByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) return Optional.empty();
        return refunds.values().stream()
            .filter(r -> r.orderId().equals(orderId))
            .findFirst();
    }

    @Override
    public Refund save(Refund refund) {
        refunds.put(refund.id(), refund);
        return refund;
    }
}
