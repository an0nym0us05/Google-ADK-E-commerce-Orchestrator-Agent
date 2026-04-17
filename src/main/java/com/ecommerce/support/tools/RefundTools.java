package com.ecommerce.support.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ecommerce.support.model.Refund;
import com.ecommerce.support.repository.OrderRepository;
import com.ecommerce.support.repository.RefundRepository;
import com.google.adk.tools.Annotations;

public class RefundTools {

    private static volatile RefundTools INSTANCE;

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;

    public RefundTools(RefundRepository refundRepository, OrderRepository orderRepository) {
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Registers the instance to be used by the static tool methods required by ADK FunctionTool.
     */
    public static void register(RefundTools instance) {
        INSTANCE = instance;
    }

    // -------------------------------------------------------------------------
    // Static methods — required by ADK FunctionTool (only supports static methods).
    // @Schema(name=...) ensures the LLM sees the canonical tool name.
    // -------------------------------------------------------------------------

    private static RefundTools requireInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("RefundTools not registered. Call RefundTools.register() before using tool methods.");
        }
        return INSTANCE;
    }

    @Annotations.Schema(name = "getRefundStatus", description = "Get the status of a refund by refund ID")
    public static String getRefundStatusTool(String refundId) {
        return requireInstance().getRefundStatus(refundId);
    }

    @Annotations.Schema(name = "createRefundRequest", description = "Create a new refund request for an order")
    public static String createRefundRequestTool(String orderId, String reason) {
        return requireInstance().createRefundRequest(orderId, reason);
    }

    @Annotations.Schema(name = "listRefundsByCustomer", description = "List all refunds for a customer")
    public static String listRefundsByCustomerTool(String customerId) {
        return requireInstance().listRefundsByCustomer(customerId);
    }

    // -------------------------------------------------------------------------
    // Instance methods — preserved for direct use and testing
    // -------------------------------------------------------------------------

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
        // Look up the customer who owns this order
        String customerId = orderRepository.findById(orderId)
            .map(order -> order.customerId())
            .orElse("UNKNOWN");
        String newId = "REF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Refund refund = new Refund(newId, orderId, customerId, "PENDING", reason, LocalDate.now());
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
            "  - %s | Order: %s | Status: %s | Reason: %s%n",
            r.id(), r.orderId(), r.status(), r.reason())));
        return sb.toString().trim();
    }
}
