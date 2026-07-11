package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.MarketplaceItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceDAO {

    public void listItem(int sellerId, String title, String description, double price) throws SQLException {
        String sql = "INSERT INTO marketplace_items (seller_id, title, description, price, status, created_at) " +
                "VALUES (?, ?, ?, ?, 'AVAILABLE', NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.executeUpdate();
        }
    }

    public List<MarketplaceItem> findAvailable() throws SQLException {
        String sql = """
                SELECT m.id, m.seller_id, u.name AS seller_name, m.title, m.description, m.price, m.status, m.created_at
                FROM marketplace_items m
                INNER JOIN users u ON m.seller_id = u.id
                WHERE m.status = 'AVAILABLE'
                ORDER BY m.created_at DESC
                """;
        return runQuery(sql, null);
    }

    public List<MarketplaceItem> findBySeller(int sellerId) throws SQLException {
        String sql = """
                SELECT m.id, m.seller_id, u.name AS seller_name, m.title, m.description, m.price, m.status, m.created_at
                FROM marketplace_items m
                INNER JOIN users u ON m.seller_id = u.id
                WHERE m.seller_id = ?
                ORDER BY m.created_at DESC
                """;
        return runQuery(sql, sellerId);
    }

    public void markSold(int itemId, int sellerId) throws SQLException {
        String sql = "UPDATE marketplace_items SET status = 'SOLD' WHERE id = ? AND seller_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, sellerId);
            ps.executeUpdate();
        }
    }

    private List<MarketplaceItem> runQuery(String sql, Integer sellerId) throws SQLException {
        List<MarketplaceItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sellerId != null) {
                ps.setInt(1, sellerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new MarketplaceItem(
                            rs.getInt("id"), rs.getInt("seller_id"), rs.getString("seller_name"),
                            rs.getString("title"), rs.getString("description"), rs.getDouble("price"),
                            rs.getString("status"), rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return items;
    }
}
