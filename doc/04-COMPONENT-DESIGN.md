# Component Design

## Overview

This document describes the component design of the FashionStore application, showing how different components interact and their responsibilities.

## Controller Components

```mermaid
graph TB
    subgraph "Public Controllers"
        Pub[Public Access]
        Home[HomeServlet]
        Login[LoginController]
        Register[RegisterController]
        Product[ProductController]
        Details[ProductDetailsController]
        Search[SearchController]
        Reset[PasswordResetController]
    end
    
    subgraph "Private Controllers"
        Priv[Auth Required]
        Cart[CartController]
        Wishlist[WishlistController]
        Checkout[CheckoutController]
        Order[OrderController]
        Payment[PaymentController]
        Review[ReviewController]
    end
    
    subgraph "Admin Controllers"
        Adm[Admin Only]
        Dashboard[AdminDashboardController]
        AdminProd[AdminProductController]
        AdminUser[AdminUsersController]
        AdminOrder[AdminOrderController]
    end
    
    Pub --> Home
    Pub --> Login
    Pub --> Register
    Pub --> Product
    Pub --> Details
    Pub --> Search
    Pub --> Reset
    
    Priv --> Cart
    Priv --> Wishlist
    Priv --> Checkout
    Priv --> Order
    Priv --> Payment
    Priv --> Review
    
    Adm --> Dashboard
    Adm --> AdminProd
    Adm --> AdminUser
    Adm --> AdminOrder
    
    style Pub fill:#e1ffe1
    style Priv fill:#fff4e1
    style Adm fill:#ffe1e1
```

## Service Layer Components

```mermaid
graph TB
    subgraph "Service Layer"
        Svc[Business Logic Services]
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

## DAO Layer Components

```mermaid
graph TB
    subgraph "DAO Interfaces"
        Intf[DAO Interfaces]
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
    
    Intf --> UserDAO
    Intf --> ProductDAO
    Intf --> OrderDAO
    Intf --> CartDAO
    Intf --> WishlistDAO
    Intf --> CategoryDAO
    Intf --> PaymentDAO
    Intf --> ReviewDAO
    
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
    
    style Intf fill:#e1f5ff
    style Impl fill:#fff4e1
    style DB fill:#ffe1e1
    style Conn fill:#e1ffe1
```

## Security Components

```mermaid
graph TB
    subgraph "Security Filters"
        Flt[Filter Chain]
        Headers[SecurityHeadersFilter]
        Security[SecurityFilter]
        CSRF[CSRFFilter]
        Auth[AuthFilter]
    end
    
    subgraph "Security Services"
        Sec[Security Services]
        CSRFProt[CSRFProtection]
        Rate[RateLimiter]
        Hash[BCrypt Password Hashing]
    end
    
    subgraph "Security Features"
        Feat[Features]
        CSRFToken[CSRF Token Generation]
        CSRFVal[CSRF Token Validation]
        RateLimit[Rate Limiting]
        Session[Session Management]
        HeadersSet[Security Headers]
    end
    
    Flt --> Headers
    Flt --> Security
    Flt --> CSRF
    Flt --> Auth
    
    Sec --> CSRFProt
    Sec --> Rate
    Sec --> Hash
    
    CSRFProt --> CSRFToken
    CSRFProt --> CSRFVal
    
    Rate --> RateLimit
    
    Auth --> Session
    
    Headers --> HeadersSet
    
    style Flt fill:#e1f5ff
    style Sec fill:#e1ffe1
    style Feat fill:#fff4e1
```

## Cache Components

```mermaid
graph TB
    subgraph "Cache Layer"
        Cache[CacheService]
        Store[ConcurrentHashMap Store]
        Key[CacheKey]
        TTL[CacheTTL]
        Entry[CacheEntry]
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
        Time[Time To Live]
        Size[Size Based]
        Pattern[Pattern Based]
    end
    
    Cache --> Store
    Cache --> Key
    Cache --> TTL
    Cache --> Entry
    
    Ops --> Get
    Ops --> Put
    Ops --> Remove
    Ops --> Invalidate
    Ops --> Clear
    
    Strat --> Time
    Strat --> Size
    Strat --> Pattern
    
    Get --> Cache
    Put --> Cache
    Remove --> Cache
    Invalidate --> Cache
    Clear --> Cache
    
    Time --> Entry
    Size --> Store
    Pattern --> Key
    
    style Cache fill:#e1f5ff
    style Store fill:#e1ffe1
    style Key fill:#fff4e1
    style Entry fill:#ffe1e1
