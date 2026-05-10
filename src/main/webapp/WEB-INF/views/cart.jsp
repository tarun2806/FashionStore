<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.fashionstore.model.CartItem" %>

<!DOCTYPE html>
<html lang="en">
<head>
<%
    request.setAttribute("_pageTitle", "Shopping Cart");
    request.setAttribute("_pageCSS", "cart");
%>
<jsp:include page="/WEB-INF/views/partials/head.jsp" />
</head>

<body>

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/partials/navbar.jsp" />

<%
    List<CartItem> cartItems = new ArrayList<>();
    Object obj = request.getAttribute("cartItems");
    if (obj instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<CartItem> temp = (List<CartItem>) obj;
        cartItems = temp;
    }

    Object totalObj = request.getAttribute("cartTotal");
    double cartTotal = (totalObj instanceof Number) ? ((Number) totalObj).doubleValue() : 0.0;

    int totalQty = 0;
    for (CartItem ci : cartItems) totalQty += ci.getQuantity();

    double freeShipThreshold = 999.0;
    double amountAwayFromFreeShip = Math.max(0, freeShipThreshold - cartTotal);
    boolean qualifiesFreeShip = cartTotal >= freeShipThreshold;
%>

<main class="cart-page" data-context-path="<%= request.getContextPath() %>">
    <div class="container">

        <!-- Breadcrumb -->
        <nav class="cart-breadcrumb" aria-label="Breadcrumb">
            <a href="<%= request.getContextPath() %>/home">Home</a>
            <span aria-hidden="true">/</span>
            <span aria-current="page">Shopping Cart</span>
        </nav>

        <!-- Page Header -->
        <div class="cart-header">
            <div class="cart-header-text">
                <h1 class="cart-title">Shopping Cart</h1>
                <p class="cart-count"><%= cartItems.size() %> item<%= cartItems.size() != 1 ? "s" : "" %><% if (totalQty > cartItems.size()) { %> &middot; <%= totalQty %> units<% } %></p>
            </div>
            <% if (!cartItems.isEmpty()) { %>
            <a href="<%= request.getContextPath() %>/products" class="cart-continue-link">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                Continue shopping
            </a>
            <% } %>
        </div>

        <!-- Free shipping progress -->
        <% if (!cartItems.isEmpty()) { %>
        <div class="free-ship-bar <%= qualifiesFreeShip ? "is-complete" : "" %>" role="status">
            <% if (qualifiesFreeShip) { %>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
                <span>You qualify for <strong>FREE shipping</strong></span>
            <% } else { %>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
                <span>Add <strong>₹<%= String.format("%.2f", amountAwayFromFreeShip) %></strong> more to unlock <strong>FREE shipping</strong></span>
            <% } %>
            <div class="free-ship-progress">
                <div class="free-ship-progress-bar" style="width: <%= Math.min(100, (cartTotal / freeShipThreshold) * 100) %>%"></div>
            </div>
        </div>
        <% } %>

        <!-- Error Message -->
        <% String error = (String) session.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="alert alert-error">
                <%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(error) %>
            </div>
            <% session.removeAttribute("error"); %>
        <% } %>

        <% if (!cartItems.isEmpty()) { %>
            <div class="cart-layout">
                <!-- Cart Items List -->
                <section class="cart-items" aria-label="Cart items">
                    <header class="cart-items-toolbar">
                        <span class="cart-items-toolbar-title">Items in your cart</span>
                        <span class="cart-items-toolbar-count"><%= cartItems.size() %> item<%= cartItems.size() != 1 ? "s" : "" %></span>
                    </header>

                    <% for (CartItem item : cartItems) {
                        double itemTotal = item.getPrice() * item.getQuantity();
                    %>
                    <article class="cart-item" data-id="<%= item.getCartItemId() %>">
                        <!-- Image -->
                        <a href="<%= request.getContextPath() %>/product?id=<%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(String.valueOf(item.getProductId())) %>" class="cart-item-image">
                            <img src="<%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(item.getImageUrl()) %>"
                                 alt="<%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(item.getProductName()) %>"
                                 loading="lazy"
                                 onerror="this.src='<%= request.getContextPath() %>/assets/images/placeholder-product.jpg'; this.onerror=null;">
                        </a>

                        <!-- Body: name + meta + actions -->
                        <div class="cart-item-body">
                            <div class="cart-item-info">
                                <a href="<%= request.getContextPath() %>/product?id=<%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(String.valueOf(item.getProductId())) %>" class="cart-item-name"><%= org.apache.commons.text.StringEscapeUtils.escapeHtml4(item.getProductName()) %></a>
                                <ul class="cart-item-meta">
                                    <% if (item.getSizeLabel() != null && !item.getSizeLabel().isEmpty()) { %>
                                        <li><span class="meta-label">Size</span><span class="meta-value"><%= item.getSizeLabel() %></span></li>
                                    <% } %>
                                    <li><span class="meta-label">Unit price</span><span class="meta-value">₹<%= String.format("%.2f", item.getPrice()) %></span></li>
                                    <li class="cart-item-stock in-stock"><span class="stock-dot"></span>In stock</li>
                                </ul>
                            </div>

                            <div class="cart-item-actions">
                                <button type="button" class="cart-action-btn ajax-save-later-btn"
                                        data-id="<%= item.getCartItemId() %>" aria-label="Save for later">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                                    <span>Save for later</span>
                                </button>
                                <button type="button" class="cart-action-btn cart-action-danger ajax-remove-btn"
                                        data-id="<%= item.getCartItemId() %>" aria-label="Remove from cart">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/></svg>
                                    <span>Remove</span>
                                </button>
                            </div>
                        </div>

                        <!-- Quantity stepper -->
                        <div class="cart-item-qty" aria-label="Quantity">
                            <span class="cart-item-qty-label">Qty</span>
                            <div class="qty-stepper">
                                <button type="button" class="qty-btn ajax-qty-btn"
                                        data-action="decrease"
                                        data-id="<%= item.getCartItemId() %>"
                                        data-qty="<%= item.getQuantity() %>"
                                        aria-label="Decrease quantity"
                                        <%= item.getQuantity() <= 1 ? "disabled" : "" %>>
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                </button>
                                <input type="number" class="qty-input"
                                       value="<%= item.getQuantity() %>"
                                       min="1" max="10"
                                       data-id="<%= item.getCartItemId() %>"
                                       aria-label="Item quantity">
                                <button type="button" class="qty-btn ajax-qty-btn"
                                        data-action="increase"
                                        data-id="<%= item.getCartItemId() %>"
                                        data-qty="<%= item.getQuantity() %>"
                                        aria-label="Increase quantity">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                </button>
                            </div>
                        </div>

                        <!-- Line total -->
                        <div class="cart-item-total">
                            <span class="cart-item-total-label">Subtotal</span>
                            <span class="cart-item-total-value" id="item-total-<%= item.getCartItemId() %>">₹<%= String.format("%.2f", itemTotal) %></span>
                        </div>
                    </article>
                    <% } %>
                </section>

                <!-- Order Summary -->
                <div class="cart-summary">
                    <div class="summary-card">
                        <h2 class="summary-title">Order Summary</h2>

                        <div class="summary-row">
                            <span>Subtotal (<%= cartItems.size() %> items)</span>
                            <span>₹<span id="summary-subtotal"><%= String.format("%.2f", cartTotal) %></span></span>
                        </div>
                        <div class="summary-row">
                            <span>Shipping</span>
                            <span class="free-shipping">FREE</span>
                        </div>
                        <div class="summary-row discount" id="discountRow" style="display:none;">
                            <span>Discount</span>
                            <span id="discount">₹0.00</span>
                        </div>

                        <!-- Coupon Code -->
                        <div class="coupon-section">
                            <label for="couponCode" class="coupon-label">Coupon Code</label>
                            <div class="coupon-input-group">
                                <input type="text" id="couponCode" placeholder="Enter code" class="coupon-input">
                                <button type="button" class="btn btn-primary btn-apply" onclick="FashionStore.applyCoupon()">Apply</button>
                            </div>
                            <div id="couponMessage" class="coupon-message"></div>
                        </div>

                        <div class="summary-divider"></div>

                        <div class="summary-row total">
                            <span>Total</span>
                            <span class="total-amount">₹<span id="summary-total"><%= String.format("%.2f", cartTotal) %></span></span>
                        </div>

                        <a href="<%= request.getContextPath() %>/checkout" class="btn btn-primary btn-checkout">Proceed to Checkout</a>

                        <div class="trust-badges">
                            <div class="trust-badge">
                                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                                </svg>
                                <span>Secure Checkout</span>
                            </div>
                            <div class="trust-badge">
                                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M5 13l4 4L19 7"/>
                                </svg>
                                <span>Free Shipping</span>
                            </div>
                            <div class="trust-badge">
                                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                                </svg>
                                <span>Easy Returns</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        <% } else { %>
            <!-- Empty Cart -->
            <div class="empty-cart">
                <div class="empty-icon">
                    <svg width="64" height="64" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="26" cy="56" r="4"/>
                        <circle cx="48" cy="56" r="4"/>
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4 4h8l5.6 28H52l6-22H18"/>
                    </svg>
                </div>
                <h2>Your cart is empty</h2>
                <p>Looks like you haven't added anything yet. Start shopping!</p>
                <a href="<%= request.getContextPath() %>/products" class="btn btn-primary">Browse Products</a>
            </div>
        <% } %>

        <!-- Recommendations -->
        <% if (!cartItems.isEmpty()) { %>
        <section class="cart-recommendations">
            <div class="recommendations-header">
                <h2>You might also like</h2>
                <a href="<%= request.getContextPath() %>/products?tag=trending" class="btn btn-outline">View All</a>
            </div>
            <div class="recommendations-grid">
                <!-- Dynamic recommendations would go here -->
            </div>
        </section>
        <% } %>
    </div>
</main>

<!-- FOOTER -->
<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
