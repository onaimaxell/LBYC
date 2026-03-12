package com.dasalla.pos.dao;

import com.dasalla.pos.model.Transaction;
import com.dasalla.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean insert(Transaction t) {
        String sql = """
            INSERT INTO transactions (order_id, amount, payment_method, gcash_reference, processed_by)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getOrderId());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getPaymentMethod());
            ps.setString(4, t.getGcashReference());
            ps.setInt(5, t.getProcessedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, o.order_number, c.name as customer_name, u.full_name as processed_by_name
            FROM transactions t
            JOIN orders o ON t.order_id = o.id
            JOIN customers c ON o.customer_id = c.id
            LEFT JOIN users u ON t.processed_by = u.id
            ORDER BY t.created_at DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapTransaction(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> search(String keyword, String dateFrom, String dateTo) {
        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, o.order_number, c.name as customer_name, u.full_name as processed_by_name
            FROM transactions t
            JOIN orders o ON t.order_id = o.id
            JOIN customers c ON o.customer_id = c.id
            LEFT JOIN users u ON t.processed_by = u.id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (c.name LIKE ? OR o.order_number LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append(" AND date(t.created_at) >= date(?)");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append(" AND date(t.created_at) <= date(?)");
            params.add(dateTo);
        }
        sql.append(" ORDER BY t.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTransaction(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setOrderId(rs.getInt("order_id"));
        t.setOrderNumber(rs.getString("order_number"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setAmount(rs.getDouble("amount"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setGcashReference(rs.getString("gcash_reference"));
        t.setProcessedBy(rs.getInt("processed_by"));
        t.setProcessedByName(rs.getString("processed_by_name"));
        t.setCreatedAt(rs.getString("created_at"));
        return t;
    }
}
