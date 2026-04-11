package com.ecommerce.support.model;

import java.time.LocalDate;
import java.util.List;

public record Order(
    String id,
    String customerId,
    String status,
    List<String> items,
    double total,
    LocalDate createdAt,
    LocalDate estimatedDelivery
) {}
