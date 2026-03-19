package com.dasalla.pos.controller;

import java.util.List;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.service.InventoryService;
import com.dasalla.pos.service.OrderService;
import com.dasalla.pos.util.SceneUtil;
import com.dasalla.pos.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label revenueLabel;
    @FXML private Label ordersCountLabel;
    @FXML private Label pendingLabel;
    @FXML private Label completedLabel;
    @FXML private Label lowStockAlert;
    @FXML private TableView<Order> activeOrdersTable;
    @FXML private TableColumn<Order, String> colOrderNum;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private ComboBox<String> periodCombo;
    @FXML private Button btnNewOrder;
    @FXML private Button btnOrders;
    @FXML private Button btnInventory;
    @FXML private Button btnTransactions;
    @FXML private Button btnLogout;

    private final OrderService orderService = new OrderService();
    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        setupUserInfo();
        setupTable();
        setupPeriodCombo();
        setupNavigation();
        refreshDashboard();
    }

    private void setupUserInfo() {
        if (SessionManager.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getFullName());
            roleLabel.setText(SessionManager.getCurrentUser().getRole());
        }
    }

    private void setupTable() {
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("formattedTotal"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        activeOrdersTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Order selected = activeOrdersTable.getSelectionModel().getSelectedItem();
                if (selected != null) openOrderSummary(selected.getOrderNumber());
            }
        });
    }

    private void setupPeriodCombo() {
        periodCombo.setItems(FXCollections.observableArrayList("TODAY", "WEEKLY", "MONTHLY"));
        periodCombo.setValue("TODAY");
        periodCombo.setOnAction(e -> updateRevenue(periodCombo.getValue()));
    }

    private void setupNavigation() {
        btnNewOrder.setOnAction(e -> openScreen("/fxml/order_entry.fxml"));
        btnOrders.setOnAction(e -> openScreen("/fxml/order_list.fxml"));
        btnInventory.setOnAction(e -> openScreen("/fxml/inventory.fxml"));
        btnTransactions.setOnAction(e -> openScreen("/fxml/transaction_history.fxml"));
        btnLogout.setOnAction(e -> logout());

        if (!SessionManager.isAdmin()) {
            btnInventory.setDisable(false);
        }
    }

    public void refreshDashboard() {
        int total = orderService.getTotalOrders();
        int pending = orderService.getOrderCountByStatus("PENDING");
        int completed = orderService.getOrderCountByStatus("COMPLETED");

        ordersCountLabel.setText(String.valueOf(total));
        pendingLabel.setText(String.valueOf(pending));
        completedLabel.setText(String.valueOf(completed));

        updateRevenue(periodCombo.getValue() != null ? periodCombo.getValue() : "TODAY");

        List<Order> active = orderService.getActiveOrders();
        activeOrdersTable.setItems(FXCollections.observableArrayList(active));

        if (inventoryService.hasLowStockAlerts()) {
            lowStockAlert.setVisible(true);
            lowStockAlert.setText("⚠ Low stock alert! Check inventory.");
        } else {
            lowStockAlert.setVisible(false);
        }
    }

    private void updateRevenue(String period) {
        double revenue = orderService.getRevenueByPeriod(period != null ? period : "TODAY");
        revenueLabel.setText(String.format("₱%.2f", revenue));
    }

    private void openScreen(String fxmlPath) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                showAlert("Error", "FXML not found: " + fxmlPath + ". Run: mvn clean compile");
                return;
            }

            // TODO
            // Fix ui going back to default resolution when changing scene
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) btnNewOrder.getScene().getWindow();
            // paul
            SceneUtil.switchScene(stage, "/fxml/dashboard.fxml");

            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed: " + fxmlPath + " " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void openOrderSummary(String orderNumber) {
        try {
            // Fix ui going back to default resolution when changing scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_summary.fxml"));
            Parent root = loader.load();
            OrderSummaryController controller = loader.getController();
            controller.loadOrder(orderNumber);
            Stage stage = (Stage) activeOrdersTable.getScene().getWindow();
            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
