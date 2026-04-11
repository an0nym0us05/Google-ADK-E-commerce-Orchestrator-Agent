package com.ecommerce.support.tools;

import com.ecommerce.support.model.Product;
import com.ecommerce.support.repository.ProductRepository;
import com.google.adk.tools.Annotations;

import java.util.List;
import java.util.Optional;

public class ProductTools {

    private static volatile ProductTools INSTANCE;

    private final ProductRepository productRepository;

    public ProductTools(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Registers the instance to be used by the static tool methods required by ADK FunctionTool.
     */
    public static void register(ProductTools instance) {
        INSTANCE = instance;
    }

    // -------------------------------------------------------------------------
    // Static methods — required by ADK FunctionTool (only supports static methods).
    // @Schema(name=...) ensures the LLM sees the canonical tool name.
    // -------------------------------------------------------------------------

    @Annotations.Schema(name = "getProductById", description = "Get product details by product ID")
    public static String getProductByIdTool(String productId) {
        if (INSTANCE == null) {
            throw new IllegalStateException("ProductTools not registered. Call ProductTools.register() before using tool methods.");
        }
        return INSTANCE.getProductById(productId);
    }

    @Annotations.Schema(name = "searchProducts", description = "Search products by keyword")
    public static String searchProductsTool(String keyword) {
        if (INSTANCE == null) {
            throw new IllegalStateException("ProductTools not registered. Call ProductTools.register() before using tool methods.");
        }
        return INSTANCE.searchProducts(keyword);
    }

    @Annotations.Schema(name = "checkProductAvailability", description = "Check stock availability of a product")
    public static String checkProductAvailabilityTool(String productId) {
        if (INSTANCE == null) {
            throw new IllegalStateException("ProductTools not registered. Call ProductTools.register() before using tool methods.");
        }
        return INSTANCE.checkProductAvailability(productId);
    }

    // -------------------------------------------------------------------------
    // Instance methods — preserved for direct use and testing
    // -------------------------------------------------------------------------

    public String getProductById(String productId) {
        if (productId == null || productId.isBlank()) {
            return "Error: productId is required.";
        }
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            return "Product " + productId + " not found.";
        }
        Product p = product.get();
        String availability = p.stockQuantity() > 0
            ? p.stockQuantity() + " in stock"
            : "Out of stock";
        return String.format(
            "Product %s | %s | Category: %s | Price: $%.2f | Availability: %s | Description: %s",
            p.id(), p.name(), p.category(), p.price(), availability, p.description());
    }

    public String searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "Error: keyword is required.";
        }
        List<Product> results = productRepository.searchByKeyword(keyword);
        if (results.isEmpty()) {
            return "No products found matching '" + keyword + "'.";
        }
        StringBuilder sb = new StringBuilder("Products matching '" + keyword + "':\n");
        results.forEach(p -> {
            String availability = p.stockQuantity() > 0
                ? p.stockQuantity() + " in stock"
                : "Out of stock";
            sb.append(String.format("  - %s | %s | $%.2f | %s\n",
                p.id(), p.name(), p.price(), availability));
        });
        return sb.toString().trim();
    }

    public String checkProductAvailability(String productId) {
        if (productId == null || productId.isBlank()) {
            return "Error: productId is required.";
        }
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            return "Product " + productId + " not found.";
        }
        Product p = product.get();
        if (p.stockQuantity() > 0) {
            return String.format("%s (%s) is in stock with %d units available.",
                p.name(), p.id(), p.stockQuantity());
        } else {
            return String.format("%s (%s) is currently out of stock.",
                p.name(), p.id());
        }
    }
}
