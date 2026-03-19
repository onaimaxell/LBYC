package com.dasalla.pos.controller;

import com.dasalla.pos.model.InventoryItem;
import com.dasalla.pos.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Optional;

public class InventoryController {

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colItemName;
    @FXML private TableColumn<InventoryItem, String> colStock;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, Void> colActions;

    @FXML private TextField itemNameField;
    @FXML private TextField quantityField;
    @FXML private TextField unitField;
    @FXML private TextField thresholdField;
    @FXML private TextField costField;
    @FXML private Button btnAddItem;
    @FXML private Button btnBack;
    @FXML private Label alertLabel;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        setupTable();
        loadInventory();
        btnAddItem.setOnAction(e -> addItem());
        btnBack.setOnAction(e -> goBack());
    }

    private void setupTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockDisplay"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                setStyle("LOW".equals(status) ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                        : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button restockBtn = new Button("Restock");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(6, restockBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER);
                restockBtn.getStyleClass().add("btn-restock");
                deleteBtn.getStyleClass().add("btn-icon-danger");

                restockBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    showRestockDialog(item);
                });
                deleteBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    if (confirmDelete(item.getItemName())) {
                        inventoryService.deleteItem(item.getId());
                        loadInventory();
                    }
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadInventory() {
        var items = inventoryService.getAllItems();
        inventoryTable.setItems(FXCollections.observableArrayList(items));

        var lowStock = inventoryService.getLowStockItems();
        if (!lowStock.isEmpty()) {
            alertLabel.setText("⚠ Low stock: " + lowStock.stream()
                    .map(InventoryItem::getItemName).reduce((a, b) -> a + ", " + b).orElse(""));
            alertLabel.setVisible(true);
        } else {
            alertLabel.setVisible(false);
        }
    }

    private void addItem() {
        try {
            String name = itemNameField.getText().trim();
            int qty = Integer.parseInt(quantityField.getText().trim());
            String unit = unitField.getText().trim();
            int threshold = Integer.parseInt(thresholdField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());

            if (name.isEmpty() || unit.isEmpty()) { showAlert("Please fill in all fields."); return; }

            InventoryItem item = new InventoryItem(name, qty, unit, threshold, cost);
            if (inventoryService.addItem(item)) {
                clearForm();
                loadInventory();
            } else {
                showAlert("Item already exists or error occurred.");
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers for quantity, threshold, and cost.");
        }
    }

    private void showRestockDialog(InventoryItem item) {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Restock - " + item.getItemName());
        dialog.setHeaderText("Current stock: " + item.getQuantity() + " " + item.getUnit());
        dialog.setContentText("Add quantity:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(val -> {
            try {
                int addQty = Integer.parseInt(val.trim());
                if (addQty > 0) {
                    inventoryService.restock(item.getId(), addQty);
                    loadInventory();
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid quantity.");
            }
        });
    }

    private boolean confirmDelete(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setContentText("Delete '" + itemName + "' from inventory?");
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private void clearForm() {
        itemNameField.clear(); quantityField.clear(); unitField.clear();
        thresholdField.clear(); costField.clear();
    }

    private void goBack() {
        try {
            // Fix ui going back to default resolution when changing scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            // paul

            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            // paul
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
