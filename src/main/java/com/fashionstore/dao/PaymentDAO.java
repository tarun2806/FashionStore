package com.fashionstore.dao;

import com.fashionstore.model.Payment;
import java.util.List;

/**
 * DAO interface for Payment operations
 */
public interface PaymentDAO {
    int createPayment(Payment payment);
    Payment getPaymentById(int paymentId);
    Payment getPaymentByTransactionId(String transactionId);
    Payment getPaymentByOrderId(int orderId);
    Payment getPaymentByStripePaymentIntentId(String stripePaymentIntentId);
    boolean updatePaymentStatus(int paymentId, String status);
    boolean updatePaymentVerification(int paymentId, boolean verified, String webhookId);
    boolean updatePaymentFailureReason(int paymentId, String failureReason);
    boolean updateOrderPaymentStatus(int orderId, String paymentStatus, String transactionId);
    List<Payment> getPaymentsByOrderId(int orderId);
    List<Payment> getPaymentsByUserId(int userId);
    boolean logPaymentDetails(Payment payment);
}
