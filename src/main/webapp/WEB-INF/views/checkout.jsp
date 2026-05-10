<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.fashionstore.model.CartItem" %>
<%@ page import="com.fashionstore.model.Address" %>
<%@ page import="com.fashionstore.model.User" %>
<%@ page import="java.util.Map" %>

<!DOCTYPE html>
<html lang="en">
<head>
<%
    request.setAttribute("_pageTitle", "Checkout");
    request.setAttribute("_pageCSS", "checkout");
%>
<jsp:include page="/WEB-INF/views/partials/head.jsp" />
<!-- Stripe.js -->
<script src="https://js.stripe.com/v3/"></script>
</head>

<body>

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/partials/navbar.jsp" />

<%
    List<CartItem> cartItems = new ArrayList<>();
    Object cartItemsObj = request.getAttribute("cartItems");
    if (cartItemsObj instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<CartItem> temp = (List<CartItem>) cartItemsObj;
        cartItems = temp;
    }

    Object totalObj = request.getAttribute("cartTotal");
    double cartTotal = (totalObj instanceof Number) ? ((Number) totalObj).doubleValue() : 0.0;

    // Get saved addresses
    List<Address> addresses = (List<Address>) request.getAttribute("addresses");
    Address defaultShipping = (Address) request.getAttribute("defaultShipping");
    Address defaultBilling = (Address) request.getAttribute("defaultBilling");

    String selectedShippingId = (String) request.getAttribute("selectedShippingAddressId");
    String selectedBillingId = (String) request.getAttribute("selectedBillingAddressId");
    Boolean useNewShipping = (Boolean) request.getAttribute("useNewShipping");
    Boolean useNewBilling = (Boolean) request.getAttribute("useNewBilling");
    @SuppressWarnings("unchecked")
    Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");

    // Resolve which option should be checked initially:
    //  1) Whatever the user previously selected (after a validation error)
    //  2) The default shipping address
    //  3) The first available shipping address
    //  4) Otherwise: "new"
    String effectiveSelectedId = selectedShippingId;
    if ((effectiveSelectedId == null || effectiveSelectedId.isEmpty()) && !Boolean.TRUE.equals(useNewShipping)) {
        if (defaultShipping != null) {
            effectiveSelectedId = String.valueOf(defaultShipping.getAddressId());
        } else if (addresses != null) {
            for (Address a : addresses) {
                if ("shipping".equals(a.getAddressType()) || "both".equals(a.getAddressType())) {
                    effectiveSelectedId = String.valueOf(a.getAddressId());
                    break;
                }
            }
        }
    }
    boolean showNewForm = Boolean.TRUE.equals(useNewShipping)
            || (effectiveSelectedId == null || effectiveSelectedId.isEmpty() || "new".equals(effectiveSelectedId));
%>