```

## Validation Components

```mermaid
graph TB
    subgraph "Validation Layer"
        Val[Validation Layer]
        Validator[Validator]
        Result[ValidationResult]
        Rules[Validation Rules]
    end
    
    subgraph "Validation Types"
        Types[Validation Types]
        Email[Email Validation]
        Phone[Phone Validation]
        Password[Password Validation]
        Name[Name Validation]
    end
    
    subgraph "Validation Rules"
        Rule[Rules]
        EmailFormat[Email Format Pattern]
        PhoneFormat[Phone Format Pattern]
        PasswordComplex[Password Complexity]
        NameFormat[Name Format Pattern]
    end
    
    Val --> Validator
    Val --> Result
    Val --> Rules
    
    Types --> Email
    Types --> Phone
    Types --> Password
    Types --> Name
    
    Rule --> EmailFormat
    Rule --> PhoneFormat
    Rule --> PasswordComplex
    Rule --> NameFormat
    
    Validator --> Types
    Validator --> Rule
    Validator --> Result
    
    style Val fill:#e1f5ff
    style Types fill:#e1ffe1
    style Rule fill:#fff4e1
```

## Email Components

```mermaid
graph TB
    subgraph "Email Layer"
        Email[Email Layer]
        Service[EmailService]
        Queue[Email Queue]
        Worker[Email Worker]
    end
    
    subgraph "Email Types"
        Types[Email Types]
        Welcome[Welcome Email]
        PasswordReset[Password Reset Email]
        OrderConfirm[Order Confirmation Email]
        Shipping[Shipping Notification Email]
        Delivery[Delivery Notification Email]
    end
    
    subgraph "Email Components"
        Comp[Components]
        Template[Email Template]
        Content[Email Content]
        Recipient[Recipient Address]
        Subject[Email Subject]
    end
    
    Email --> Service
    Email --> Queue
    Email --> Worker
    
    Types --> Welcome
    Types --> PasswordReset
    Types --> OrderConfirm
    Types --> Shipping
    Types --> Delivery
    
    Comp --> Template
    Comp --> Content
    Comp --> Recipient
    Comp --> Subject
    
    Service --> Types
    Service --> Comp
    Service --> Queue
    
    Queue --> Worker
    Worker --> Types
    
    style Email fill:#e1f5ff
    style Types fill:#e1ffe1
    style Comp fill:#fff4e1
```

## Model Components

```mermaid
graph TB
    subgraph "Model Layer"
        Model[Model Classes]
        User[User]
        Product[Product]
        Order[Order]
        CartItem[CartItem]
        WishlistItem[WishlistItem]
        Review[Review]
        Category[Category]
        Address[Address]
        Payment[Payment]
    end
    
    subgraph "Model Attributes"
        Attr[Model Attributes]
        ID[Primary Key]
        Fields[Fields]
        Methods[Methods]
        Relationships[Relationships]
    end
    
    subgraph "Model Relationships"
        Rel[Relationships]
        OneToOne[One-to-One]
        OneToMany[One-to-Many]
        ManyToOne[Many-to-One]
        ManyToMany[Many-to-Many]
    end
    
    Model --> User
    Model --> Product
    Model --> Order
    Model --> CartItem
    Model --> WishlistItem
    Model --> Review
    Model --> Category
    Model --> Address
    Model --> Payment
    
    Attr --> ID
    Attr --> Fields
    Attr --> Methods
    Attr --> Relationships
    
    Rel --> OneToOne
    Rel --> OneToMany
    Rel --> ManyToOne
    Rel --> ManyToMany
    
    User --> Attr
    User --> Rel
    Product --> Attr
    Product --> Rel
    Order --> Attr
    Order --> Rel
    
    style Model fill:#e1f5ff
    style Attr fill:#e1ffe1
    style Rel fill:#fff4e1
