package com.dasalla.pos.controller;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.model.OrderItem;
import com.dasalla.pos.service.OrderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class OrderEntryController {

    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;

    @FXML private ToggleButton btnWashFold;
    @FXML private ToggleButton btnDryCleaning;
    @FXML private ToggleButton btnIroning;
    @FXML private ToggleGroup serviceGroup;

    @FXML private Spinner<Double> quantitySpinner;
    @FXML private Label priceLabel;

    @FXML private CheckBox expressAddon;
    @FXML private CheckBox delicateAddon;
    @FXML private CheckBox stainRemovalAddon;

    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> colItemDesc;
    @FXML private TableColumn<OrderItem, Double> colItemQty;
    @FXML private TableColumn<OrderItem, String> colItemPrice;
    @FXML private TableColumn<OrderItem, Void> colItemAction;

    @FXML private Label totalLabel;
    @FXML private Button btnAddItem;
    @FXML private Button btnReset;
    @FXML private Button btnPlaceOrder;
    @FXML private Button btnBack;

    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final OrderService orderService = new OrderService();
    private String selectedService = "Wash & Fold";

    @FXML
    public void initialize() {
        setupServiceToggle();
        setupSpinner();
        setupTable();
        setupAddons();
        setupButtons();
        updatePriceDisplay();
    }

    private void setupServiceToggle() {
        serviceGroup = new ToggleGroup();
        btnWashFold.setToggleGroup(serviceGroup);
        btnDryCleaning.setToggleGroup(serviceGroup);
        btnIroning.setToggleGroup(serviceGroup);
        btnWashFold.setSelected(true);

        serviceGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == btnWashFold) selectedService = "Wash & Fold";
            else if (newVal == btnDryCleaning) selectedService = "Dry Cleaning";
            else if (newVal == btnIroning) selectedService = "Ironing";
            else if (newVal == null) { btnWashFold.setSelected(true); selectedService = "Wash & Fold"; }
            updatePriceDisplay();
        });
    }

    private void setupSpinner() {
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 100.0, 5.0, 0.5);
        quantitySpinner.setValueFactory(factory);
        quantitySpinner.valueProperty().addListener((obs, old, newVal) -> updatePriceDisplay());
        quantitySpinner.setEditable(true);
    }

    private void setupTable() {
        colItemDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("formattedSubtotal"));

        colItemAction.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");
            {
                deleteBtn.getStyleClass().add("btn-icon-danger");
                deleteBtn.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    orderItems.remove(item);
                    updateTotal();
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : deleteBtn);
                setAlignment(Pos.CENTER);
            }
        });

        itemsTable.setItems(orderItems);
    }

    private void setupAddons() {
        expressAddon.setOnAction(e -> updatePriceDisplay());
        delicateAddon.setOnAction(e -> updatePriceDisplay());
        stainRemovalAddon.setOnAction(e -> updatePriceDisplay());
    }

    private void setupButtons() {
        btnAddItem.setOnAction(e -> addItem());
        btnReset.setOnAction(e -> resetForm());
        btnPlaceOrder.setOnAction(e -> placeOrder());
        btnBack.setOnAction(e -> goBack());
    }

    private void updatePriceDisplay() {
        double qty = quantitySpinner.getValue();
        double basePrice = orderService.calculateItemPrice(selectedService, qty);
        double addons = 0;
        if (expressAddon.isSelected()) addons += OrderService.EXPRESS_ADDON;
        if (delicateAddon.isSelected()) addons += OrderService.DELICATE_ADDON;
        if (stainRemovalAddon.isSelected()) addons += OrderService.STAIN_REMOVAL_ADDON;
        priceLabel.setText(String.format("₱%.0f", basePrice + addons));
    }

    private void addItem() {
        double qty = quantitySpinner.getValue();
        double basePrice = orderService.calculateItemPrice(selectedService, qty);
        double addons = 0;
        StringBuilder desc = new StringBuilder(selectedService + " " + qty + " kg");

        if (expressAddon.isSelected()) { addons += OrderService.EXPRESS_ADDON; desc.append(" + Express"); }
        if (delicateAddon.isSelected()) { addons += OrderService.DELICATE_ADDON; desc.append(" + Delicate"); }
        if (stainRemovalAddon.isSelected()) { addons += OrderService.STAIN_REMOVAL_ADDON; desc.append(" + Stain Removal"); }

        double totalItemPrice = basePrice + addons;
        OrderItem item = new OrderItem(selectedService, desc.toString(), qty, totalItemPrice / qty);
        item.setSubtotal(totalItemPrice);
        orderItems.add(item);

        updateTotal();
        expressAddon.setSelected(false);
        delicateAddon.setSelected(false);
        stainRemovalAddon.setSelected(false);
        updatePriceDisplay();
    }

    private void updateTotal() {
        double total = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
        totalLabel.setText(String.format("₱%.2f", total));
    }

    private void placeOrder() {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty()) { showAlert("Please enter customer name."); return; }
        if (phone.isEmpty()) { showAlert("Please enter phone number."); return; }
        if (orderItems.isEmpty()) { showAlert("Please add at least one item."); return; }

        try {
            Order order = orderService.createOrder(name, phone, new ArrayList<>(orderItems));
            openOrderSummary(order.getOrderNumber());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error creating order: " + e.getMessage());
        }
    }

    private void resetForm() {
        customerNameField.clear();
        customerPhoneField.clear();
        orderItems.clear();
        btnWashFold.setSelected(true);
        selectedService = "Wash & Fold";
        quantitySpinner.getValueFactory().setValue(5.0);
        expressAddon.setSelected(false);
        delicateAddon.setSelected(false);
        stainRemovalAddon.setSelected(false);
        totalLabel.setText("₱0.00");
        updatePriceDisplay();
    }

    private void openOrderSummary(String orderNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_summary.fxml"));
            Parent root = loader.load();
            OrderSummaryController controller = loader.getController();
            controller.loadOrder(orderNumber);
            Stage stage = (Stage) btnPlaceOrder.getScene().getWindow();
            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