<main class="site-main checkout-page">
    <div class="checkout-container">
        
        <div class="checkout-header">
            <h1 class="checkout-title">Secure Checkout</h1>
            <p class="checkout-subtitle">Complete your order in a few simple steps</p>
        </div>

        <!-- ══ CHECKOUT PROGRESS INDICATOR ══ -->
        <div class="checkout-progress">
            <div class="progress-step active" data-step="1">
                <div class="step-number">1</div>
                <div class="step-label">Shipping</div>
            </div>
            <div class="progress-line"></div>
            <div class="progress-step" data-step="2">
                <div class="step-number">2</div>
                <div class="step-label">Payment</div>
            </div>
            <div class="progress-line"></div>
            <div class="progress-step" data-step="3">
                <div class="step-number">3</div>
                <div class="step-label">Review</div>
            </div>
        </div>

        <div class="checkout-layout">
            
            <!-- ══ SHIPPING & PAYMENT FORM ══ -->
            <div class="checkout-form-col">
                <form action="<%= request.getContextPath() %>/checkout" method="post" class="modern-form" id="checkoutForm">
                    <% String formError = (String) request.getAttribute("error"); %>
                    <div id="form-error" class="checkout-form-error <%= formError != null ? "is-visible" : "" %>">
                        <%= formError != null ? formError : "" %>
                    </div>
                    <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrfToken") != null ? request.getAttribute("csrfToken") : "" %>">
                    
                    <!-- Step 1: Shipping -->
                    <div class="form-section checkout-step-section is-active" id="step1">
                        <h3 class="checkout-section-title">Shipping Information</h3>

                        <% if (addresses != null && !addresses.isEmpty()) { %>
                            <div class="saved-addresses">
                                <div class="saved-addresses-header">
                                    <h4 class="saved-addresses-title">Deliver to a saved address</h4>
                                    <a href="<%= request.getContextPath() %>/account/addresses" class="saved-addresses-manage" target="_blank" rel="noopener">Manage</a>
                                </div>
                                <div class="saved-addresses-list" id="savedAddressesList">
                                    <% for (Address addr : addresses) {
                                        if (!("shipping".equals(addr.getAddressType()) || "both".equals(addr.getAddressType()))) continue;
                                        boolean isChecked = effectiveSelectedId != null
                                                && effectiveSelectedId.equals(String.valueOf(addr.getAddressId()));
                                    %>
                                        <label class="saved-address-card <%= isChecked ? "active" : "" %>" data-address-id="<%= addr.getAddressId() %>">
                                            <input type="radio" name="shippingAddressId" value="<%= addr.getAddressId() %>" <%= isChecked ? "checked" : "" %> data-address-radio>
                                            <div class="saved-address-content">
                                                <div class="saved-address-label">
                                                    <%= addr.getFullName() %>
                                                    <% if (addr.isDefault()) { %>
                                                        <span class="badge-default">Default</span>
                                                    <% } %>
                                                </div>
                                                <div class="saved-address-details">
                                                    <%= addr.getAddressLine1() %><% if (addr.getAddressLine2() != null && !addr.getAddressLine2().isEmpty()) { %>, <%= addr.getAddressLine2() %><% } %><br>
                                                    <%= addr.getCity() %>, <%= addr.getState() %> <%= addr.getPostalCode() %><br>
                                                    <%= addr.getPhone() %>
                                                </div>
                                            </div>
                                        </label>
                                    <% } %>

                                    <label class="saved-address-card saved-address-card--new <%= showNewForm ? "active" : "" %>" id="newAddressOption">
                                        <input type="radio" name="shippingAddressId" value="new" <%= showNewForm ? "checked" : "" %> data-address-radio>
                                        <div class="saved-address-content">
                                            <div class="saved-address-label">+ Add a new address</div>
                                            <div class="saved-address-details">Use a different shipping address</div>
                                        </div>
                                    </label>
                                </div>
                            </div>
                        <% } else { %>
                            <input type="hidden" name="shippingAddressId" value="new">
                        <% } %>

                        <div class="new-address-form<%= showNewForm ? " is-visible" : "" %>" id="newAddressForm" style="display:<%= showNewForm ? "block" : "none" %>;">
                            <% if (addresses != null && !addresses.isEmpty()) { %>
                                <h4 class="saved-addresses-title">New shipping address</h4>
                            <% } %>
                            <div class="form-grid">
                            <div class="form-group full-width">
                                <label for="fullName">Full Name</label>
                                <input type="text" id="fullName" name="fullName" placeholder="John Doe" value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>" required>
                            </div>
                            <div class="form-group full-width">
                                <label for="address">Street Address</label>
                                <input type="text" id="address" name="address" placeholder="123 Fashion St, Area" value="<%= request.getAttribute("address") != null ? request.getAttribute("address") : "" %>" required>
                            </div>
                            <div class="form-group">
                                <label for="city">City</label>
                                <input type="text" id="city" name="city" placeholder="Mumbai" value="<%= request.getAttribute("city") != null ? request.getAttribute("city") : "" %>" required>
                            </div>
                            <div class="form-group">
                                <label for="state">State</label>
                                <input type="text" id="state" name="state" placeholder="Maharashtra" value="<%= request.getAttribute("state") != null ? request.getAttribute("state") : "" %>" required>
                            </div>
                            <div class="form-group <%= fieldErrors != null && fieldErrors.containsKey("postalCode") ? "has-error" : "" %>">
                                <label for="zip">ZIP Code</label>
                                <input type="text" id="zip" name="zip" placeholder="400001" value="<%= request.getAttribute("zip") != null ? request.getAttribute("zip") : "" %>" pattern="[0-9]{6}" title="6-digit ZIP code">
                                <% if (fieldErrors != null && fieldErrors.get("postalCode") != null) { %>
                                    <span class="field-error"><%= fieldErrors.get("postalCode") %></span>
                                <% } %>
                            </div>
                            <div class="form-group <%= fieldErrors != null && fieldErrors.containsKey("phone") ? "has-error" : "" %>">
                                <label for="phone">Phone Number</label>
                                <input type="tel" id="phone" name="phone" placeholder="9876543210" value="<%= request.getAttribute("phone") != null ? request.getAttribute("phone") : "" %>" pattern="[6-9][0-9]{9}" title="10-digit mobile number">
                                <% if (fieldErrors != null && fieldErrors.get("phone") != null) { %>
                                    <span class="field-error"><%= fieldErrors.get("phone") %></span>
                                <% } %>
                            </div>
                        </div>
                        <div class="form-group checkbox-group">
                            <label class="checkbox-label">
                                <input type="checkbox" name="saveAddress" value="true" checked>
                                <span class="checkbox-text">
                                    <span class="checkbox-title">Save this address to my account</span>
                                    <span class="checkbox-hint">Speeds up your future checkouts</span>
                                </span>
                            </label>
                        </div>
                        </div>
                        <div class="shipping-methods">
                            <label class="payment-card">
                                <input type="radio" name="shippingMethod" value="STANDARD" checked>
                                <div class="payment-info">
                                    <span class="payment-name">Standard delivery</span>
                                    <span class="payment-desc">3-6 business days · Free</span>
                                </div>
                            </label>
                            <label class="payment-card">
                                <input type="radio" name="shippingMethod" value="EXPRESS">
                                <div class="payment-info">
                                    <span class="payment-name">Express delivery</span>
                                    <span class="payment-desc">1-2 business days · Calculated at dispatch</span>
                                </div>
                            </label>
                        </div>
                        <button type="button" class="place-order-btn" onclick="FashionStore.validateAndProceedToPayment()">
                            Continue to Payment
                        </button>
                    </div>

                    <!-- Step 2: Payment -->
                    <div class="form-section checkout-step-section" id="step2">
                        <h3 class="checkout-section-title">Payment Method</h3>
                        <div class="payment-options">
                            <label class="payment-card">
                                <input type="radio" name="paymentMethod" value="COD" checked>
                                <div class="payment-info">
                                    <span class="payment-name">Cash on Delivery</span>
                                    <span class="payment-desc">Pay when you receive the package</span>
                                </div>
                            </label>
                            <label class="payment-card">
                                <input type="radio" name="paymentMethod" value="STRIPE">
                                <div class="payment-info">
                                    <span class="payment-name">Credit / Debit Card</span>
                                    <span class="payment-desc">Secure online payment via Stripe</span>
                                </div>
                            </label>
                            <label class="payment-card">
                                <input type="radio" name="paymentMethod" value="RAZORPAY">
                                <div class="payment-info">
                                    <span class="payment-name">UPI / Net Banking</span>
                                    <span class="payment-desc">Pay using Google Pay, PhonePe, or Paytm</span>
                                </div>
                            </label>
                        </div>
                        
                        <!-- Stripe Payment Element Container -->
                        <div id="stripe-payment-element-container" style="display: none; margin: 20px 0;">
                            <div id="stripe-card-element">
                                <!-- Stripe Elements will be mounted here -->
                            </div>
                            <div id="stripe-payment-errors" class="checkout-form-error"></div>
                        </div>
                        
                        <button type="button" class="place-order-btn secondary" onclick="FashionStore.goToCheckoutStep(1)">
                            Back
                        </button>
                        <button type="button" class="place-order-btn" onclick="FashionStore.reviewOrder()">
                            Review Order
                        </button>
                    </div>

                    <!-- Step 3: Review -->
                    <div class="form-section checkout-step-section" id="step3">
                        <h3 class="checkout-section-title">Review Your Order</h3>
                        <div class="order-review">
                            <p>Please review your shipping and payment details before placing the order.</p>
                        </div>
                        <button type="button" class="place-order-btn secondary" onclick="FashionStore.goToCheckoutStep(2)">
                            Back
                        </button>
                        <button type="submit" class="place-order-btn" name="placeOrder" value="true">
                            Complete Purchase – ₹<%= String.format("%.2f", cartTotal) %>
                        </button>
                    </div>
                </form>
            </div>

            <!-- ══ ORDER SUMMARY ══ -->
            <div class="checkout-summary-col">
                <div class="order-summary-card">
                    <h3 class="summary-title">Order Summary</h3>
                    
                    <div class="summary-items">
                        <% if (cartItems != null) { 
                            for (CartItem item : cartItems) { %>
                        <div class="summary-item">
                            <div class="item-img-wrap">
                                <img src="<%= item.getImageUrl() %>" alt="<%= item.getProductName() %>" onerror="this.src='<%= request.getContextPath() %>/assets/images/placeholder-product.jpg'; this.onerror=null;">
                                <span class="item-qty-badge"><%= item.getQuantity() %></span>
                            </div>
                            <div class="item-details">
                                <span class="item-name"><%= item.getProductName() %></span>
                                <span class="item-price">₹<%= String.format("%.2f", item.getPrice() * item.getQuantity()) %></span>
                            </div>
                        </div>
                        <% } 
                        } %>
                    </div>

                    <div class="summary-divider"></div>

                    <div class="summary-totals">
                        <div class="summary-row">
                            <span>Subtotal</span>
                            <span>₹<%= String.format("%.2f", cartTotal) %></span>
                        </div>
                        <div class="summary-row">
                            <span>Shipping</span>
                            <span class="free-text">FREE</span>
                        </div>
                        <div class="summary-divider"></div>
                        <div class="summary-row grand-total">
                            <span>Total</span>
                            <span class="total-amount">₹<%= String.format("%.2f", cartTotal) %></span>
                        </div>
                    </div>
                    <div class="checkout-assurance">
                        <span>SSL secure payment</span>
                        <span>Encrypted order data</span>
                        <span>7-day easy returns</span>
                    </div>
                </div>
            </div>

        </div>

    </div>
