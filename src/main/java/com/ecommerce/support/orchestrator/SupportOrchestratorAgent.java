package com.ecommerce.support.orchestrator;

import com.ecommerce.support.agents.OrderAgent;
import com.ecommerce.support.agents.ProductAgent;
import com.ecommerce.support.agents.RefundAgent;
import com.ecommerce.support.tools.OrderTools;
import com.ecommerce.support.tools.ProductTools;
import com.ecommerce.support.tools.RefundTools;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.AgentTool;

public class SupportOrchestratorAgent {

    private SupportOrchestratorAgent() {}

    public static LlmAgent create(OrderTools orderTools,
                                   RefundTools refundTools,
                                   ProductTools productTools) {

        LlmAgent orderAgent = OrderAgent.create(orderTools);
        LlmAgent refundAgent = RefundAgent.create(refundTools);
        LlmAgent productAgent = ProductAgent.create(productTools);

        return LlmAgent.builder()
            .name("support-orchestrator")
            .description("Top-level e-commerce support dispatcher.")
            .model("gemini-2.5-flash")
            .instruction("""
                You are a friendly support dispatcher for an e-commerce store.

                When a customer greets you (says hi, hello, hey, or similar),
                respond warmly and introduce the available support areas:
                - Orders: track, view, or manage orders
                - Refunds: check status or request a refund
                - Products: search products or check availability

                For all other messages, delegate to the appropriate specialist:
                - Order questions (order status, tracking, order history) → order-agent
                - Refund questions (refund status, new refund request, refund history) → refund-agent
                - Product questions (product details, search, stock availability) → product-agent

                Always be warm, concise, and professional.
                """)
            .tools(
                AgentTool.create(orderAgent),
                AgentTool.create(refundAgent),
                AgentTool.create(productAgent)
            )
            .build();
    }
}
