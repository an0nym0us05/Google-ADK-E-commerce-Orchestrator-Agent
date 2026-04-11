package com.ecommerce.support.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.ecommerce.support.tools.OrderTools;

public class OrderAgent {

    private OrderAgent() {}

    public static LlmAgent create(OrderTools orderTools) {
        OrderTools.register(orderTools);
        return LlmAgent.builder()
            .name("order-agent")
            .description("Handles customer queries about orders: lookup, listing, and shipment tracking.")
            .model("gemini-2.0-flash")
            .instruction("""
                You are an order support specialist for an e-commerce store.
                Help customers look up their orders, check order status, and track shipments.
                Always use the available tools to fetch real data before responding.
                Be concise, friendly, and helpful.
                """)
            .tools(
                FunctionTool.create(OrderTools.class, "getOrderByIdTool"),
                FunctionTool.create(OrderTools.class, "listOrdersByCustomerTool"),
                FunctionTool.create(OrderTools.class, "trackOrderTool")
            )
            .build();
    }
}
