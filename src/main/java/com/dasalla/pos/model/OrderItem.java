package com.dasalla.pos.model;

public class OrderItem {
    private int id;
    private int orderId;
    private String serviceType;
    private String description;
    private double quantity;
    private double unitPrice;
    private double subtotal;

    public OrderItem() {}

    public OrderItem(String serviceType, String description, double quantity, double unitPrice) {
        this.serviceType = serviceType;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getFormattedSubtotal() {
        return String.format("₱%.2f", subtotal);
    }

    @Override
    public String toString() {
        return description + " x" + quantity + " = " + getFormattedSubtotal();
    }
}
