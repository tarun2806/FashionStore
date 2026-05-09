# Process Flows

## Overview

This document describes the key process flows in the FashionStore application, showing the sequence of operations for user interactions and business processes.

## User Registration Flow

```mermaid
sequenceDiagram
    participant User
    participant RegisterForm
    participant RegisterController
    participant Validator
    participant UserService
    participant UserDAO
    participant Database
    participant EmailService
    participant Email
    
    User->>RegisterForm: Fill Registration Form
    RegisterForm->>RegisterController: POST /register
    RegisterController->>Validator: Validate Input
    Validator->>Validator: Check Email Format
    Validator->>Validator: Check Password Strength
    Validator->>Validator: Check Phone Format
    
    alt Validation Success
        Validator-->>RegisterController: Valid
        RegisterController->>UserService: registerUser(user)
        UserService->>UserDAO: registerUser(user)
        UserDAO->>UserDAO: Hash Password with BCrypt
        UserDAO->>Database: INSERT INTO users
        Database-->>UserDAO: Success
        UserDAO-->>UserService: User Created
        UserService-->>RegisterController: Success
        RegisterController->>EmailService: sendWelcomeEmail()
        EmailService->>Email: Send Welcome Email
        Email-->>User: Welcome Email
        RegisterController-->>User: Redirect to Login
    else Validation Failure
        Validator-->>RegisterController: Invalid
        RegisterController-->>User: Error Message
    end
```

## User Login Flow

```mermaid
sequenceDiagram
    participant User
    participant LoginForm
    participant LoginController
    participant RateLimiter
    participant UserService
    participant UserDAO
    participant Database
    participant Session
    participant CSRF
    
    User->>LoginForm: Enter Credentials
    LoginForm->>LoginController: POST /login
    LoginController->>RateLimiter: checkRateLimit()
    
    alt Rate Limit Exceeded
        RateLimiter-->>LoginController: Locked Out
        LoginController-->>User: Error: Too Many Attempts
    else Rate Limit OK
        LoginController->>UserService: loginUser(email, password)
        UserService->>UserDAO: loginUser(email, password)
        UserDAO->>Database: SELECT * FROM users WHERE email = ?
        Database-->>UserDAO: User Record
        UserDAO->>UserDAO: Verify Password with BCrypt
        
        alt Valid Credentials
            UserDAO-->>UserService: User Object
            UserService-->>LoginController: Success
            LoginController->>Session: Create Session
            Session->>CSRF: Generate CSRF Token
            Session-->>LoginController: Session Created
            LoginController-->>User: Redirect to Home
        else Invalid Credentials
            UserDAO-->>UserService: null
            UserService-->>LoginController: Failure
            LoginController->>RateLimiter: Increment Attempt Count
            LoginController-->>User: Error: Invalid Credentials
        end
    end
```

## Product Browsing Flow

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant ProductController
    participant ProductService
    participant CacheService
    participant ProductDAO
    participant Database
    participant View
    
    User->>Browser: Navigate to Products
    Browser->>ProductController: GET /products?category=X
    ProductController->>ProductService: getProductsByCategory(categoryId)
    ProductService->>CacheService: checkCache(cacheKey)
    
    alt Cache Hit
        CacheService-->>ProductService: Cached Products
    else Cache Miss
        ProductService->>ProductDAO: getProductsByCategory(categoryId)
        ProductDAO->>Database: SELECT * FROM products WHERE category_id = ?
        Database-->>ProductDAO: Product List
        ProductDAO-->>ProductService: Product List
        ProductService->>CacheService: updateCache(cacheKey, products)
    end
    
    ProductService-->>ProductController: Product List
    ProductController->>View: Render Products Page
    View-->>Browser: HTML Response
    Browser-->>User: Display Products
```

## Add to Cart Flow

```mermaid
sequenceDiagram
    participant User
    participant CartController
    participant CSRF
    participant CartDAO
    participant ProductSizeDAO
    participant Database
    participant Cache
    
    User->>CartController: POST /cart/add
    CartController->>CSRF: validateToken(request, token)
    
    alt CSRF Valid
        CSRF-->>CartController: Valid
        CartController->>CartDAO: addToCart(cartItem)
        CartDAO->>Database: INSERT INTO cart_items
        Database-->>CartDAO: Success
        CartDAO-->>CartController: Success
        CartController->>Cache: invalidateUserCartCache(userId)
        Cache-->>CartController: Cache Invalidated
        CartController-->>User: JSON: {success: true}
    else CSRF Invalid
        CSRF-->>CartController: Invalid
        CartController-->>User: JSON: {success: false, error: CSRF invalid}
    end
