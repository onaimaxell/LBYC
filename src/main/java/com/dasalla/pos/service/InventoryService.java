package com.dasalla.pos.service;

import com.dasalla.pos.dao.InventoryDAO;
import com.dasalla.pos.model.InventoryItem;

import java.util.List;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<InventoryItem> getAllItems() {
        return inventoryDAO.getAll();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryDAO.getLowStock();
    }

    public boolean addItem(InventoryItem item) {
        return inventoryDAO.insert(item);
    }

    public boolean updateItem(InventoryItem item) {
        return inventoryDAO.update(item);
    }

    public boolean restock(int itemId, int additionalQuantity) {
        List<InventoryItem> items = inventoryDAO.getAll();
        for (InventoryItem item : items) {
            if (item.getId() == itemId) {
                return inventoryDAO.updateQuantity(itemId, item.getQuantity() + additionalQuantity);
            }
        }
        return false;
    }

    public boolean deleteItem(int id) {
        return inventoryDAO.delete(id);
    }

    public boolean hasLowStockAlerts() {
        return !inventoryDAO.getLowStock().isEmpty();
    }
}
