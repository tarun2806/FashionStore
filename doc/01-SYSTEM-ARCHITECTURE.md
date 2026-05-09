# System Architecture

## Overview

The FashionStore system follows a Model-View-Controller (MVC) architecture pattern, providing a clear separation of concerns between data access, business logic, and presentation layers.

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Browser[Web Browser]
        Mobile[Mobile Browser]
    end
    
    subgraph "Web Server Layer"
        Tomcat[Apache Tomcat Server]
        WebApp[FashionStore Web Application]
    end
    
    subgraph "Application Layer"
        Controllers[Servlet Controllers]
        Filters[Security Filters]
        Services[Service Layer]
        DAO[DAO Layer]
    end
    
    subgraph "Data Layer"
        MySQL[(MySQL Database)]
        Cache[Cache Service]
    end
    
    subgraph "External Services"
        Email[Email Service]
        Payment[Payment Gateway]
    end
    
    Browser --> Tomcat
    Mobile --> Tomcat
    Tomcat --> WebApp
    WebApp --> Filters
    Filters --> Controllers
    Controllers --> Services
    Services --> DAO
    DAO --> MySQL
    Services --> Cache
    Services --> Email
    Services --> Payment
    
    style Browser fill:#e1f5ff
    style Mobile fill:#e1f5ff
    style Tomcat fill:#fff4e1
    style MySQL fill:#ffe1e1
    style Cache fill:#e1ffe1
    style Email fill:#f0e1ff
    style Payment fill:#f0e1ff
```

## MVC Architecture Pattern

```mermaid
graph LR
    subgraph "View Layer"
        JSP[JSP Views]
        CSS[CSS Stylesheets]
        JS[JavaScript]
    end
    
    subgraph "Controller Layer"
        Servlet[Servlet Controllers]
        Filter[Security Filters]
    end
    
    subgraph "Model Layer"
        Model[Model Classes]
        DAO[DAO Interfaces]
        Impl[DAO Implementations]
    end
    
    subgraph "Data Layer"
        DB[(Database)]
    end
    
    JSP --> Servlet
    CSS --> JSP
    JS --> JSP
    Servlet --> Filter
    Filter --> Servlet
    Servlet --> Model
    Model --> DAO
    DAO --> Impl
    Impl --> DB
    DB --> Impl
    Impl --> DAO
    DAO --> Model
    Model --> Servlet
    Servlet --> JSP
    
    style JSP fill:#e1f5ff
    style Servlet fill:#fff4e1
    style Model fill:#e1ffe1
    style DB fill:#ffe1e1
```

## Request Flow Architecture

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant Tomcat
    participant FilterChain
    participant Controller
    participant Service
    participant DAO
    participant Database
    participant Cache
    participant View
    
    User->>Browser: HTTP Request
    Browser->>Tomcat: Send Request
    Tomcat->>FilterChain: Security Headers
    FilterChain->>FilterChain: CSRF Filter
    FilterChain->>FilterChain: Auth Filter
    FilterChain->>FilterChain: Security Filter
    FilterChain->>Controller: Process Request
    Controller->>Service: Business Logic
    Service->>Cache: Check Cache
    alt Cache Hit
        Cache-->>Service: Cached Data
    else Cache Miss
        Service->>DAO: Database Query
        DAO->>Database: SQL Query
        Database-->>DAO: Result Set
        DAO-->>Service: Data
        Service->>Cache: Update Cache
    end
    Service-->>Controller: Result
    Controller->>View: Render Response
    View-->>Controller: HTML
    Controller-->>Tomcat: HTTP Response
    Tomcat-->>Browser: Response
    Browser-->>User: Display Page
```

## Package Structure