```

## Filter Components

```mermaid
graph TB
    subgraph "Filter Chain"
        Chain[Filter Chain]
        Order[Execution Order]
        Headers[SecurityHeadersFilter]
        Security[SecurityFilter]
        CSRF[CSRFFilter]
        Auth[AuthFilter]
    end
    
    subgraph "Filter Responsibilities"
        Resp[Responsibilities]
        HResp[Security Headers]
        SResp[CSRF Protection & Rate Limiting]
        CResp[CSRF Validation]
        AResp[Authentication & Authorization]
    end
    
    subgraph "Filter Logic"
        Logic[Filter Logic]
        Init[Initialization]
        DoFilter[Request Processing]
        Destroy[Cleanup]
    end
    
    Chain --> Order
    Chain --> Headers
    Chain --> Security
    Chain --> CSRF
    Chain --> Auth
    
    Resp --> HResp
    Resp --> SResp
    Resp --> CResp
    Resp --> AResp
    
    Logic --> Init
    Logic --> DoFilter
    Logic --> Destroy
    
    Headers --> HResp
    Headers --> Logic
    
    Security --> SResp
    Security --> Logic
    
    CSRF --> CResp
    CSRF --> Logic
    
    Auth --> AResp
    Auth --> Logic
    
    style Chain fill:#e1f5ff
    style Resp fill:#e1ffe1
    style Logic fill:#fff4e1
```

## Exception Handling Components

```mermaid
graph TB
    subgraph "Exception Layer"
        Exc[Exception Layer]
        Handler[ExceptionHandler]
        AppExc[ApplicationException]
        ErrCode[ErrorCode]
    end
    
    subgraph "Exception Types"
        Types[Exception Types]
        ValidationExc[Validation Exception]
        BusinessExc[Business Exception]
        SystemExc[System Exception]
        SecurityExc[Security Exception]
    end
    
    subgraph "Exception Handling"
        Handle[Handling]
        Log[Log Error]
        Response[Generate Response]
        Notify[Notify User]
    end
    
    Exc --> Handler
    Exc --> AppExc
    Exc --> ErrCode
    
    Types --> ValidationExc
    Types --> BusinessExc
    Types --> SystemExc
    Types --> SecurityExc
    
    Handle --> Log
    Handle --> Response
    Handle --> Notify
    
    Handler --> Types
    Handler --> Handle
    
    ValidationExc --> Log
    BusinessExc --> Log
    SystemExc --> Log
    SecurityExc --> Log
    
    style Exc fill:#e1f5ff
    style Types fill:#e1ffe1
    style Handle fill:#fff4e1
```

## Utility Components

```mermaid
graph TB
    subgraph "Utility Layer"
        Util[Utility Classes]
        DBConn[DBConnection]
        XSSUtil[XSSUtil]
        AuditLog[AuditLogger]
        DateUtil[DateUtil]
    end
    
    subgraph "DB Connection"
        DB[Database Connection]
        Pool[Connection Pool]
        Config[Configuration]
    end
    
    subgraph "XSS Protection"
        XSS[XSS Protection]
        Escape[Escape HTML]
        Sanitize[Sanitize Input]
    end
    
    subgraph "Logging"
        Log[Logging]
        Info[Info Level]
        Error[Error Level]
        Debug[Debug Level]
    end
    
    Util --> DBConn
    Util --> XSSUtil
    Util --> AuditLog
    Util --> DateUtil
    
    DBConn --> DB
    DBConn --> Pool
    DBConn --> Config
    
    XSSUtil --> XSS
    XSS --> Escape
    XSS --> Sanitize
    
    AuditLog --> Log
    Log --> Info
    Log --> Error
    Log --> Debug
    
    style Util fill:#e1f5ff
    style DB fill:#e1ffe1
    style XSS fill:#fff4e1
    style Log fill:#ffe1e1
```

## Component Interaction Diagram

```mermaid
graph TB
    subgraph "Presentation Layer"
        Pres[Presentation Layer]
        JSP[JSP Views]
        Controllers[Controllers]
        Filters[Filters]
    end
    
    subgraph "Business Layer"
        Bus[Business Layer]
        Services[Services]
        Cache[Cache Service]
        Validation[Validation]
    end
    
    subgraph "Data Layer"
        Data[Data Layer]
        DAO[DAO Layer]
        DB[Database]
    end
    
    subgraph "External Layer"
        Ext[External Layer]
        Email[Email Service]
        Payment[Payment Gateway]
    end
    
    Pres --> JSP
    Pres --> Controllers
    Pres --> Filters
    
    Controllers --> Services
    Controllers --> Validation
    Filters --> Services
    
    Bus --> Services
    Bus --> Cache
    Bus --> Validation
    
    Services --> DAO
    Services --> Cache
    Services --> Email
    Services --> Payment
    
    Data --> DAO
    Data --> DB
    
    Ext --> Email
    Ext --> Payment
    
    style Pres fill:#e1f5ff
    style Bus fill:#e1ffe1
    style Data fill:#fff4e1
    style Ext fill:#ffe1e1
```
