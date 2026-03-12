package com.dasalla.pos.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String orderNumber;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private String status;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String gcashReference;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.paymentStatus = "UNPAID";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getGcashReference() { return gcashReference; }
    public void setGcashReference(String gcashReference) { this.gcashReference = gcashReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public void addItem(OrderItem item) { this.items.add(item); }

    public double calculateTotal() {
        totalAmount = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        return totalAmount;
    }

    public String getFormattedTotal() {
        return String.format("₱%.2f", totalAmount);
    }
}
