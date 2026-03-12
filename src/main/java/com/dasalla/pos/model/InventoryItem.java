package com.dasalla.pos.model;

public class InventoryItem {
    private int id;
    private String itemName;
    private int quantity;
    private String unit;
    private int restockThreshold;
    private double unitCost;
    private String updatedAt;

    public InventoryItem() {}

    public InventoryItem(String itemName, int quantity, String unit, int restockThreshold, double unitCost) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.restockThreshold = restockThreshold;
        this.unitCost = unitCost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getRestockThreshold() { return restockThreshold; }
    public void setRestockThreshold(int restockThreshold) { this.restockThreshold = restockThreshold; }

    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isLowStock() {
        return quantity <= restockThreshold;
    }

    public String getStockDisplay() {
        return quantity + " " + unit + " in stock";
    }

    public String getStockStatus() {
        return isLowStock() ? "LOW" : "OK";
    }
}
