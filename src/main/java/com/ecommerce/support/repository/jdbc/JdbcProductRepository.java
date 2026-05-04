package com.ecommerce.support.repository.jdbc;

import com.ecommerce.support.model.Product;
import com.ecommerce.support.repository.ProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {

    private final JdbcTemplate jdbc;

    public JdbcProductRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Product> findById(String productId) {
        if (productId == null || productId.isBlank()) return Optional.empty();
        List<Product> rows = jdbc.query(
            "SELECT * FROM products WHERE id = ?",
            (rs, n) -> mapRow(rs),
            productId
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM products", (rs, n) -> mapRow(rs));
    }

    @Override
    public List<Product> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        String pattern = "%" + keyword + "%";
        return jdbc.query(
            "SELECT * FROM products WHERE name ILIKE ? OR description ILIKE ? OR category ILIKE ?",
            (rs, n) -> mapRow(rs),
            pattern, pattern, pattern
        );
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getInt("stock_quantity"),
            rs.getString("category")
        );
    }
}
