package com.ecommerce.support.tools;

import com.ecommerce.support.model.Refund;
import com.ecommerce.support.repository.RefundRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RefundTools {

    private final RefundRepository refundRepository;

    public RefundTools(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    public String getRefundStatus(String refundId) {
        if (refundId == null || refundId.isBlank()) {
            return "Error: refundId is required.";
        }
        Optional<Refund> refund = refundRepository.findById(refundId);
        if (refund.isEmpty()) {
            return "Refund " + refundId + " not found.";
        }
        Refund r = refund.get();
        return String.format(
            "Refund %s | Order: %s | Status: %s | Reason: %s | Requested: %s",
            r.id(), r.orderId(), r.status(), r.reason(), r.createdAt());
    }

    public String createRefundRequest(String orderId, String reason) {
        if (orderId == null || orderId.isBlank()) {
            return "Error: orderId is required.";
        }
        if (reason == null || reason.isBlank()) {
            return "Error: reason is required.";
        }
        String newId = "REF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Refund refund = new Refund(newId, orderId, "UNKNOWN", "PENDING", reason, LocalDate.now());
        refundRepository.save(refund);
        return String.format(
            "Refund request created successfully. Refund ID: %s | Order: %s | Status: PENDING | Reason: %s. You will hear back within 3-5 business days.",
            newId, orderId, reason);
    }

    public String listRefundsByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return "Error: customerId is required.";
        }
        List<Refund> refunds = refundRepository.findByCustomerId(customerId);
        if (refunds.isEmpty()) {
            return "No refunds found for customer " + customerId + ".";
        }
        StringBuilder sb = new StringBuilder("Refunds for customer " + customerId + ":\n");
        refunds.forEach(r -> sb.append(String.format(
            "  - %s | Order: %s | Status: %s | Reason: %s\n",
            r.id(), r.orderId(), r.status(), r.reason())));
        return sb.toString().trim();
    }
}
