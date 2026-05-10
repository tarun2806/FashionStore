package com.fashionstore.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public class StructuredLogger {

    private final Logger logger;

    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void logLogin(String email, boolean success, String reason) {
        MDC.put("event", "login_attempt");
        MDC.put("email", email);
        MDC.put("success", String.valueOf(success));
        if (reason != null) {
            MDC.put("reason", reason);
        }

        if (success) {
            logger.info("User login successful - email={}", email);
        } else {
            logger.warn("User login failed - email={}, reason={}", email, reason);
        }

        MDC.remove("event");
        MDC.remove("email");
        MDC.remove("success");
        MDC.remove("reason");
    }

    public void logCheckout(String userId, String orderId, double totalAmount, String status) {
        MDC.put("event", "checkout");
        MDC.put("userId", userId);
        MDC.put("orderId", orderId);
        MDC.put("totalAmount", String.valueOf(totalAmount));
        MDC.put("status", status);

        logger.info("Checkout processed - userId={}, orderId={}, amount={}, status={}", userId, orderId, totalAmount, status);

        MDC.remove("event");
        MDC.remove("userId");
        MDC.remove("orderId");
        MDC.remove("totalAmount");
        MDC.remove("status");
    }

    public void logPayment(String orderId, String paymentMethod, double amount, String status) {
        MDC.put("event", "payment");
        MDC.put("orderId", orderId);
        MDC.put("paymentMethod", paymentMethod);
        MDC.put("amount", String.valueOf(amount));
        MDC.put("status", status);

        if (status.equals("success")) {
            logger.info("Payment successful - orderId={}, method={}, amount={}", orderId, paymentMethod, amount);
        } else {
            logger.error("Payment failed - orderId={}, method={}, amount={}, status={}", orderId, paymentMethod, amount, status);
        }

        MDC.remove("event");
        MDC.remove("orderId");
        MDC.remove("paymentMethod");
        MDC.remove("amount");
        MDC.remove("status");
    }

    public void logAdminAction(String userId, String action, String target, String details) {
        MDC.put("event", "admin_action");
        MDC.put("userId", userId);
        MDC.put("action", action);
        MDC.put("target", target);
        if (details != null) {
            MDC.put("details", details);
        }

        logger.info("Admin action performed - userId={}, action={}, target={}, details={}", userId, action, target, details);

        MDC.remove("event");
        MDC.remove("userId");
        MDC.remove("action");
        MDC.remove("target");
        MDC.remove("details");
    }

    public void logInventoryChange(String productId, String productName, int oldStock, int newStock, String reason) {
        MDC.put("event", "inventory_change");
        MDC.put("productId", productId);
        MDC.put("productName", productName);
        MDC.put("oldStock", String.valueOf(oldStock));
        MDC.put("newStock", String.valueOf(newStock));
        MDC.put("change", String.valueOf(newStock - oldStock));
        MDC.put("reason", reason);

        logger.info("Inventory updated - productId={}, product={}, old={}, new={}, reason={}", 
                productId, productName, oldStock, newStock, reason);

        MDC.remove("event");
        MDC.remove("productId");
        MDC.remove("productName");
        MDC.remove("oldStock");
        MDC.remove("newStock");
        MDC.remove("change");
        MDC.remove("reason");
    }

    public void logApiFailure(String endpoint, String method, int statusCode, String errorMessage) {
        MDC.put("event", "api_failure");
        MDC.put("endpoint", endpoint);
        MDC.put("method", method);
        MDC.put("statusCode", String.valueOf(statusCode));
        MDC.put("errorMessage", errorMessage);

        logger.error("API request failed - endpoint={}, method={}, status={}, error={}", 
                endpoint, method, statusCode, errorMessage);

        MDC.remove("event");
        MDC.remove("endpoint");
        MDC.remove("method");
        MDC.remove("statusCode");
        MDC.remove("errorMessage");
    }

    public void logDatabaseFailure(String operation, String query, String errorMessage) {
        MDC.put("event", "database_failure");
        MDC.put("operation", operation);
        MDC.put("errorMessage", errorMessage);

        logger.error("Database operation failed - operation={}, error={}", operation, errorMessage);

        MDC.remove("event");
        MDC.remove("operation");
        MDC.remove("errorMessage");
    }

    public void logCacheOperation(String operation, String key, boolean hit, long duration) {
        MDC.put("event", "cache_operation");
        MDC.put("operation", operation);
        MDC.put("key", key);
        MDC.put("hit", String.valueOf(hit));
        MDC.put("duration", String.valueOf(duration));

        logger.debug("Cache operation - operation={}, key={}, hit={}, duration={}ms", operation, key, hit, duration);

        MDC.remove("event");
        MDC.remove("operation");
        MDC.remove("key");
        MDC.remove("hit");
        MDC.remove("duration");
    }

    public void logPerformance(String operation, long duration, Map<String, String> metadata) {
        MDC.put("event", "performance");
        MDC.put("operation", operation);
        MDC.put("duration", String.valueOf(duration));

        if (metadata != null) {
            metadata.forEach(MDC::put);
        }

        logger.info("Performance metric - operation={}, duration={}ms", operation, duration);

        MDC.remove("event");
        MDC.remove("operation");
        MDC.remove("duration");
        if (metadata != null) {
            metadata.keySet().forEach(MDC::remove);
        }
    }
}
