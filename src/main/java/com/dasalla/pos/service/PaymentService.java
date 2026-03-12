package com.dasalla.pos.service;

import com.dasalla.pos.dao.OrderDAO;
import com.dasalla.pos.dao.TransactionDAO;
import com.dasalla.pos.model.Transaction;
import com.dasalla.pos.util.SessionManager;

public class PaymentService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public boolean processCashPayment(int orderId, double amount) {
        boolean paid = orderDAO.markAsPaid(orderId, "CASH", null);
        if (paid) {
            Transaction t = new Transaction();
            t.setOrderId(orderId);
            t.setAmount(amount);
            t.setPaymentMethod("CASH");
            t.setProcessedBy(SessionManager.getCurrentUser() != null
                    ? SessionManager.getCurrentUser().getId() : 1);
            transactionDAO.insert(t);
            orderDAO.updateStatus(orderId, "PROCESSING");
        }
        return paid;
    }

    public boolean processGCashPayment(int orderId, double amount, String gcashRef) {
        boolean paid = orderDAO.markAsPaid(orderId, "GCASH", gcashRef);
        if (paid) {
            Transaction t = new Transaction();
            t.setOrderId(orderId);
            t.setAmount(amount);
            t.setPaymentMethod("GCASH");
            t.setGcashReference(gcashRef);
            t.setProcessedBy(SessionManager.getCurrentUser() != null
                    ? SessionManager.getCurrentUser().getId() : 1);
            transactionDAO.insert(t);
            orderDAO.updateStatus(orderId, "PROCESSING");
        }
        return paid;
    }
}