</main>

<!-- FOOTER -->
<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

<script>
// Handle address selection - saved vs new
document.addEventListener('DOMContentLoaded', function() {
    const radios = document.querySelectorAll('input[name="shippingAddressId"]');
    const newAddressForm = document.getElementById('newAddressForm');
    const newAddressInputs = newAddressForm
        ? newAddressForm.querySelectorAll('input[name="fullName"], input[name="address"], input[name="city"], input[name="state"], input[name="zip"], input[name="phone"]')
        : [];

    function syncCardActive() {
        document.querySelectorAll('.saved-address-card').forEach(card => {
            const input = card.querySelector('input[type="radio"]');
            card.classList.toggle('active', !!(input && input.checked));
        });
    }

    function toggleAddressForm() {
        const selected = document.querySelector('input[name="shippingAddressId"]:checked');
        const useNew = !selected || selected.value === 'new' || selected.value === '';
        if (newAddressForm) {
            newAddressForm.style.display = useNew ? 'block' : 'none';
            newAddressForm.classList.toggle('is-visible', useNew);
        }
        // Toggle 'required' on new address inputs based on visibility
        newAddressInputs.forEach(inp => {
            if (useNew) {
                inp.setAttribute('required', 'required');
            } else {
                inp.removeAttribute('required');
            }
        });
        syncCardActive();
    }

    radios.forEach(radio => radio.addEventListener('change', toggleAddressForm));
    toggleAddressForm();
});

