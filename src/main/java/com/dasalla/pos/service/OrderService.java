package com.dasalla.pos.service;

import com.dasalla.pos.dao.CustomerDAO;
import com.dasalla.pos.dao.OrderDAO;
import com.dasalla.pos.model.Customer;
import com.dasalla.pos.model.Order;
import com.dasalla.pos.model.OrderItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.dasalla.pos.dao.InventoryDAO;
import com.dasalla.pos.model.InventoryItem;
import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final NotificationService notificationService = new NotificationService();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // Pricing constants
    public static final double WASH_FOLD_PRICE_PER_KG = 50.0;
    public static final double DRY_CLEANING_PRICE_PER_KG = 120.0;
    public static final double IRONING_PRICE_PER_KG = 30.0;
    public static final double EXPRESS_ADDON = 50.0;
    public static final double DELICATE_ADDON = 30.0;
    public static final double STAIN_REMOVAL_ADDON = 20.0;

    public Order createOrder(String customerName, String customerPhone, List<OrderItem> items) {
        // Find or create customer
        Customer customer = customerDAO.findByPhone(customerPhone);
        int customerId;
        if (customer == null) {
            Customer newCustomer = new Customer(customerName, customerPhone);
            customerId = customerDAO.insertAndGetId(newCustomer);
        } else {
            customerId = customer.getId();
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setItems(items);
        order.calculateTotal();

        int id = orderDAO.insertAndGetId(order);
        // Deduct inventory based on total kg
        deductInventory(order.getItems());
        order.setId(id);
        return order;
    }

    private void deductInventory(List<OrderItem> items) {
    // Calculate total kg across all items
    double totalKg = items.stream().mapToDouble(OrderItem::getQuantity).sum();

    List<InventoryItem> stock = inventoryDAO.getAll();

    for (InventoryItem item : stock) {
        String name = item.getItemName().toLowerCase();

        // Deduct 1 unit of detergent per 5 kg
        if (name.contains("detergent")) {
            int use = (int) Math.ceil(totalKg / 5.0);
            int newQty = Math.max(0, item.getQuantity() - use);
            inventoryDAO.updateQuantity(item.getId(), newQty);
        }

        // Deduct 1 unit of fabric softener per 7 kg
        if (name.contains("fabric softener")) {
            int use = (int) Math.ceil(totalKg / 7.0);
            int newQty = Math.max(0, item.getQuantity() - use);
            inventoryDAO.updateQuantity(item.getId(), newQty);
        }

        // Deduct 1 unit of bleach per 10 kg
        if (name.contains("bleach")) {
            int use = (int) Math.ceil(totalKg / 10.0);
            int newQty = Math.max(0, item.getQuantity() - use);
            inventoryDAO.updateQuantity(item.getId(), newQty);
        }
    }
}


    public boolean updateStatus(int orderId, String newStatus) {
        boolean result = orderDAO.updateStatus(orderId, newStatus);
        if (result && "COMPLETED".equals(newStatus)) {
            Order order = orderDAO.findById(orderId);
            if (order != null) {
                notificationService.sendCompletionSMS(order.getCustomerPhone(),
                        order.getOrderNumber(), order.getCustomerName());
            }
        }
        return result;
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    public Order getOrderById(int id) {
        return orderDAO.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAll();
    }

    public List<Order> getActiveOrders() {
        return orderDAO.getActiveOrders();
    }

    public List<Order> searchOrders(String keyword, String dateFrom, String dateTo, String status) {
        return orderDAO.searchOrders(keyword, dateFrom, dateTo, status);
    }

    public int getTotalOrders() {
        return orderDAO.countAll();
    }

    public int getOrderCountByStatus(String status) {
        return orderDAO.countByStatus(status);
    }

    public double getTotalRevenue() {
        return orderDAO.getTotalRevenue();
    }

    public double getRevenueByPeriod(String period) {
        return orderDAO.getRevenueByPeriod(period);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int count = orderDAO.countAll() + 1;
        return String.format("%05d", count);
    }

    public double calculateItemPrice(String serviceType, double quantity) {
        return switch (serviceType) {
            case "Wash & Fold" -> quantity * WASH_FOLD_PRICE_PER_KG;
            case "Dry Cleaning" -> quantity * DRY_CLEANING_PRICE_PER_KG;
            case "Ironing" -> quantity * IRONING_PRICE_PER_KG;
            default -> 0;
        };
    }
}
