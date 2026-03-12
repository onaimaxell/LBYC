package com.dasalla.pos.dao;

import com.dasalla.pos.model.InventoryItem;
import com.dasalla.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<InventoryItem> getAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY item_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapItem(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<InventoryItem> getLowStock() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity <= restock_threshold ORDER BY item_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapItem(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(InventoryItem item) {
        String sql = "INSERT INTO inventory (item_name, quantity, unit, restock_threshold, unit_cost) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getQuantity());
            ps.setString(3, item.getUnit());
            ps.setInt(4, item.getRestockThreshold());
            ps.setDouble(5, item.getUnitCost());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(InventoryItem item) {
        String sql = """
            UPDATE inventory SET item_name=?, quantity=?, unit=?, restock_threshold=?, unit_cost=?,
            updated_at=datetime('now','localtime') WHERE id=?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getQuantity());
            ps.setString(3, item.getUnit());
            ps.setInt(4, item.getRestockThreshold());
            ps.setDouble(5, item.getUnitCost());
            ps.setInt(6, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateQuantity(int id, int newQuantity) {
        String sql = "UPDATE inventory SET quantity=?, updated_at=datetime('now','localtime') WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM inventory WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private InventoryItem mapItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getInt("id"));
        item.setItemName(rs.getString("item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnit(rs.getString("unit"));
        item.setRestockThreshold(rs.getInt("restock_threshold"));
        item.setUnitCost(rs.getDouble("unit_cost"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }
}
