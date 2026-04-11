package com.ecommerce.support.model;

import java.time.LocalDate;

public record Refund(
    String id,
    String orderId,
    String customerId,
    String status,
    String reason,
    LocalDate createdAt
) {}
