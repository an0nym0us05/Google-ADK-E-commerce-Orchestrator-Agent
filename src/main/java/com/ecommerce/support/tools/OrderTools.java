package com.ecommerce.support.tools;

import java.util.List;
import java.util.Optional;

import com.ecommerce.support.model.Order;
import com.ecommerce.support.repository.OrderRepository;
import com.google.adk.tools.Annotations;

public class OrderTools {

    private static volatile OrderTools INSTANCE;

    private final OrderRepository orderRepository;

    public OrderTools(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Registers the instance to be used by the static tool methods required by ADK FunctionTool.
     */
    public static void register(OrderTools instance) {
        INSTANCE = instance;
    }

    // -------------------------------------------------------------------------
    // Static methods — required by ADK FunctionTool (only supports static methods).
    // @Schema(name=...) ensures the LLM sees the canonical tool name.
    // -------------------------------------------------------------------------

    private static OrderTools requireInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("OrderTools not registered. Call OrderTools.register() before using tool methods.");
        }
        return INSTANCE;
    }

    @Annotations.Schema(name = "getOrderById", description = "Get order details by order ID")
    public static String getOrderByIdTool(String orderId) {
        return requireInstance().getOrderById(orderId);
    }

    @Annotations.Schema(name = "listOrdersByCustomer", description = "List all orders for a customer")
    public static String listOrdersByCustomerTool(String customerId) {
        return requireInstance().listOrdersByCustomer(customerId);
    }

    @Annotations.Schema(name = "trackOrder", description = "Track the shipment status of an order")
    public static String trackOrderTool(String orderId) {
        return requireInstance().trackOrder(orderId);
    }

    // -------------------------------------------------------------------------
    // Instance methods — preserved for direct use and testing
    // -------------------------------------------------------------------------

    public String getOrderById(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "Error: orderId is required.";
        }
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return "Order " + orderId + " not found.";
        }
        Order o = order.get();
        return String.format(
            "Order %s | Customer: %s | Status: %s | Items: %s | Total: $%.2f | Placed: %s | Est. Delivery: %s",
            o.id(), o.customerId(), o.status(), String.join(", ", o.items()),
            o.total(), o.createdAt(), o.estimatedDelivery());
    }

    public String listOrdersByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return "Error: customerId is required.";
        }
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        if (orders.isEmpty()) {
            return "No orders found for customer " + customerId + ".";
        }
        StringBuilder sb = new StringBuilder("Orders for customer " + customerId + ":\n");
        orders.forEach(o -> sb.append(String.format(
            "  - %s | Status: %s | Total: $%.2f | Placed: %s%n",
            o.id(), o.status(), o.total(), o.createdAt())));
        return sb.toString().trim();
    }

    public String trackOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "Error: orderId is required.";
        }
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return "Order " + orderId + " not found.";
        }
        Order o = order.get();
        return switch (o.status()) {
            case "PROCESSING" -> String.format("Order %s is being processed and will ship soon.", o.id());
            case "SHIPPED" -> String.format("Order %s has been shipped. Estimated delivery: %s.", o.id(), o.estimatedDelivery());
            case "DELIVERED" -> String.format("Order %s was delivered on %s.", o.id(), o.estimatedDelivery());
            case "CANCELLED" -> String.format("Order %s has been cancelled.", o.id());
            default -> String.format("Order %s status: %s.", o.id(), o.status());
        };
    }
}