```mermaid
graph TB
    Root[com.fashionstore]
    
    subgraph "Controller Package"
        Ctrl[controller]
        Home[HomeServlet]
        Login[LoginController]
        Register[RegisterController]
        Product[ProductController]
        Cart[CartController]
        Checkout[CheckoutController]
        Order[OrderController]
        Admin[Admin Controllers]
    end
    
    subgraph "Model Package"
        Model[model]
        User[User]
        Product[Product]
        Order[Order]
        Cart[CartItem]
        Wishlist[WishlistItem]
    end
    
    subgraph "DAO Package"
        DAO[dao]
        UserDAO[UserDAO]
        ProductDAO[ProductDAO]
        OrderDAO[OrderDAO]
    end
    
    subgraph "DAO Implementation Package"
        DAOImpl[daoimpl]
        UserDAOImpl[UserDAOImpl]
        ProductDAOImpl[ProductDAOImpl]
        OrderDAOImpl[OrderDAOImpl]
    end
    
    subgraph "Service Package"
        Svc[service]
        UserService[UserService]
        ProductService[ProductService]
    end
    
    subgraph "Filter Package"
        Flt[filter]
        Auth[AuthFilter]
        Security[SecurityFilter]
        CSRF[CSRFFilter]
        Headers[SecurityHeadersFilter]
    end
    
    subgraph "Security Package"
        Sec[security]
        CSRFProt[CSRFProtection]
        Rate[RateLimiter]
    end
    
    subgraph "Cache Package"
        Cache[cache]
        CacheSvc[CacheService]
        CacheKey[CacheKey]
        CacheTTL[CacheTTL]
    end
    
    subgraph "Utility Package"
        Util[util]
        DB[DBConnection]
        Validator[Validator]
    end
    
    Root --> Ctrl
    Ctrl --> Home
    Ctrl --> Login
    Ctrl --> Register
    Ctrl --> Product
    Ctrl --> Cart
    Ctrl --> Checkout
    Ctrl --> Order
    Ctrl --> Admin
    
    Root --> Model
    Model --> User
    Model --> Product
    Model --> Order
    Model --> Cart
    Model --> Wishlist
    
    Root --> DAO
    DAO --> UserDAO
    DAO --> ProductDAO
    DAO --> OrderDAO
    
    Root --> DAOImpl
    DAOImpl --> UserDAOImpl
    DAOImpl --> ProductDAOImpl
    DAOImpl --> OrderDAOImpl
    
    Root --> Svc
    Svc --> UserService
    Svc --> ProductService
    
    Root --> Flt
    Flt --> Auth
    Flt --> Security
    Flt --> CSRF
    Flt --> Headers
    
    Root --> Sec
    Sec --> CSRFProt
    Sec --> Rate
    
    Root --> Cache
    Cache --> CacheSvc
    Cache --> CacheKey
    Cache --> CacheTTL
    
    Root --> Util
    Util --> DB
    Util --> Validator
    
    style Ctrl fill:#e1f5ff
    style Model fill:#e1ffe1
    style DAO fill:#fff4e1
    style DAOImpl fill:#ffe1e1
    style Svc fill:#f0e1ff
    style Flt fill:#ffe1ff
    style Sec fill:#e1ffe1
    style Cache fill:#fff4e1
    style Util fill:#e1f5ff
```

## Filter Chain Architecture

