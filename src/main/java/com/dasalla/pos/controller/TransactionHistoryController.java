package com.dasalla.pos.controller;

import com.dasalla.pos.model.Transaction;
import com.dasalla.pos.service.ReportService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class TransactionHistoryController {

    @FXML private TextField searchField;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private Button btnSearch;
    @FXML private Button btnExport;
    @FXML private Button btnBack;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colOrderNum;
    @FXML private TableColumn<Transaction, String> colCustomer;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colMethod;
    @FXML private TableColumn<Transaction, String> colRef;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private Label totalLabel;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        setupTable();
        loadAll();
        btnSearch.setOnAction(e -> search());
        btnExport.setOnAction(e -> exportCSV());
        btnBack.setOnAction(e -> goBack());
        searchField.setOnAction(e -> search());
    }

    private void setupTable() {
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colRef.setCellValueFactory(new PropertyValueFactory<>("gcashReference"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void loadAll() {
        var list = reportService.getAllTransactions();
        transactionTable.setItems(FXCollections.observableArrayList(list));
        updateTotal(list);
    }

    private void search() {
        String keyword = searchField.getText().trim();
        String dateFrom = dateFromPicker.getValue() != null ? dateFromPicker.getValue().toString() : null;
        String dateTo = dateToPicker.getValue() != null ? dateToPicker.getValue().toString() : null;
        var list = reportService.searchTransactions(keyword, dateFrom, dateTo);
        transactionTable.setItems(FXCollections.observableArrayList(list));
        updateTotal(list);
    }

    private void updateTotal(java.util.List<Transaction> list) {
        double total = list.stream().mapToDouble(Transaction::getAmount).sum();
        totalLabel.setText("Total: ₱" + String.format("%.2f", total) + " (" + list.size() + " transactions)");
    }

    private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Transactions");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("transactions.csv");
        File file = chooser.showSaveDialog(btnExport.getScene().getWindow());
        if (file != null) {
            String keyword = searchField.getText().trim();
            String dateFrom = dateFromPicker.getValue() != null ? dateFromPicker.getValue().toString() : null;
            String dateTo = dateToPicker.getValue() != null ? dateToPicker.getValue().toString() : null;
            boolean ok = reportService.exportTransactionsToCSV(file.getAbsolutePath(), keyword, dateFrom, dateTo);
            Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(ok ? "Exported to: " + file.getAbsolutePath() : "Export failed.");
            alert.showAndWait();
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
        } catch (Exception e) { e.printStackTrace(); }
    }
}
