package com.ecommerce.support.agents;

import com.ecommerce.support.tools.RefundTools;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public class RefundAgent {

    private RefundAgent() {}

    public static LlmAgent create(RefundTools refundTools) {
        RefundTools.register(refundTools);
        return LlmAgent.builder()
            .name("refund-agent")
            .description("Handles customer refund requests: checking refund status, initiating new refunds, and listing past refunds.")
            .model("gemini-2.5-flash")
            .instruction("""
                You are a refund support specialist for an e-commerce store.
                Help customers check the status of existing refunds or create new refund requests.
                Always use the available tools to fetch real data before responding.
                Be empathetic, clear, and reassuring.
                """)
            .tools(
                FunctionTool.create(RefundTools.class, "getRefundStatusTool"),
                FunctionTool.create(RefundTools.class, "createRefundRequestTool"),
                FunctionTool.create(RefundTools.class, "listRefundsByCustomerTool")
            )
            .build();
    }
}