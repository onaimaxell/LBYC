package com.dasalla.pos.controller;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.service.OrderService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class OrderListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnSearch;
    @FXML private Button btnBack;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colOrderNum;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colPayment;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colDate;

    private final OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        setupTable();
        statusFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "PROCESSING", "COMPLETED", "CLAIMED"));
        statusFilter.setValue("ALL");
        loadOrders();
        btnSearch.setOnAction(e -> loadOrders());
        searchField.setOnAction(e -> loadOrders());
        btnBack.setOnAction(e -> goBack());

        ordersTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Order selected = ordersTable.getSelectionModel().getSelectedItem();
                if (selected != null) openOrderSummary(selected.getOrderNumber());
            }
        });
    }

    private void setupTable() {
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("formattedTotal"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void loadOrders() {
        String keyword = searchField.getText().trim();
        String status = statusFilter.getValue();
        var list = orderService.searchOrders(keyword, null, null, status);
        ordersTable.setItems(FXCollections.observableArrayList(list));
    }

    private void openOrderSummary(String orderNumber) {
        try {
            // Fix ui going back to default resolution when changing scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_summary.fxml"));
            Parent root = loader.load();
            OrderSummaryController controller = loader.getController();
            controller.loadOrder(orderNumber);
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
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
}
