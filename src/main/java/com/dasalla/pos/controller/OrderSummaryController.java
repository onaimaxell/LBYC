package com.dasalla.pos.controller;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.model.OrderItem;
import com.dasalla.pos.service.OrderService;
import com.dasalla.pos.service.PaymentService;
import com.dasalla.pos.service.QRCodeService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrderSummaryController {

    @FXML private Label orderNumberLabel;
    @FXML private ImageView barcodeImageView;
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;
    @FXML private Button btnPay;
    @FXML private Button btnUpdateStatus;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button btnBack;
    @FXML private ImageView receiptQRView;
    @FXML private ImageView lookupQRView;

    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();
    private final QRCodeService qrCodeService = new QRCodeService();
    private Order currentOrder;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("PENDING", "PROCESSING", "COMPLETED", "CLAIMED");
        btnUpdateStatus.setOnAction(e -> updateStatus());
        btnBack.setOnAction(e -> goBack());
    }

    public void loadOrder(String orderNumber) {
        currentOrder = orderService.getOrderByNumber(orderNumber);
        if (currentOrder == null) return;
        populateUI();
    }

    private void populateUI() {
        orderNumberLabel.setText("#" + currentOrder.getOrderNumber());
        statusCombo.setValue(currentOrder.getStatus());

        // QR code
        Image qr = qrCodeService.generateOrderLookupQR(currentOrder.getOrderNumber(), 80);
        if (qr != null && receiptQRView != null) receiptQRView.setImage(qr);

        itemsContainer.getChildren().clear();
        for (OrderItem item : currentOrder.getItems()) {
            HBox row = new HBox();
            row.getStyleClass().add("item-row");
            Label desc = new Label(item.getDescription());
            desc.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(desc, javafx.scene.layout.Priority.ALWAYS);
            Label price = new Label(item.getFormattedSubtotal());
            price.getStyleClass().add("item-price");
            row.getChildren().addAll(desc, price);
            itemsContainer.getChildren().add(row);
        }

        totalLabel.setText(String.format("₱%.2f", currentOrder.getTotalAmount()));

        // Pay button
        if ("PAID".equals(currentOrder.getPaymentStatus())) {
            btnPay.setText("✓ Paid (" + currentOrder.getPaymentMethod() + ")");
            btnPay.setDisable(true);
            btnPay.getStyleClass().add("btn-paid");
        } else {
            btnPay.setDisable(false);
            btnPay.setOnAction(e -> openPaymentDialog());
        }
    }

    private void openPaymentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Parent root = loader.load();
            PaymentController controller = loader.getController();
            controller.setOrder(currentOrder);
            controller.setOnPaymentComplete(() -> {
                currentOrder = orderService.getOrderById(currentOrder.getId());
                populateUI();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Process Payment");
            Scene scene = new Scene(root, 380, 550);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        String newStatus = statusCombo.getValue();
        if (newStatus != null && currentOrder != null) {
            boolean success = orderService.updateStatus(currentOrder.getId(), newStatus);
            if (success) {
                currentOrder.setStatus(newStatus);
                if ("COMPLETED".equals(newStatus)) {
                    showInfo("Status updated! SMS notification sent to " + currentOrder.getCustomerPhone());
                } else {
                    showInfo("Status updated to " + newStatus);
                }
            }
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root, 420, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
