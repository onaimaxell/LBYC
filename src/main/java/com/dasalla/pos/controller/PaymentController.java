package com.dasalla.pos.controller;

import com.dasalla.pos.model.Order;
import com.dasalla.pos.service.PaymentService;
import com.dasalla.pos.service.QRCodeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PaymentController {

    @FXML private Label orderNumLabel;
    @FXML private Label amountLabel;
    @FXML private ToggleButton btnCash;
    @FXML private ToggleButton btnGCash;
    @FXML private ToggleGroup paymentGroup;
    @FXML private VBox gcashPanel;
    @FXML private ImageView gcashQRView;
    @FXML private TextField gcashRefField;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel;
    @FXML private VBox cashPanel;
    @FXML private Button btnConfirmPay;
    @FXML private Button btnCancel;

    private Order order;
    private Runnable onPaymentComplete;
    private final PaymentService paymentService = new PaymentService();
    private final QRCodeService qrCodeService = new QRCodeService();

    @FXML
    public void initialize() {
        paymentGroup = new ToggleGroup();
        btnCash.setToggleGroup(paymentGroup);
        btnGCash.setToggleGroup(paymentGroup);
        btnCash.setSelected(true);

        gcashPanel.setVisible(false);
        gcashPanel.setManaged(false);
        cashPanel.setVisible(true);
        cashPanel.setManaged(true);

        paymentGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            boolean isGCash = newVal == btnGCash;
            gcashPanel.setVisible(isGCash);
            gcashPanel.setManaged(isGCash);
            cashPanel.setVisible(!isGCash);
            cashPanel.setManaged(!isGCash);

            if (isGCash) {
                Image qr = qrCodeService.getGCashQRCode(180);
                if (qr != null) gcashQRView.setImage(qr);
            }
        });

        cashReceivedField.textProperty().addListener((obs, old, val) -> {
            try {
                double received = Double.parseDouble(val);
                double change = received - (order != null ? order.getTotalAmount() : 0);
                changeLabel.setText("Change: ₱" + String.format("%.2f", Math.max(0, change)));
            } catch (NumberFormatException e) {
                changeLabel.setText("Change: ₱0.00");
            }
        });

        btnConfirmPay.setOnAction(e -> processPayment());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setOrder(Order order) {
        this.order = order;
        orderNumLabel.setText("Order #" + order.getOrderNumber());
        amountLabel.setText(String.format("₱%.2f", order.getTotalAmount()));
    }

    public void setOnPaymentComplete(Runnable callback) {
        this.onPaymentComplete = callback;
    }

    private void processPayment() {
        if (order == null) return;

        boolean isCash = btnCash.isSelected();
        boolean success;

        if (isCash) {
            String cashText = cashReceivedField.getText().trim();
            if (cashText.isEmpty()) { showError("Please enter cash received amount."); return; }
            try {
                double received = Double.parseDouble(cashText);
                if (received < order.getTotalAmount()) {
                    showError("Insufficient cash. Amount due: ₱" + String.format("%.2f", order.getTotalAmount()));
                    return;
                }
                success = paymentService.processCashPayment(order.getId(), order.getTotalAmount());
            } catch (NumberFormatException e) {
                showError("Invalid cash amount.");
                return;
            }
        } else {
            // GCash (for testing stuff)
            String ref = gcashRefField.getText().trim();
            success = paymentService.processGCashPayment(order.getId(), order.getTotalAmount(), ref);
        }

        if (success) {
            showSuccess("Payment confirmed successfully!");
            if (onPaymentComplete != null) onPaymentComplete.run();
            closeDialog();
        } else {
            showError("Payment processing failed. Please try again.");
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
