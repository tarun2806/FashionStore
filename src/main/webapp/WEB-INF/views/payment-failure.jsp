<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.fashionstore.model.Order" %>

<!DOCTYPE html>
<html lang="en">
<head>
<%
    request.setAttribute("_pageTitle", "Payment Failed");
    request.setAttribute("_pageCSS", "success");
%>
<jsp:include page="/WEB-INF/views/partials/head.jsp" />
<style>
.payment-failure-page {
    min-height: calc(100vh - 140px);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
}

.error-container {
    max-width: 480px;
    width: 100%;
    text-align: center;
    animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.error-icon {
    width: 96px;
    height: 96px;
    margin: 0 auto 32px;
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 20px 40px -12px rgba(239, 68, 68, 0.4);
    animation: scaleIn 0.5s ease-out 0.2s both;
}

@keyframes scaleIn {
    from {
        transform: scale(0);
        opacity: 0;
    }
    to {
        transform: scale(1);
        opacity: 1;
    }
}

.error-icon svg {
    color: white;
    width: 48px;
    height: 48px;
}

.error-title {
    font-size: 32px;
    font-weight: 700;
    color: #101010;
    margin-bottom: 12px;
    letter-spacing: -0.02em;
}

.error-message {
    font-size: 16px;
    color: #6b7280;
    line-height: 1.6;
    margin-bottom: 40px;
}

.order-summary {
    background: white;
    border-radius: 16px;
    padding: 28px;
    margin-bottom: 32px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    text-align: left;
}

.order-summary h2 {
    font-size: 18px;
    font-weight: 600;
    color: #101010;
    margin-bottom: 20px;
    letter-spacing: -0.01em;
}

.order-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16px;
    border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
    padding-bottom: 0;
    border-bottom: none;
}

.info-label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
}

.info-value {
    font-size: 14px;
    color: #101010;
    font-weight: 600;
}

.status-failed {
    color: #dc2626;
    background: #fee2e2;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.error-actions {
    display: flex;
    gap: 12px;
    flex-direction: column;
}

.error-actions .btn {
    padding: 14px 28px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    text-align: center;
    transition: all 0.2s ease;
}

.btn-primary {
    background: #101010;
    color: white;
    border: 2px solid #101010;
}

.btn-primary:hover {
    background: #2a2a2a;
    border-color: #2a2a2a;
    transform: translateY(-2px);
    box-shadow: 0 8px 16px -4px rgba(0, 0, 0, 0.3);
}

.btn-outline {
    background: white;
    color: #101010;
    border: 2px solid #e5e7eb;
}

.btn-outline:hover {
    background: #f9fafb;
    border-color: #d1d5db;
}

@media (min-width: 640px) {
    .error-actions {
        flex-direction: row;
    }
    
    .error-actions .btn {
        flex: 1;
    }
}
</style>
</head>

<body>

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/partials/navbar.jsp" />

<%
    Order order = (Order) request.getAttribute("order");
%>

<main class="site-main payment-failure-page">
    <div class="container">
        <div class="error-container">
            <div class="error-icon">
                <svg width="48" height="48" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"/>
                </svg>
            </div>
            
            <h1 class="error-title">Payment Failed</h1>
            <p class="error-message">We couldn't process your payment. Please try again or choose a different payment method.</p>
            
            <% if (order != null) { %>
            <div class="order-summary">
                <h2>Order Details</h2>
                <div class="order-info">
                    <div class="info-item">
                        <span class="info-label">Order ID</span>
                        <span class="info-value">#<%= order.getOrderId() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Payment Method</span>
                        <span class="info-value"><%= order.getPaymentMethod() %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Total Amount</span>
                        <span class="info-value">₹<%= String.format("%.2f", order.getTotalAmount()) %></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Status</span>
                        <span class="info-value status-failed">Payment Failed</span>
                    </div>
                </div>
            </div>
            <% } %>
            
            <div class="error-actions">
                <a href="<%= request.getContextPath() %>/cart" class="btn btn-primary">Try Again</a>
                <a href="<%= request.getContextPath() %>/products" class="btn btn-outline">Continue Shopping</a>
            </div>
        </div>
    </div>
</main>

<!-- FOOTER -->
<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
