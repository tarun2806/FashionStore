# Database Schema

## Overview

The FashionStore database uses MySQL with InnoDB engine, supporting transactions and foreign key constraints. The schema consists of 30 tables organized to support e-commerce functionality.

## Database Configuration

- **Database Name:** fashionstore
- **Engine:** InnoDB
- **Character Set:** utf8mb4
- **Collation:** utf8mb4_general_ci
- **Total Tables:** 30

## Entity Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ CART_ITEMS : has
    USERS ||--o{ WISHLIST_ITEMS : has
    USERS ||--o{ SAVED_ITEMS : has
    USERS ||--o{ ORDERS : places
    USERS ||--o{ REVIEWS : writes
    USERS ||--o{ PAYMENT_METHODS : owns
    USERS ||--o{ ADDRESSES : has
    USERS ||--o{ PASSWORD_RESET_TOKENS : has
    USERS ||--o{ SEARCH_HISTORY : performs
    USERS ||--o{ RECENTLY_VIEWED : views
    USERS ||--o{ COUPON_USAGE : uses
    
    CATEGORIES ||--o{ PRODUCTS : contains
    PRODUCTS ||--o{ PRODUCT_SIZES : has
    PRODUCTS ||--o{ CART_ITEMS : in
    PRODUCTS ||--o{ WISHLIST_ITEMS : in
    PRODUCTS ||--o{ SAVED_ITEMS : in
    PRODUCTS ||--o{ ORDER_ITEMS : in
    PRODUCTS ||--o{ REVIEWS : has
    PRODUCTS ||--o{ PRODUCT_ATTRIBUTES : has
    PRODUCTS ||--o{ PRODUCT_RECOMMENDATIONS : has
    PRODUCTS ||--o{ SEARCH_HISTORY : matches
    PRODUCTS ||--o{ RECENTLY_VIEWED : viewed
    
    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o{ PAYMENTS : has
    ORDERS ||--o{ ORDER_STATUS_HISTORY : tracks
    ORDERS ||--o{ PAYMENT_TRANSACTIONS : has
    ORDERS ||--o{ REFUNDS : has
    ORDERS ||--o{ INVOICES : has
    ORDERS ||--o{ COUPON_USAGE : uses
    ORDERS }o--|| ADDRESSES : billing
    ORDERS }o--|| ADDRESSES : shipping
    
    COUPONS ||--o{ COUPON_USAGE : used_in
    PAYMENT_METHODS ||--o{ PAYMENT_TRANSACTIONS : used_in
    
    CATEGORIES ||--o{ CATEGORIES : parent_of
```

## Core Tables

### USERS Table

```mermaid
graph TB
    subgraph "USERS Table"
        User[USERS]
        ID[user_id: INT PK AUTO_INCREMENT]
        Name[full_name: VARCHAR100 NOT NULL]
        Email[email: VARCHAR100 UNIQUE NOT NULL]
        Phone[phone: VARCHAR15]
        Password[password: VARCHAR255 NOT NULL]
        Gender[gender: VARCHAR10]
        Address[address: TEXT]
        Role[role: VARCHAR20 DEFAULT customer]
        Active[is_active: BOOLEAN DEFAULT TRUE]
        Created[created_at: TIMESTAMP DEFAULT NOW]
        Updated[updated_at: TIMESTAMP ON UPDATE NOW]
    end
    
    User --> ID
    User --> Name
    User --> Email
    User --> Phone
    User --> Password
    User --> Gender
    User --> Address
    User --> Role
    User --> Active
    User --> Created
    User --> Updated
    
    style User fill:#e1f5ff
    style ID fill:#e1ffe1
    style Email fill:#ffe1e1
    style Password fill:#ffe1e1
```

**Indexes:**
- PRIMARY KEY on user_id
- UNIQUE KEY on email

**Relationships:**
- One-to-Many with CART_ITEMS
- One-to-Many with WISHLIST_ITEMS
- One-to-Many with ORDERS
- One-to-Many with REVIEWS

---

### PRODUCTS Table

```mermaid
graph TB
    subgraph "PRODUCTS Table"
        Product[PRODUCTS]
        PID[product_id: INT PK AUTO_INCREMENT]
        PName[product_name: VARCHAR255 NOT NULL]
        Desc[description: TEXT]
        Price[price: DECIMAL102 NOT NULL]
        Discount[discount_percent: DECIMAL52 DEFAULT 0]
        Image[image_url: VARCHAR255]
        Active[active: BOOLEAN DEFAULT TRUE]
        IsNew[is_new: BOOLEAN DEFAULT FALSE]
        IsSale[is_sale: BOOLEAN DEFAULT FALSE]
        IsTrending[is_trending: BOOLEAN DEFAULT FALSE]
        Brand[brand: VARCHAR100]
        Stock[stock_quantity: INT DEFAULT 0]
        CatID[category_id: INT FK]
        Score[popular_score: DECIMAL52 DEFAULT 0]
        Created[created_at: TIMESTAMP DEFAULT NOW]
        Updated[updated_at: TIMESTAMP ON UPDATE NOW]
    end
    
    Product --> PID
    Product --> PName
    Product --> Desc
    Product --> Price
    Product --> Discount
    Product --> Image
    Product --> Active
    Product --> IsNew
    Product --> IsSale
    Product --> IsTrending
    Product --> Brand
    Product --> Stock
    Product --> CatID
    Product --> Score
    Product --> Created
    Product --> Updated
    
    style Product fill:#e1f5ff
    style PID fill:#e1ffe1
    style CatID fill:#ffe1e1
    style Price fill:#ffe1e1
```

**Indexes:**
- PRIMARY KEY on product_id
- FOREIGN KEY on category_id
- INDEX on active, price
- INDEX on brand
- INDEX on is_new, is_sale, is_trending

**Relationships:**
- Many-to-One with CATEGORIES
- One-to-Many with PRODUCT_SIZES
- One-to-Many with CART_ITEMS

---

### ORDERS Table

```mermaid
graph TB
    subgraph "ORDERS Table"
        Order[ORDERS]
        OID[order_id: INT PK AUTO_INCREMENT]
        UID[user_id: INT FK]
        BillAddr[billing_address_id: INT FK]
        ShipAddr[shipping_address_id: INT FK]
        PayMethod[payment_method_id: INT FK]
        Coupon[coupon_id: INT FK]
        FName[full_name: VARCHAR100]
        Addr[address: TEXT]
        City[city: VARCHAR50]
        State[state: VARCHAR50]
        Zip[zip: VARCHAR10]
        Phone[phone: VARCHAR15]
        PayType[payment_method: VARCHAR20]
        Status[status: VARCHAR20 DEFAULT PLACED]
        ShipStatus[shipping_status: VARCHAR50 DEFAULT PROCESSING]
        Track[tracking_number: VARCHAR100]
        EstDel[estimated_delivery: DATE]
        ActDel[actual_delivery: DATE]
        Notes[notes: TEXT]
        Subtotal[subtotal: DECIMAL102 NOT NULL DEFAULT 0]
        Tax[tax_amount: DECIMAL102 DEFAULT 0]
        ShipFee[shipping_fee: DECIMAL102 DEFAULT 0]
        Disc[discount_amount: DECIMAL102 DEFAULT 0]
        Total[total_amount: DECIMAL102 NOT NULL]
        PayStatus[payment_status: ENUM DEFAULT pending]
        Gateway[payment_gateway: VARCHAR50]
        TransID[transaction_id: VARCHAR255]
        Created[created_at: TIMESTAMP DEFAULT NOW]
        Updated[updated_at: TIMESTAMP ON UPDATE NOW]
    end
    
    Order --> OID
    Order --> UID
    Order --> BillAddr
    Order --> ShipAddr
    Order --> PayMethod
    Order --> Coupon
    Order --> FName
    Order --> Addr
    Order --> City
    Order --> State
    Order --> Zip
    Order --> Phone
    Order --> PayType
    Order --> Status
    Order --> ShipStatus
    Order --> Track
    Order --> EstDel
    Order --> ActDel
    Order --> Notes
    Order --> Subtotal
    Order --> Tax
    Order --> ShipFee
    Order --> Disc
    Order --> Total
    Order --> PayStatus
    Order --> Gateway
    Order --> TransID
    Order --> Created
    Order --> Updated
    
    style Order fill:#e1f5ff
    style OID fill:#e1ffe1
    style UID fill:#ffe1e1
    style Total fill:#ffe1e1
    style Status fill:#ffe1e1
```

**Indexes:**
- PRIMARY KEY on order_id
- FOREIGN KEY on user_id, billing_address_id, shipping_address_id
- INDEX on status, created_at DESC
- INDEX on user_id, created_at DESC

**Relationships:**
- Many-to-One with USERS
- One-to-Many with ORDER_ITEMS
- One-to-Many with PAYMENTS

---

## Supporting Tables

### CATEGORIES Table

```mermaid
graph TB
    subgraph "CATEGORIES Table"
        Category[CATEGORIES]
        CID[category_id: INT PK AUTO_INCREMENT]
        CName[category_name: VARCHAR100 UNIQUE NOT NULL]
        Desc[description: TEXT]
        Parent[parent_category_id: INT FK]
        Active[is_active: BOOLEAN DEFAULT TRUE]
        Created[created_at: TIMESTAMP DEFAULT NOW]
    end
    
    Category --> CID
    Category --> CName
    Category --> Desc
    Category --> Parent
    Category --> Active
    Category --> Created
    
    style Category fill:#e1f5ff
    style CID fill:#e1ffe1
    style CName fill:#ffe1e1
    style Parent fill:#ffe1e1
```

**Relationships:**
- Self-reference for parent-child hierarchy
- One-to-Many with PRODUCTS

---

### CART_ITEMS Table

```mermaid
graph TB
    subgraph "CART_ITEMS Table"
        Cart[CART_ITEMS]
        CID[cart_item_id: INT PK AUTO_INCREMENT]
        UID[user_id: INT FK]
        PID[product_id: INT FK]
        Size[size_label: VARCHAR10]
        Qty[quantity: INT DEFAULT 1]
        Added[added_at: TIMESTAMP DEFAULT NOW]
    end
    
    Cart --> CID
    Cart --> UID
    Cart --> PID
    Cart --> Size
    Cart --> Qty
    Cart --> Added
    
    style Cart fill:#e1f5ff
    style CID fill:#e1ffe1
    style UID fill:#ffe1e1
    style PID fill:#ffe1e1
```

**Relationships:**
- Many-to-One with USERS
- Many-to-One with PRODUCTS

---

### WISHLIST_ITEMS Table

```mermaid
graph TB
    subgraph "WISHLIST_ITEMS Table"
        Wishlist[WISHLIST_ITEMS]
        WID[wishlist_item_id: INT PK AUTO_INCREMENT]
        UID[user_id: INT FK]
        PID[product_id: INT FK]
        Created[created_at: TIMESTAMP DEFAULT NOW]
    end
    
    Wishlist --> WID
    Wishlist --> UID
    Wishlist --> PID
    Wishlist --> Created
    
    style Wishlist fill:#e1f5ff
    style WID fill:#e1ffe1
    style UID fill:#ffe1e1
    style PID fill:#ffe1e1
```

**Relationships:**
- Many-to-One with USERS
- Many-to-One with PRODUCTS

---

### PAYMENTS Table

```mermaid
graph TB
    subgraph "PAYMENTS Table"
        Payment[PAYMENTS]
        PID[payment_id: INT PK AUTO_INCREMENT]
        OID[order_id: INT FK]
        PayMethod[payment_method: VARCHAR50 NOT NULL]
        TransID[transaction_id: VARCHAR255 NOT NULL]
        Amount[amount: DECIMAL102 NOT NULL]
        Currency[currency: VARCHAR3 DEFAULT INR]
        Status[status: VARCHAR50 DEFAULT PENDING]
        GatewayResp[gateway_response: TEXT]
        Signature[payment_signature: VARCHAR255]
        Verified[verified: BOOLEAN DEFAULT FALSE]
        Created[created_at: TIMESTAMP DEFAULT NOW]
        Updated[updated_at: TIMESTAMP ON UPDATE NOW]
    end
    
    Payment --> PID
    Payment --> OID
    Payment --> PayMethod
    Payment --> TransID
    Payment --> Amount
    Payment --> Currency
    Payment --> Status
    Payment --> GatewayResp
    Payment --> Signature
    Payment --> Verified
    Payment --> Created
    Payment --> Updated
    
    style Payment fill:#e1f5ff
    style PID fill:#e1ffe1
    style OID fill:#ffe1e1
    style Amount fill:#ffe1e1
```

**Relationships:**
- Many-to-One with ORDERS

---

### REVIEWS Table

```mermaid
graph TB
    subgraph "REVIEWS Table"
        Review[REVIEWS]
        RID[review_id: INT PK AUTO_INCREMENT]
        UID[user_id: INT FK]
        PID[product_id: INT FK]
        Rating[rating: INT CHECK 1-5]
        Comment[comment: TEXT]
        Created[created_at: TIMESTAMP DEFAULT NOW]
    end
    
    Review --> RID
    Review --> UID
    Review --> PID
    Review --> Rating
    Review --> Comment
    Review --> Created
    
    style Review fill:#e1f5ff
    style RID fill:#e1ffe1
    style UID fill:#ffe1e1
    style PID fill:#ffe1e1
```

**Relationships:**
- Many-to-One with USERS
- Many-to-One with PRODUCTS

---

## Database Indexes

```mermaid
graph TB
    subgraph "Primary Indexes"
        PK[Primary Keys]
        UserID[USERS.user_id]
        ProductID[PRODUCTS.product_id]
        OrderID[ORDERS.order_id]
        CatID[CATEGORIES.category_id]
    end
    
    subgraph "Unique Indexes"
        Unique[Unique Constraints]
        UserEmail[USERS.email]
        CatName[CATEGORIES.category_name]
        CouponCode[COUPONS.code]
        InvoiceNo[INVOICES.invoice_number]
    end
    
    subgraph "Foreign Key Indexes"
        FK[Foreign Keys]
        ProdCat[PRODUCTS.category_id]
        CartUser[CART_ITEMS.user_id]
        OrderUser[ORDERS.user_id]
        OrderProd[ORDER_ITEMS.product_id]
    end
    
    subgraph "Performance Indexes"
        Perf[Performance Indexes]
        ProdActive[PRODUCTS.active, price]
        ProdBrand[PRODUCTS.brand]
        OrderStatus[ORDERS.status, created_at]
        OrderUser[ORDERS.user_id, created_at]
    end
    
    PK --> UserID
    PK --> ProductID
    PK --> OrderID
    PK --> CatID
    
    Unique --> UserEmail
    Unique --> CatName
    Unique --> CouponCode
    Unique --> InvoiceNo
    
    FK --> ProdCat
    FK --> CartUser
    FK --> OrderUser
    FK --> OrderProd
    
    Perf --> ProdActive
    Perf --> ProdBrand
    Perf --> OrderStatus
    Perf --> OrderUser
    
    style PK fill:#e1ffe1
    style Unique fill:#ffe1e1
    style FK fill:#fff4e1
    style Perf fill:#e1f5ff
```

## Database Views

### v_trending_products
Returns top 50 trending products ordered by popular_score.

```mermaid
graph TB
    subgraph "View: v_trending_products"
        View[v_trending_products]
        Query[SELECT p.* FROM products p WHERE p.active = TRUE ORDER BY p.popular_score DESC, p.created_at DESC LIMIT 50]
        Result[Trending Products]
    end
    
    View --> Query
    Query --> Result
    
    style View fill:#e1f5ff
    style Query fill:#e1ffe1
    style Result fill:#fff4e1
```

### v_low_stock_products
Returns products with low stock (≤10 items).

```mermaid
graph TB
    subgraph "View: v_low_stock_products"
        View[v_low_stock_products]
        Query[SELECT p.*, CASE WHEN p.stock_quantity = 0 THEN OUT_OF_STOCK WHEN p.stock_quantity <= 5 THEN CRITICAL WHEN p.stock_quantity <= 10 THEN LOW ELSE NORMAL END as stock_status FROM products p WHERE p.active = TRUE AND p.stock_quantity <= 10 ORDER BY p.stock_quantity ASC]
        Result[Low Stock Products]
    end
    
    View --> Query
    Query --> Result
    
    style View fill:#e1f5ff
    style Query fill:#e1ffe1
    style Result fill:#fff4e1
```

## Database Triggers

### trg_product_update_timestamp
Automatically updates the `updated_at` timestamp when a product is updated.

```mermaid
graph TB
    subgraph "Trigger: trg_product_update_timestamp"
        Trigger[BEFORE UPDATE ON products]
        Action[SET NEW.updated_at = NOW]
        Result[Timestamp Updated]
    end
    
    Trigger --> Action
    Action --> Result
    
    style Trigger fill:#e1f5ff
    style Action fill:#e1ffe1
    style Result fill:#fff4e1
```

### trg_prevent_duplicate_pending_orders
Prevents duplicate pending orders within 5 minutes for the same user.

```mermaid
graph TB
    subgraph "Trigger: trg_prevent_duplicate_pending_orders"
        Trigger[BEFORE INSERT ON orders]
        Check[Check for existing pending orders within 5 minutes]
        Condition[IF pending_count > 0]
        Error[SIGNAL SQLSTATE 45000]
        Result[Error: Another order is already being processed]
    end
    
    Trigger --> Check
    Check --> Condition
    Condition --> Error
    Error --> Result
    
    style Trigger fill:#e1f5ff
    style Check fill:#e1ffe1
    style Condition fill:#fff4e1
    style Error fill:#ffe1e1
```

## Table Relationships Summary

```mermaid
graph TB
    subgraph "User Centered"
        UC[User Tables]
        Users[USERS]
        Cart[CART_ITEMS]
        Wishlist[WISHLIST_ITEMS]
        Saved[SAVED_ITEMS]
        Orders[ORDERS]
        Reviews[REVIEWS]
        PayMethods[PAYMENT_METHODS]
        Addresses[ADDRESSES]
    end
    
    subgraph "Product Centered"
        PC[Product Tables]
        Products[PRODUCTS]
        Categories[CATEGORIES]
        Sizes[PRODUCT_SIZES]
        Attrs[PRODUCT_ATTRIBUTES]
    end
    
    subgraph "Order Centered"
        OC[Order Tables]
        Orders2[ORDERS]
        OrderItems[ORDER_ITEMS]
        Payments[PAYMENTS]
        Invoices[INVOICES]
        Refunds[REFUNDS]
    end
    
    UC --> Users
    Users --> Cart
    Users --> Wishlist
    Users --> Saved
    Users --> Orders
    Users --> Reviews
    Users --> PayMethods
    Users --> Addresses
    
    PC --> Products
    Products --> Categories
    Products --> Sizes
    Products --> Attrs
    
    OC --> Orders2
    Orders2 --> OrderItems
    Orders2 --> Payments
    Orders2 --> Invoices
    Orders2 --> Refunds
    
    style UC fill:#e1f5ff
    style PC fill:#e1ffe1
    style OC fill:#fff4e1
```
