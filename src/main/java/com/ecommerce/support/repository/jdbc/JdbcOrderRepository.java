package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Order;
import com.ecommerce.support.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcOrderRepository implements OrderRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public JdbcOrderRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null || orderId.isBlank()) return Optional.empty();
        List<Order> rows = jdbc.query(
            "SELECT * FROM orders WHERE id = ?",
            (rs, n) -> mapRow(rs),
            orderId
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) return List.of();
        return jdbc.query(
            "SELECT * FROM orders WHERE customer_id = ?",
            (rs, n) -> mapRow(rs),
            customerId
        );
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        try {
            List<String> items = MAPPER.readValue(
                rs.getString("items"),
                new TypeReference<List<String>>() {}
            );
            LocalDate estimatedDelivery = rs.getDate("estimated_delivery") != null
                ? rs.getDate("estimated_delivery").toLocalDate()
                : null;
            return new Order(
                rs.getString("id"),
                rs.getString("customer_id"),
                rs.getString("status"),
                items,
                rs.getDouble("total"),
                rs.getDate("created_at").toLocalDate(),
                estimatedDelivery
            );
        } catch (Exception e) {
            throw new SQLException("Failed to deserialize order row", e);
        }
    }
}
