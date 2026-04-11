package com.ecommerce.support.tools;

import com.ecommerce.support.repository.mock.MockOrderRepository;
import com.ecommerce.support.repository.mock.MockRefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefundToolsTest {

    private RefundTools tools;

    @BeforeEach
    void setUp() {
        tools = new RefundTools(new MockRefundRepository(), new MockOrderRepository());
    }

    @Test
    void getRefundStatus_existingRefund_containsStatus() {
        String result = tools.getRefundStatus("REF-001");
        assertTrue(result.contains("REF-001"));
        assertTrue(result.contains("COMPLETED"));
    }

    @Test
    void getRefundStatus_unknownRefund_returnsNotFound() {
        String result = tools.getRefundStatus("REF-999");
        assertTrue(result.toLowerCase().contains("not found"));
    }

    @Test
    void createRefundRequest_newRefund_returnsRefundId() {
        String result = tools.createRefundRequest("ORD-001", "Defective product");
        assertTrue(result.toLowerCase().contains("refund"));
        assertTrue(result.contains("ORD-001"));
    }

    @Test
    void listRefundsByCustomer_returnsCustomerRefunds() {
        String result = tools.listRefundsByCustomer("CUST-001");
        assertTrue(result.contains("REF-001"));
    }
}