document.getElementById('checkoutForm').addEventListener('submit', function(e) {
    const form = this;
    const errorEl = document.getElementById('form-error');
    const selectedAddress = document.querySelector('input[name="shippingAddressId"]:checked');
    const selectedPaymentMethod = document.querySelector('input[name="paymentMethod"]:checked');

    const usingNew = !selectedAddress || selectedAddress.value === '' || selectedAddress.value === 'new';

    // If using saved address, skip validation for new address fields
    if (!usingNew) {
        if (errorEl) errorEl.classList.remove('is-visible');
        const btn = form.querySelector('.place-order-btn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Processing...';
        return;
    }

    const requiredFields = ['fullName', 'address', 'city', 'state', 'zip', 'phone'];

    for (const fieldName of requiredFields) {
        const input = form.querySelector('[name="' + fieldName + '"]');
        if (!input || !input.value || !input.value.trim()) {
            e.preventDefault();
            if (errorEl) {
                const pretty = fieldName.replace(/([A-Z])/g, ' $1').toLowerCase();
                errorEl.textContent = 'Please fill in the ' + pretty;
                errorEl.classList.add('is-visible');
            }
            if (input) input.focus();
            return;
        }
    }

    if (errorEl) errorEl.classList.remove('is-visible');

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Processing...';
});

// Stripe payment handling
let stripe;
let elements;
let cardElement;

document.addEventListener('DOMContentLoaded', function() {
    // Handle payment method selection
    const paymentMethodRadios = document.querySelectorAll('input[name="paymentMethod"]');
    const stripeContainer = document.getElementById('stripe-payment-element-container');
    
    paymentMethodRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.value === 'STRIPE') {
                stripeContainer.style.display = 'block';
                initStripeElements();
            } else {
                stripeContainer.style.display = 'none';
            }
        });
    });
});

