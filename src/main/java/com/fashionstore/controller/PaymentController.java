package com.fashionstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fashionstore.dao.CartDAO;
import com.fashionstore.daoimpl.CartDAOImpl;
import com.fashionstore.model.CartItem;
import com.fashionstore.model.Order;
import com.fashionstore.model.Payment;
import com.fashionstore.service.PaymentService;
import com.fashionstore.service.StripePaymentService;
import com.fashionstore.util.AuditLogger;
import com.fashionstore.util.DBConnection;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment controller for handling payment operations
 * Supports Razorpay, Stripe, and COD
 */
@WebServlet("/payment")
public class PaymentController extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private PaymentService paymentService;
    private StripePaymentService stripePaymentService;
    private CartDAO cartDAO;
    
    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
        stripePaymentService = new StripePaymentService();
        cartDAO = new CartDAOImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        
        if ("success".equals(action)) {
            handlePaymentSuccess(req, resp);
        } else if ("failure".equals(action)) {
            handlePaymentFailure(req, resp);
        } else if ("stripe-webhook".equals(action)) {
            handleStripeWebhook(req, resp);
        } else if ("webhook".equals(action)) {
            handleWebhook(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        
        if ("initiate".equals(action)) {
            initiatePayment(req, resp);
        } else if ("verify".equals(action)) {
            verifyPayment(req, resp);
        } else if ("stripe-webhook".equals(action)) {
            handleStripeWebhook(req, resp);
        } else if ("webhook".equals(action)) {
            handleWebhook(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    
    /**
     * Initiate payment for an order
     */
    private void initiatePayment(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String paymentMethod = req.getParameter("paymentMethod");
        
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Payment method is required");
            return;
        }
        
        try {
            // Get cart items
            List<CartItem> cartItems = cartDAO.getCartItemsByUserId(userId);
            if (cartItems.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cart is empty");
                return;
            }
            
            // Calculate total
            double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
            
            // Create order (pending payment)
            int orderId = createOrder(userId, total, paymentMethod, req);
            
            if (orderId == -1) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create order");
                return;
            }
            
            // Create payment record
            if ("COD".equals(paymentMethod)) {
                paymentService.processCODPayment(orderId, BigDecimal.valueOf(total), req);
                // For COD, redirect directly to success
                resp.sendRedirect(req.getContextPath() + "/payment?action=success&orderId=" + orderId);
            } else if ("RAZORPAY".equals(paymentMethod)) {
                String razorpayOrderId = "rzp_order_" + System.currentTimeMillis();
                paymentService.processRazorpayPayment(orderId, BigDecimal.valueOf(total), razorpayOrderId, req);
                // Return Razorpay order ID to frontend
                resp.setContentType("application/json");
                resp.getWriter().write("{\"razorpayOrderId\":\"" + razorpayOrderId + "\",\"amount\":" + (int)(total * 100) + ",\"orderId\":" + orderId + "}");
            } else if ("STRIPE".equals(paymentMethod)) {
                // Create real Stripe Payment Intent
                try {
                    if (!stripePaymentService.isConfigured()) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Stripe is not configured");
                        return;
                    }
                    
                    // Get user email from session
                    String email = (String) session.getAttribute("email");
                    String name = (String) session.getAttribute("fullName");
                    
                    // Create metadata for the payment
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("order_id", String.valueOf(orderId));
                    metadata.put("user_id", String.valueOf(userId));
                    
                    // Create Stripe Payment Intent
                    PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                        BigDecimal.valueOf(total),
                        "INR",
                        email,
                        name,
                        "FashionStore Order #" + orderId,
                        metadata,
                        req
                    );
                    
                    // Create payment record with Stripe payment intent ID
                    paymentService.processStripePayment(orderId, BigDecimal.valueOf(total), paymentIntent.getId(), req);
                    
                    // Return Stripe client secret to frontend
                    resp.setContentType("application/json");
                    resp.getWriter().write("{\"clientSecret\":\"" + paymentIntent.getClientSecret() + "\",\"paymentIntentId\":\"" + paymentIntent.getId() + "\",\"amount\":" + (int)(total * 100) + ",\"orderId\":" + orderId + "}");
                    
                } catch (StripeException e) {
                    logger.error("Stripe error in PaymentController.initiatePayment: {}", e.getMessage(), e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating Stripe payment intent");
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid payment method");
            }
            
        } catch (Exception e) {
            logger.error("Error in PaymentController.initiatePayment: {}", e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error initiating payment");
        }
    }
    
    /**
     * Verify payment after completion
     */
    private void verifyPayment(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String paymentMethod = req.getParameter("paymentMethod");
        int orderId = Integer.parseInt(req.getParameter("orderId"));
        
        try {
            if ("RAZORPAY".equals(paymentMethod)) {
                String razorpayOrderId = req.getParameter("razorpayOrderId");
                String razorpayPaymentId = req.getParameter("razorpayPaymentId");
                String razorpaySignature = req.getParameter("razorpaySignature");
                
                // Verify signature to prevent fake confirmations
                boolean isValid = paymentService.verifyRazorpaySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature, req);
                
                if (!isValid) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid payment signature");
                    return;
                }
                
                // Update payment status
                Payment payment = paymentService.getPaymentByOrderId(orderId);
                if (payment != null) {
                    paymentService.handlePaymentSuccess(payment.getPaymentId(), razorpayPaymentId, req);
                    paymentService.markPaymentVerified(payment.getPaymentId(), razorpayPaymentId, req);
                }
                
                resp.sendRedirect(req.getContextPath() + "/payment?action=success&orderId=" + orderId);
                
            } else if ("STRIPE".equals(paymentMethod)) {
                String stripePaymentIntentId = req.getParameter("paymentIntentId");
                
                // Update payment status
                Payment payment = paymentService.getPaymentByOrderId(orderId);
                if (payment != null) {
                    paymentService.handlePaymentSuccess(payment.getPaymentId(), stripePaymentIntentId, req);
                }
                
                resp.sendRedirect(req.getContextPath() + "/payment?action=success&orderId=" + orderId);
            }
            
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/payment?action=failure&orderId=" + orderId);
        }
    }
    
    /**
     * Handle payment success page
     */
    private void handlePaymentSuccess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int orderId = Integer.parseInt(req.getParameter("orderId"));
        
        try {
            // Get order details
            Order order = getOrderById(orderId);
            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/views/payment-success.jsp").forward(req, resp);
            
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading payment success page");
        }
    }
    
    /**
     * Handle payment failure page
     */
    private void handlePaymentFailure(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int orderId = Integer.parseInt(req.getParameter("orderId"));
        
        try {
            // Get order details
            Order order = getOrderById(orderId);
            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/views/payment-failure.jsp").forward(req, resp);
            
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading payment failure page");
        }
    }
    
    /**
     * Handle webhook callbacks from payment gateways
     */
    private void handleWebhook(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read webhook payload
        StringBuilder buffer = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        
        String payload = buffer.toString();
        
        try {
            // Parse webhook payload (simplified - in production, use proper JSON parsing)
            String webhookId = "webhook_" + System.currentTimeMillis();
            
            // Extract payment details from webhook
            String paymentId = extractPaymentIdFromWebhook(payload);
            
            if (paymentId != null) {
                Payment payment = paymentService.getPaymentByTransactionId(paymentId);
                if (payment != null) {
                    paymentService.markPaymentVerified(payment.getPaymentId(), webhookId, req);
                }
            }
            
            resp.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Handle Stripe webhook with signature verification
     */
    private void handleStripeWebhook(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read webhook payload
        StringBuilder buffer = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        
        String payload = buffer.toString();
        String signatureHeader = req.getHeader("Stripe-Signature");
        
        try {
            // Verify webhook signature
            boolean isValid = stripePaymentService.verifyWebhookSignature(payload, signatureHeader, req);
            
            if (!isValid) {
                logger.warn("Invalid Stripe webhook signature");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Parse webhook event
            com.stripe.model.Event event = stripePaymentService.parseWebhookEvent(payload);
            
            // Handle different event types
            String eventType = event.getType();
            
            switch (eventType) {
                case "payment_intent.succeeded":
                    stripePaymentService.handlePaymentSucceeded(event, req);
                    break;
                case "payment_intent.payment_failed":
                    stripePaymentService.handlePaymentFailed(event, req);
                    break;
                case "charge.refunded":
                    // Handle refund if needed
                    logger.info("Stripe refund received: {}", event.getId());
                    break;
                default:
                    logger.info("Unhandled Stripe webhook event type: {}", eventType);
            }
            
            resp.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            logger.error("Error handling Stripe webhook: {}", e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Create order with transactional safety
     */
    private int createOrder(int userId, double total, String paymentMethod, HttpServletRequest req) {
        String sql = "INSERT INTO orders (user_id, total_amount, payment_method, status) VALUES (?, ?, ?, 'PENDING')";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, userId);
            ps.setDouble(2, total);
            ps.setString(3, paymentMethod);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        AuditLogger.log("ORDER_CREATED", "Order created: " + orderId + " for user: " + userId, 
                                       String.valueOf(userId), req);
                        return orderId;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error in PaymentController.createOrder: {}", e.getMessage(), e);
        }
        return -1;
    }
    
    /**
     * Get order by ID
     */
    private Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    order.setStatus(rs.getString("status"));
                    order.setOrderDate(rs.getDate("order_date"));
                    return order;
                }
            }
        } catch (SQLException e) {
            logger.error("Error in PaymentController.getOrderById: {}", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Extract payment ID from webhook payload (simplified)
     */
    private String extractPaymentIdFromWebhook(String payload) {
        // In production, parse JSON and extract relevant fields
        if (payload.contains("razorpay_payment_id")) {
            int start = payload.indexOf("razorpay_payment_id") + 21;
            int end = payload.indexOf("\"", start);
            return payload.substring(start, end);
        }
        return null;
    }
}