```mermaid
graph TB
    subgraph "Filter Chain"
        Request[HTTP Request]
        HeadersFilter[SecurityHeadersFilter]
        SecurityFilter[SecurityFilter]
        CSRFFilter[CSRFFilter]
        AuthFilter[AuthFilter]
        Controller[Controller]
        Response[HTTP Response]
    end
    
    subgraph "SecurityHeadersFilter"
        H1[X-Frame-Options: DENY]
        H2[X-Content-Type-Options: nosniff]
        H3[X-XSS-Protection: 1; mode=block]
        H4[Strict-Transport-Security]
        H5[Content-Security-Policy]
        H6[Referrer-Policy]
    end
    
    subgraph "SecurityFilter"
        S1[CSRF Token Generation]
        S2[CSRF Token Validation]
        S3[Rate Limiting]
    end
    
    subgraph "CSRFFilter"
        C1[Token Validation]
        C2[Token Expiration Check]
        C3[Replay Prevention]
    end
    
    subgraph "AuthFilter"
        A1[Session Validation]
        A2[Role Check]
        A3[AJAX Handling]
    end
    
    Request --> HeadersFilter
    HeadersFilter --> H1
    HeadersFilter --> H2
    HeadersFilter --> H3
    HeadersFilter --> H4
    HeadersFilter --> H5
    HeadersFilter --> H6
    HeadersFilter --> SecurityFilter
    
    SecurityFilter --> S1
    SecurityFilter --> S2
    SecurityFilter --> S3
    SecurityFilter --> CSRFFilter
    
    CSRFFilter --> C1
    CSRFFilter --> C2
    CSRFFilter --> C3
    CSRFFilter --> AuthFilter
    
    AuthFilter --> A1
    AuthFilter --> A2
    AuthFilter --> A3
    AuthFilter --> Controller
    
    Controller --> Response
    
    style Request fill:#e1f5ff
    style HeadersFilter fill:#e1ffe1
    style SecurityFilter fill:#fff4e1
    style CSRFFilter fill:#ffe1e1
    style AuthFilter fill:#e1f5ff
    style Controller fill:#e1ffe1
    style Response fill:#fff4e1
```

## Service Layer Architecture

```mermaid
graph TB
    subgraph "Service Layer"
        Svc[Service Layer]
        UserService[UserService]
        ProductService[ProductService]
        CategoryService[CategoryService]
        OrderService[OrderService]
        PaymentService[PaymentService]
        EmailService[EmailService]
        SearchService[SearchService]
        RecService[RecommendationService]
    end
    
    subgraph "Service Dependencies"
        Dep[Dependencies]
        Cache[CacheService]
        DB[DAO Layer]
        Email[Email API]
        Payment[Payment Gateway]
    end
    
    Svc --> UserService
    Svc --> ProductService
    Svc --> CategoryService
    Svc --> OrderService
    Svc --> PaymentService
    Svc --> EmailService
    Svc --> SearchService
    Svc --> RecService
    
    UserService --> Cache
    UserService --> DB
    
    ProductService --> Cache
    ProductService --> DB
    
    CategoryService --> DB
    
    OrderService --> DB
    OrderService --> Cache
    
    PaymentService --> DB
    PaymentService --> Payment
    
    EmailService --> Email
    
    SearchService --> DB
    SearchService --> Cache
    
    RecService --> DB
    RecService --> Cache
    
    style Svc fill:#e1f5ff
    style Cache fill:#e1ffe1
    style DB fill:#fff4e1
    style Email fill:#ffe1e1
    style Payment fill:#ffe1e1
```

## Data Access Layer Architecture