function initStripeElements() {
    if (stripe) return; // Already initialized
    
    // Initialize Stripe with publishable key (should be passed from server)
    const stripePublishableKey = 'pk_test_your_stripe_publishable_key'; // Replace with actual key from server
    stripe = Stripe(stripePublishableKey);
    
    const elements = stripe.elements({
        appearance: {
            theme: 'stripe',
            variables: {
                colorPrimary: '#101010',
                colorBackground: '#ffffff',
                colorText: '#101010',
            }
        }
    });
    
    cardElement = elements.create('card', {
        style: {
            base: {
                fontSize: '16px',
                color: '#101010',
                '::placeholder': {
                    color: '#888888',
                },
            },
            invalid: {
                color: '#fa755a',
                iconColor: '#fa755a',
            },
        },
    });
    
    cardElement.mount('#stripe-card-element');
    
    // Handle real-time validation errors
    cardElement.on('change', function(event) {
        const errorElement = document.getElementById('stripe-payment-errors');
        if (event.error) {
            errorElement.textContent = event.error.message;
            errorElement.classList.add('is-visible');
        } else {
            errorElement.textContent = '';
            errorElement.classList.remove('is-visible');
        }
    });
}

// Override the review order function to handle Stripe payment
window.FashionStore = window.FashionStore || {};
const originalReviewOrder = window.FashionStore.reviewOrder;

window.FashionStore.reviewOrder = function() {
    const selectedPaymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    
    if (selectedPaymentMethod === 'STRIPE') {
        initiateStripePayment();
    } else {
        if (originalReviewOrder) {
            originalReviewOrder();
        } else {
            FashionStore.goToCheckoutStep(3);
        }
    }
};

async function initiateStripePayment() {
    const form = document.getElementById('checkoutForm');
    const btn = document.querySelector('#step2 .place-order-btn:last-of-type');
    const errorEl = document.getElementById('form-error');
    
    try {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Processing...';
        
        // Collect form data
        const formData = new FormData(form);
        formData.append('action', 'initiate');
        formData.append('paymentMethod', 'STRIPE');
        
        const response = await fetch('<%= request.getContextPath() %>/payment', {
            method: 'POST',
            body: formData
        });
        
        const data = await response.json();
        
        if (!response.ok || !data.clientSecret) {
            throw new Error(data.error || 'Failed to create payment intent');
        }
        
        // Confirm the payment with Stripe
        const { error, paymentIntent } = await stripe.confirmCardPayment(data.clientSecret, {
            payment_method: {
                card: cardElement,
                billing_details: {
                    name: formData.get('fullName'),
                    email: '<%= session.getAttribute("email") %>',
                    phone: formData.get('phone'),
                    address: {
                        line1: formData.get('address'),
                        city: formData.get('city'),
                        state: formData.get('state'),
                        postal_code: formData.get('zip'),
                    }
                }
            }
        });
        
        if (error) {
            throw new Error(error.message);
        }
        
        // Payment successful, redirect to success page
        window.location.href = '<%= request.getContextPath() %>/payment?action=success&orderId=' + data.orderId;
        
    } catch (err) {
        console.error('Stripe payment error:', err);
        if (errorEl) {
            errorEl.textContent = err.message || 'Payment failed. Please try again.';
            errorEl.classList.add('is-visible');
        }
        btn.disabled = false;
        btn.innerHTML = 'Review Order';
    }
}
</script>

</body>
</html>
