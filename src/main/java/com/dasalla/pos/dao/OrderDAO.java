package com.dasalla.pos.dao;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.model.OrderItem;
import com.dasalla.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int insertAndGetId(Order order) {
        String sql = """
            INSERT INTO orders (order_number, customer_id, status, total_amount, payment_status)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderNumber());
            ps.setInt(2, order.getCustomerId());
            ps.setString(3, order.getStatus());
            ps.setDouble(4, order.getTotalAmount());
            ps.setString(5, order.getPaymentStatus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                for (OrderItem item : order.getItems()) {
                    insertItem(conn, id, item);
                }
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void insertItem(Connection conn, int orderId, OrderItem item) throws SQLException {
        String sql = """
            INSERT INTO order_items (order_id, service_type, description, quantity, unit_price, subtotal)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, item.getServiceType());
            ps.setString(3, item.getDescription());
            ps.setDouble(4, item.getQuantity());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDouble(6, item.getSubtotal());
            ps.executeUpdate();
        }
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status=?, updated_at=datetime('now','localtime') WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAsPaid(int orderId, String paymentMethod, String gcashRef) {
        String sql = """
            UPDATE orders SET payment_status='PAID', payment_method=?, gcash_reference=?,
            updated_at=datetime('now','localtime') WHERE id=?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setString(2, gcashRef);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Order findByOrderNumber(String orderNumber) {
        String sql = """
            SELECT o.*, c.name as customer_name, c.phone as customer_phone
            FROM orders o JOIN customers c ON o.customer_id = c.id
            WHERE o.order_number = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(getItemsByOrderId(order.getId()));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Order findById(int id) {
        String sql = """
            SELECT o.*, c.name as customer_name, c.phone as customer_phone
            FROM orders o JOIN customers c ON o.customer_id = c.id
            WHERE o.id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = mapOrder(rs);
                order.setItems(getItemsByOrderId(order.getId()));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getAll() {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*, c.name as customer_name, c.phone as customer_phone
            FROM orders o JOIN customers c ON o.customer_id = c.id
            ORDER BY o.created_at DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> searchOrders(String keyword, String dateFrom, String dateTo, String status) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT o.*, c.name as customer_name, c.phone as customer_phone
            FROM orders o JOIN customers c ON o.customer_id = c.id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (c.name LIKE ? OR o.order_number LIKE ? OR c.phone LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append(" AND date(o.created_at) >= date(?)");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append(" AND date(o.created_at) <= date(?)");
            params.add(dateTo);
        }
        if (status != null && !status.isBlank() && !status.equals("ALL")) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY o.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getActiveOrders() {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*, c.name as customer_name, c.phone as customer_phone
            FROM orders o JOIN customers c ON o.customer_id = c.id
            WHERE o.status IN ('PENDING','PROCESSING','COMPLETED')
            ORDER BY o.created_at DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE payment_status='PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getRevenueByPeriod(String period) {
        String dateFilter = switch (period) {
            case "TODAY" -> "date(created_at) = date('now','localtime')";
            case "WEEKLY" -> "date(created_at) >= date('now','localtime','-7 days')";
            case "MONTHLY" -> "strftime('%Y-%m', created_at) = strftime('%Y-%m', 'now','localtime')";
            default -> "1=1";
        };
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE payment_status='PAID' AND " + dateFilter;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<OrderItem> getItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setServiceType(rs.getString("service_type"));
                item.setDescription(rs.getString("description"));
                item.setQuantity(rs.getDouble("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setGcashReference(rs.getString("gcash_reference"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(rs.getString("created_at"));
        order.setUpdatedAt(rs.getString("updated_at"));
        return order;
    }
}
