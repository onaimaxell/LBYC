package com.dasalla.pos.service;

import com.dasalla.pos.dao.TransactionDAO;
import com.dasalla.pos.model.Transaction;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public boolean exportTransactionsToCSV(String filePath, String keyword, String dateFrom, String dateTo) {
        List<Transaction> transactions = transactionDAO.search(keyword, dateFrom, dateTo);
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Header info
            writer.writeNext(new String[]{
                "ID", "Order Number", "Customer", "Amount", "Payment Method",
                "GCash Reference", "Processed By", "Date"
            });
            // Actual data
            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                    String.valueOf(t.getId()),
                    t.getOrderNumber(),
                    t.getCustomerName(),
                    String.format("%.2f", t.getAmount()),
                    t.getPaymentMethod(),
                    t.getGcashReference() != null ? t.getGcashReference() : "",
                    t.getProcessedByName() != null ? t.getProcessedByName() : "",
                    t.getCreatedAt()
                });
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAll();
    }

    public List<Transaction> searchTransactions(String keyword, String dateFrom, String dateTo) {
        return transactionDAO.search(keyword, dateFrom, dateTo);
    }
}
