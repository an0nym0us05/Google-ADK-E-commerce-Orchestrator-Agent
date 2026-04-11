package com.ecommerce.support.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.ecommerce.support.tools.ProductTools;

public class ProductAgent {

    private ProductAgent() {}

    public static LlmAgent create(ProductTools productTools) {
        ProductTools.register(productTools);
        return LlmAgent.builder()
            .name("product-agent")
            .description("Handles product queries: product details, search by keyword, and availability checks.")
            .model("gemini-2.0-flash")
            .instruction("""
                You are a product specialist for an e-commerce store.
                Help customers find product information, search the catalog, and check stock availability.
                Always use the available tools to fetch real data before responding.
                Be enthusiastic and informative.
                """)
            .tools(
                FunctionTool.create(ProductTools.class, "getProductByIdTool"),
                FunctionTool.create(ProductTools.class, "searchProductsTool"),
                FunctionTool.create(ProductTools.class, "checkProductAvailabilityTool")
            )
            .build();
    }
}