```

## Checkout Flow

```mermaid
sequenceDiagram
    participant User
    participant CheckoutController
    participant CartDAO
    participant ProductSizeDAO
    participant OrderDAO
    participant OrderItemDAO
    participant Database
    participant Transaction
    participant Cache
    
    User->>CheckoutController: POST /checkout
    CheckoutController->>Transaction: beginTransaction()
    Transaction-->>CheckoutController: Transaction Started
    
    CheckoutController->>CartDAO: getCartItems(userId)
    CartDAO-->>CheckoutController: Cart Items
    
    loop For Each Cart Item
        CheckoutController->>ProductSizeDAO: reduceStock(productId, size, quantity)
        ProductSizeDAO->>Database: UPDATE product_sizes SET stock = stock - ?
        Database-->>ProductSizeDAO: Success
    end
    
    CheckoutController->>OrderDAO: createOrder(order)
    OrderDAO->>Database: INSERT INTO orders
    Database-->>OrderDAO: Order ID
    
    loop For Each Cart Item
        CheckoutController->>OrderItemDAO: addOrderItem(orderItem)
        OrderItemDAO->>Database: INSERT INTO order_items
        Database-->>OrderItemDAO: Success
    end
    
    CheckoutController->>CartDAO: clearCart(userId)
    CartDAO->>Database: DELETE FROM cart_items
    Database-->>CartDAO: Success
    
    CheckoutController->>Transaction: commit()
    Transaction-->>CheckoutController: Committed
    
    CheckoutController->>Cache: invalidateUserCartCache(userId)
    Cache-->>CheckoutController: Cache Invalidated
    
    CheckoutController-->>User: Redirect to Success
    
    alt Error Occurs
        CheckoutController->>Transaction: rollback()
        Transaction-->>CheckoutController: Rolled Back
        CheckoutController-->>User: Error Message
    end
```

## Payment Processing Flow

```mermaid
sequenceDiagram
    participant User
    participant PaymentController
    participant PaymentService
    participant PaymentDAO
    participant PaymentGateway
    participant OrderDAO
    participant Database
    participant EmailService
    
    User->>PaymentController: POST /payment/process
    PaymentController->>PaymentService: processPayment(paymentDetails)
    PaymentService->>PaymentGateway: Initiate Payment
    PaymentGateway-->>PaymentService: Payment Status
    
    alt Payment Successful
        PaymentService->>PaymentDAO: createPayment(payment)
        PaymentDAO->>Database: INSERT INTO payments
        Database-->>PaymentDAO: Payment ID
        PaymentDAO-->>PaymentService: Success
        PaymentService->>OrderDAO: updateOrderStatus(orderId, PAID)
        OrderDAO->>Database: UPDATE orders SET status = 'PAID'
        Database-->>OrderDAO: Success
        PaymentService->>EmailService: sendPaymentConfirmation()
        EmailService->>User: Payment Confirmation Email
        PaymentService-->>PaymentController: Success
        PaymentController-->>User: Redirect to Order Confirmation
    else Payment Failed
        PaymentService-->>PaymentController: Failure
        PaymentController-->>User: Error: Payment Failed
    end
```

## Order Status Update Flow

```mermaid
sequenceDiagram
    participant Admin
    participant AdminOrderController
    participant OrderDAO
    participant OrderStatusHistory
    participant Database
    participant EmailService
    participant Customer
    
    Admin->>AdminOrderController: POST /admin/orders/update-status
    AdminOrderController->>OrderDAO: getOrderById(orderId)
    OrderDAO-->>AdminOrderController: Order Details
    
    AdminOrderController->>OrderDAO: updateOrderStatus(orderId, newStatus)
    OrderDAO->>Database: UPDATE orders SET status = ?
    Database-->>OrderDAO: Success
    
    AdminOrderController->>OrderStatusHistory: recordStatusChange(orderId, oldStatus, newStatus)
    OrderStatusHistory->>Database: INSERT INTO order_status_history
    Database-->>OrderStatusHistory: Success
    
    alt Status is SHIPPED
        AdminOrderController->>EmailService: sendShippingNotification()
        EmailService->>Customer: Shipping Email
    else Status is DELIVERED
        AdminOrderController->>EmailService: sendDeliveryNotification()
        EmailService->>Customer: Delivery Email
    end
    
    AdminOrderController-->>Admin: Success
