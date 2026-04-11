package com.ecommerce.support.repository.mock;

import com.ecommerce.support.model.Refund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockRefundRepositoryTest {

    private MockRefundRepository repo;

    @BeforeEach
    void setUp() {
        repo = new MockRefundRepository();
    }

    @Test
    void findById_existingRefund_returnsRefund() {
        Optional<Refund> result = repo.findById("REF-001");
        assertTrue(result.isPresent());
        assertEquals("REF-001", result.get().id());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("REF-999").isEmpty());
    }

    @Test
    void save_newRefund_canBeRetrieved() {
        Refund refund = new Refund("REF-NEW", "ORD-003", "CUST-002",
            "PENDING", "Wrong item", LocalDate.now());
        repo.save(refund);
        Optional<Refund> result = repo.findById("REF-NEW");
        assertTrue(result.isPresent());
        assertEquals("REF-NEW", result.get().id());
    }

    @Test
    void findByCustomerId_returnsCustomerRefunds() {
        List<Refund> refunds = repo.findByCustomerId("CUST-001");
        assertFalse(refunds.isEmpty());
        refunds.forEach(r -> assertEquals("CUST-001", r.customerId()));
    }
}
