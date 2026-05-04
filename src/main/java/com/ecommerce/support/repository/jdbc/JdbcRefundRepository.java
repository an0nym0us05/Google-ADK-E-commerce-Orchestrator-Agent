package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Refund;
import com.ecommerce.support.repository.RefundRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcRefundRepository implements RefundRepository {

    private final JdbcTemplate jdbc;

    public JdbcRefundRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Refund> findById(String refundId) {
        if (refundId == null || refundId.isBlank()) return Optional.empty();
        List<Refund> rows = jdbc.query(
            "SELECT * FROM refunds WHERE id = ?",
            (rs, n) -> mapRow(rs),
            refundId
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Refund> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) return List.of();
        return jdbc.query(
            "SELECT * FROM refunds WHERE customer_id = ?",
            (rs, n) -> mapRow(rs),
            customerId
        );
    }

    @Override
    public Optional<Refund> findByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) return Optional.empty();
        List<Refund> rows = jdbc.query(
            "SELECT * FROM refunds WHERE order_id = ? LIMIT 1",
            (rs, n) -> mapRow(rs),
            orderId
        );
        return rows.stream().findFirst();
    }

    @Override
    public Refund save(Refund refund) {
        jdbc.update("""
            INSERT INTO refunds (id, order_id, customer_id, status, reason, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                status = EXCLUDED.status,
                reason = EXCLUDED.reason
            """,
            refund.id(),
            refund.orderId(),
            refund.customerId(),
            refund.status(),
            refund.reason(),
            Date.valueOf(refund.createdAt())
        );
        return refund;
    }

    private Refund mapRow(ResultSet rs) throws SQLException {
        return new Refund(
            rs.getString("id"),
            rs.getString("order_id"),
            rs.getString("customer_id"),
            rs.getString("status"),
            rs.getString("reason"),
            rs.getDate("created_at").toLocalDate()
        );
    }
}