```

## Password Reset Flow

```mermaid
sequenceDiagram
    participant User
    participant ForgotPasswordForm
    participant PasswordResetController
    participant UserDAO
    participant PasswordResetTokenDAO
    participant Database
    participant EmailService
    participant Email
    participant ResetForm
    participant ResetController
    
    User->>ForgotPasswordForm: Enter Email
    ForgotPasswordForm->>PasswordResetController: POST /forgot-password
    PasswordResetController->>UserDAO: getUserByEmail(email)
    UserDAO->>Database: SELECT * FROM users WHERE email = ?
    Database-->>UserDAO: User Record
    UserDAO-->>PasswordResetController: User Object
    
    PasswordResetController->>PasswordResetTokenDAO: createResetToken(userId, token)
    PasswordResetTokenDAO->>Database: INSERT INTO password_reset_tokens
    Database-->>PasswordResetTokenDAO: Success
    
    PasswordResetController->>EmailService: sendPasswordResetEmail(email, resetLink)
    EmailService->>Email: Send Reset Email
    Email-->>User: Reset Email with Token
    
    User->>ResetForm: Click Reset Link
    ResetForm->>ResetController: GET /reset-password?token=xxx
    ResetController->>PasswordResetTokenDAO: validateToken(token)
    PasswordResetTokenDAO->>Database: SELECT * FROM password_reset_tokens WHERE token = ?
    Database-->>PasswordResetTokenDAO: Token Record
    
    alt Token Valid and Not Expired
        PasswordResetTokenDAO-->>ResetController: Valid
        ResetController-->>User: Display Reset Form
        User->>ResetForm: Enter New Password
        ResetForm->>ResetController: POST /reset-password
        ResetController->>UserDAO: changePassword(userId, newPassword)
        UserDAO->>Database: UPDATE users SET password = ?
        Database-->>UserDAO: Success
        ResetController->>PasswordResetTokenDAO: markAsUsed(token)
        PasswordResetTokenDAO->>Database: UPDATE password_reset_tokens SET used = TRUE
        ResetController-->>User: Redirect to Login
    else Token Invalid or Expired
        PasswordResetTokenDAO-->>ResetController: Invalid
        ResetController-->>User: Error: Invalid Token
    end
```

## Product Search Flow

```mermaid
sequenceDiagram
    participant User
    participant SearchForm
    participant SearchController
    participant SearchService
    participant ProductDAO
    participant Database
    participant Cache
    participant SearchAnalytics
    participant View
    
    User->>SearchForm: Enter Search Query
    SearchForm->>SearchController: GET /search?q=query
    SearchController->>SearchService: searchProducts(query, filters)
    SearchService->>Cache: checkCache(cacheKey)
    
    alt Cache Hit
        Cache-->>SearchService: Cached Results
    else Cache Miss
        SearchService->>ProductDAO: searchProducts(query)
        ProductDAO->>Database: SELECT * FROM products WHERE MATCH(product_name, description) AGAINST(?)
        Database-->>ProductDAO: Product List
        ProductDAO-->>SearchService: Results
        SearchService->>Cache: updateCache(cacheKey, results)
    end
    
    SearchService->>SearchAnalytics: logSearch(query, userId, resultCount)
    SearchAnalytics->>Database: INSERT INTO search_analytics
    
    SearchService-->>SearchController: Results
    SearchController->>View: Render Search Results
    View-->>User: Display Results
```

## Review Submission Flow

```mermaid
sequenceDiagram
    participant User
    participant ReviewForm
    participant ReviewController
    participant CSRF
    participant ReviewDAO
    participant ProductDAO
    participant Database
    participant Cache
    
    User->>ReviewForm: Fill Review Form
    ReviewForm->>ReviewController: POST /review/submit
    ReviewController->>CSRF: validateToken(request, token)
    
    alt CSRF Valid
        CSRF-->>ReviewController: Valid
        ReviewController->>ReviewDAO: addReview(review)
        ReviewDAO->>Database: INSERT INTO reviews
        Database-->>ReviewDAO: Success
        
        ReviewController->>ProductDAO: updateProductRating(productId)
        ProductDAO->>Database: UPDATE products SET rating = ?
        Database-->>ProductDAO: Success
        
        ReviewController->>Cache: invalidateProductCache(productId)
        Cache-->>ReviewController: Cache Invalidated
        
        ReviewController-->>User: JSON: {success: true}
    else CSRF Invalid
        CSRF-->>ReviewController: Invalid
        ReviewController-->>User: JSON: {success: false, error: CSRF invalid}
    end