```mermaid
graph TB
    subgraph "DAO Layer"
        DAO[DAO Layer]
        UserDAO[UserDAO]
        ProductDAO[ProductDAO]
        OrderDAO[OrderDAO]
        CartDAO[CartDAO]
        WishlistDAO[WishlistDAO]
        CategoryDAO[CategoryDAO]
        PaymentDAO[PaymentDAO]
        ReviewDAO[ReviewDAO]
    end
    
    subgraph "DAO Implementations"
        Impl[DAO Implementations]
        UserImpl[UserDAOImpl]
        ProductImpl[ProductDAOImpl]
        OrderImpl[OrderDAOImpl]
        CartImpl[CartDAOImpl]
        WishlistImpl[WishlistDAOImpl]
        CategoryImpl[CategoryDAOImpl]
        PaymentImpl[PaymentDAOImpl]
        ReviewImpl[ReviewDAOImpl]
    end
    
    subgraph "Database Connection"
        DB[Database]
        Conn[DBConnection]
        Pool[Connection Pool]
    end
    
    DAO --> UserDAO
    DAO --> ProductDAO
    DAO --> OrderDAO
    DAO --> CartDAO
    DAO --> WishlistDAO
    DAO --> CategoryDAO
    DAO --> PaymentDAO
    DAO --> ReviewDAO
    
    Impl --> UserImpl
    Impl --> ProductImpl
    Impl --> OrderImpl
    Impl --> CartImpl
    Impl --> WishlistImpl
    Impl --> CategoryImpl
    Impl --> PaymentImpl
    Impl --> ReviewImpl
    
    UserImpl --> Conn
    ProductImpl --> Conn
    OrderImpl --> Conn
    CartImpl --> Conn
    WishlistImpl --> Conn
    CategoryImpl --> Conn
    PaymentImpl --> Conn
    ReviewImpl --> Conn
    
    Conn --> Pool
    Pool --> DB
    
    style DAO fill:#e1f5ff
    style Impl fill:#fff4e1
    style DB fill:#ffe1e1
    style Conn fill:#e1ffe1
```

## Caching Architecture

```mermaid
graph TB
    subgraph "Cache Layer"
        Cache[CacheService]
        CacheStore[ConcurrentHashMap Store]
        CacheKey[CacheKey]
        CacheTTL[CacheTTL]
        CacheEntry[CacheEntry]
    end
    
    subgraph "Cache Operations"
        Ops[Operations]
        Get[get]
        Put[put]
        Remove[remove]
        Invalidate[invalidate]
        Clear[clear]
    end
    
    subgraph "Cache Strategies"
        Strat[Strategies]
        TTL[Time To Live]
        Size[Size Based]
        Pattern[Pattern Based]
    end
    
    Cache --> CacheStore
    Cache --> CacheKey
    Cache --> CacheTTL
    Cache --> CacheEntry
    
    Ops --> Get
    Ops --> Put
    Ops --> Remove
    Ops --> Invalidate
    Ops --> Clear
    
    Strat --> TTL
    Strat --> Size
    Strat --> Pattern
    
    Get --> Cache
    Put --> Cache
    Remove --> Cache
    Invalidate --> Cache
    Clear --> Cache
    
    TTL --> CacheEntry
    Size --> CacheStore
    Pattern --> CacheKey
    
    style Cache fill:#e1f5ff
    style CacheStore fill:#e1ffe1
    style CacheKey fill:#fff4e1
    style CacheEntry fill:#ffe1e1
```

## Security Architecture

```mermaid
graph TB
    subgraph "Security Layers"
        Layer1[Layer 1: Network Security]
        Layer2[Layer 2: Application Security]
        Layer3[Layer 3: Data Security]
    end
    
    subgraph "Network Security"
        Net[Network Protection]
        HTTPS[HTTPS/TLS]
        HSTS[HSTS Header]
        Firewall[Firewall Rules]
    end
    
    subgraph "Application Security"
        App[Application Protection]
        CSRF[CSRF Protection]
        Auth[Authentication]
        Authz[Authorization]
        Rate[Rate Limiting]
        Input[Input Validation]
        Output[Output Encoding]
    end
    
    subgraph "Data Security"
        Data[Data Protection]
        Hash[Password Hashing]
        Encrypt[Data Encryption]
        SQL[SQL Injection Prevention]
        Session[Session Security]
    end
    
    Layer1 --> Net
    Layer2 --> App
    Layer3 --> Data
    
    Net --> HTTPS
    Net --> HSTS
    Net --> Firewall
    
    App --> CSRF
    App --> Auth
    App --> Authz
    App --> Rate
    App --> Input
    App --> Output
    
    Data --> Hash
    Data --> Encrypt
    Data --> SQL
    Data --> Session
    
    style Layer1 fill:#e1ffe1
    style Layer2 fill:#fff4e1
    style Layer3 fill:#e1f5ff
```

## Transaction Management Architecture

