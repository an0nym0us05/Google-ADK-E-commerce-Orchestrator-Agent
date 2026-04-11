package com.ecommerce.support.model;

public record Product(
    String id,
    String name,
    String description,
    double price,
    int stockQuantity,
    String category
) {}