```

## Wishlist Management Flow

```mermaid
sequenceDiagram
    participant User
    participant WishlistController
    participant CSRF
    participant WishlistDAO
    participant Database
    participant Cache
    
    User->>WishlistController: POST /wishlist/add
    WishlistController->>CSRF: validateToken(request, token)
    
    alt CSRF Valid
        CSRF-->>WishlistController: Valid
        WishlistController->>WishlistDAO: addToWishlist(userId, productId)
        WishlistDAO->>Database: INSERT INTO wishlist_items
        Database-->>WishlistDAO: Success
        WishlistDAO-->>WishlistController: Success
        WishlistController->>Cache: invalidateWishlistCache(userId)
        Cache-->>WishlistController: Cache Invalidated
        WishlistController-->>User: JSON: {success: true}
    else CSRF Invalid
        CSRF-->>WishlistController: Invalid
        WishlistController-->>User: JSON: {success: false, error: CSRF invalid}
    end
    
    User->>WishlistController: GET /wishlist
    WishlistController->>WishlistDAO: getWishlistByUserId(userId)
    WishlistDAO->>Database: SELECT * FROM wishlist_items WHERE user_id = ?
    Database-->>WishlistDAO: Wishlist Items
    WishlistDAO-->>WishlistController: Items
    WishlistController-->>User: Display Wishlist
```

## Admin Product Management Flow

```mermaid
sequenceDiagram
    participant Admin
    participant AdminProductController
    participant ProductService
    participant ProductDAO
    participant ProductSizeDAO
    participant Database
    participant Cache
    
    Admin->>AdminProductController: GET /admin/products
    AdminProductController->>ProductService: getAllProducts()
    ProductService->>ProductDAO: getAllProducts()
    ProductDAO->>Database: SELECT * FROM products
    Database-->>ProductDAO: Product List
    ProductDAO-->>ProductService: Products
    ProductService-->>AdminProductController: Products
    AdminProductController-->>Admin: Display Product List
    
    Admin->>AdminProductController: POST /admin/products/add
    AdminProductController->>ProductService: addProduct(product)
    ProductService->>ProductDAO: addProduct(product)
    ProductDAO->>Database: INSERT INTO products
    Database-->>ProductDAO: Product ID
    ProductDAO-->>ProductService: Success
    ProductService->>Cache: invalidateProductCache()
    Cache-->>ProductService: Cache Invalidated
    ProductService-->>AdminProductController: Success
    AdminProductController-->>Admin: Redirect to Product List
```

## Cache Invalidation Flow

```mermaid
sequenceDiagram
    participant Service
    participant CacheService
    participant CacheStore
    participant PatternMatcher
    
    Service->>CacheService: invalidate(pattern)
    CacheService->>PatternMatcher: matchPattern(pattern)
    PatternMatcher-->>CacheService: Matching Keys
    
    loop For Each Matching Key
        CacheService->>CacheStore: remove(key)
        CacheStore-->>CacheService: Removed
    end
    
    CacheService-->>Service: Invalidated
```

## Email Notification Flow

```mermaid
sequenceDiagram
    participant Service
    participant EmailService
    participant EmailQueue
    participant EmailWorker
    participant SMTP
    participant User
    
    Service->>EmailService: sendEmail(to, subject, content)
    EmailService->>EmailQueue: enqueue(email)
    EmailQueue-->>EmailService: Queued
    
    EmailWorker->>EmailQueue: dequeue()
    EmailQueue-->>EmailWorker: Email
    
    EmailWorker->>SMTP: Connect
    SMTP-->>EmailWorker: Connected
    EmailWorker->>SMTP: Send Email
    SMTP-->>EmailWorker: Sent
    EmailWorker->>EmailQueue: markAsSent(emailId)
    EmailQueue->>Database: UPDATE email_notifications SET status = 'sent'
    
    alt Send Failed
        EmailWorker->>SMTP: Send Email
        SMTP-->>EmailWorker: Error
        EmailWorker->>EmailQueue: markAsFailed(emailId, error)
        EmailQueue->>Database: UPDATE email_notifications SET status = 'failed'
        EmailWorker->>EmailQueue: requeue(emailId)
    end
    
    SMTP->>User: Email Delivered
```