```mermaid
graph TB
    subgraph "Transaction Management"
        TM[Transaction Manager]
        Begin[beginTransaction]
        Commit[commit]
        Rollback[rollback]
    end
    
    subgraph "Transaction Boundaries"
        Bound[Transaction Boundaries]
        Checkout[Checkout Process]
        Payment[Payment Process]
        Order[Order Creation]
    end
    
    subgraph "Transaction Operations"
        Ops[Operations]
        StockRed[Stock Reduction]
        OrderIns[Order Insertion]
        OrderItemIns[Order Item Insertion]
        CartClear[Cart Clearing]
        PaymentIns[Payment Insertion]
    end
    
    TM --> Begin
    TM --> Commit
    TM --> Rollback
    
    Bound --> Checkout
    Bound --> Payment
    Bound --> Order
    
    Checkout --> Begin
    Checkout --> Ops
    Ops --> StockRed
    Ops --> OrderIns
    Ops --> OrderItemIns
    Ops --> CartClear
    Ops --> PaymentIns
    Ops --> Commit
    
    Payment --> Begin
    Payment --> Ops
    Ops --> Commit
    
    Order --> Begin
    Order --> Ops
    Ops --> Commit
    
    Ops --> Rollback
    
    style TM fill:#e1f5ff
    style Bound fill:#e1ffe1
    style Ops fill:#fff4e1
```

## Error Handling Architecture

```mermaid
graph TB
    subgraph "Error Handling"
        EH[Error Handling]
        Exception[ExceptionHandler]
        ApplicationException[ApplicationException]
        ErrorCode[ErrorCode]
    end
    
    subgraph "Error Types"
        Types[Error Types]
        Validation[Validation Errors]
        Business[Business Errors]
        System[System Errors]
        Security[Security Errors]
    end
    
    subgraph "Error Response"
        Response[Error Response]
        JSON[JSON Response]
        HTML[HTML Error Page]
        Log[Error Logging]
    end
    
    EH --> Exception
    EH --> ApplicationException
    EH --> ErrorCode
    
    Types --> Validation
    Types --> Business
    Types --> System
    Types --> Security
    
    Exception --> Types
    ApplicationException --> Response
    
    Response --> JSON
    Response --> HTML
    Response --> Log
    
    Validation --> JSON
    Business --> HTML
    System --> Log
    Security --> JSON
    
    style EH fill:#e1f5ff
    style Types fill:#e1ffe1
    style Response fill:#fff4e1
```

## Logging Architecture

```mermaid
graph TB
    subgraph "Logging Layer"
        Log[Logging Layer]
        SLF4J[SLF4J API]
        Logback[Logback Implementation]
        AuditLogger[AuditLogger]
    end
    
    subgraph "Log Levels"
        Levels[Log Levels]
        ERROR[ERROR]
        WARN[WARN]
        INFO[INFO]
        DEBUG[DEBUG]
        TRACE[TRACE]
    end
    
    subgraph "Log Outputs"
        Output[Log Outputs]
        Console[Console Appender]
        File[File Appender]
        Rolling[Rolling File Appender]
    end
    
    subgraph "Log Events"
        Events[Log Events]
        Login[Login Events]
        Order[Order Events]
        Payment[Payment Events]
        Error[Error Events]
    end
    
    Log --> SLF4J
    SLF4J --> Logback
    Logback --> AuditLogger
    
    Levels --> ERROR
    Levels --> WARN
    Levels --> INFO
    Levels --> DEBUG
    Levels --> TRACE
    
    Logback --> Output
    Output --> Console
    Output --> File
    Output --> Rolling
    
    AuditLogger --> Events
    Events --> Login
    Events --> Order
    Events --> Payment
    Events --> Error
    
    style Log fill:#e1f5ff
    style Levels fill:#e1ffe1
    style Output fill:#fff4e1
    style Events fill:#ffe1e1
```
