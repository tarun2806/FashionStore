package com.fashionstore.controller;

import com.fashionstore.dao.*;
import com.fashionstore.daoimpl.*;
import com.fashionstore.model.*;
import com.fashionstore.service.UserService;
import com.fashionstore.util.JsonUtil;
import com.fashionstore.util.SecurityUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * JSON API for the React + Vite admin dashboard.
 * Mounted at /api/admin/*  (servlet runs alongside the existing JSP admin pages).
 *
 * Session-based auth: same JSESSIONID is used. The Vite dev server proxies /api -> :8080
 * so the cookie is shared in development. In production both apps are served from the
 * same origin behind a reverse proxy.
 */
@WebServlet("/api/admin/*")
public class AdminApiController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    private UserService userService;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private ProductDAO productDAO;
    private ProductSizeDAO productSizeDAO;
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private CouponDAO couponDAO;

    @Override
    public void init() {
        userService = new UserService();
        orderDAO = new OrderDAOImpl();
        orderItemDAO = new OrderItemDAOImpl();
        productDAO = new ProductDAOImpl();
        productSizeDAO = new ProductSizeDAOImpl();
        userDAO = new UserDAOImpl();
        categoryDAO = new CategoryDAOImpl();
        couponDAO = new CouponDAOImpl();
    }

    // ============================================================
    // HTTP method dispatch
    // ============================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyCors(request, response);
        route(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyCors(request, response);
        // Allow login and register without origin check (they have their own secret key validation)
        String path = request.getPathInfo();
        if (path != null && (path.equals("/login") || path.equals("/register"))) {
            route(request, response, "POST");
            return;
        }
        if (!isTrustedStateChangingRequest(request)) {
            writeJson(response, 403, Map.of("success", false, "message", "Blocked by origin policy"));
            return;
        }
        route(request, response, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyCors(request, response);
        if (!isTrustedStateChangingRequest(request)) {
            writeJson(response, 403, Map.of("success", false, "message", "Blocked by origin policy"));
            return;
        }
        route(request, response, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        applyCors(request, response);
        if (!isTrustedStateChangingRequest(request)) {
            writeJson(response, 403, Map.of("success", false, "message", "Blocked by origin policy"));
            return;
        }
        route(request, response, "DELETE");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        applyCors(request, response);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // ============================================================
    // Router
    // ============================================================

    private void route(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
        String[] segs = segments(request);
        String resource = segs.length > 1 ? segs[1] : "";
        String id = segs.length > 2 ? segs[2] : null;
        String sub = segs.length > 3 ? segs[3] : null;

        try {
            switch (resource) {
                case "me" -> meEndpoint(request, response);
                case "login" -> {
                    if ("POST".equals(method)) loginEndpoint(request, response);
                    else notFound(response);
                }
                case "logout" -> {
                    if ("POST".equals(method)) logoutEndpoint(request, response);
                    else notFound(response);
                }
                case "register" -> {
                    if ("POST".equals(method)) registerEndpoint(request, response);
                    else notFound(response);
                }
                case "dashboard" -> {
                    if ("GET".equals(method)) dashboardEndpoint(request, response);
                    else notFound(response);
                }
                case "stats" -> {
                    if ("GET".equals(method)) statsEndpoint(request, response);
                    else notFound(response);
                }
                case "orders" -> routeOrders(request, response, method, id, sub);
                case "products" -> routeProducts(request, response, method, id);
                case "users" -> routeUsers(request, response, method, id, sub);
                case "inventory" -> routeInventory(request, response, method, id, sub);
                case "categories" -> routeCategories(request, response, method, id);
                case "coupons" -> routeCoupons(request, response, method, id);
                default -> notFound(response);
            }
        } catch (Exception e) {
            logger.error("Admin API error {} {}: {}", method, request.getPathInfo(), e.getMessage(), e);
            writeJson(response, 500, Map.of("success", false, "message", "Internal server error"));
        }
    }

    private String[] segments(HttpServletRequest request) {
        String p = request.getPathInfo();
        if (p == null || p.isEmpty() || "/".equals(p)) return new String[]{""};
        return p.split("/");
    }

    private void notFound(HttpServletResponse response) throws IOException {
        writeJson(response, 404, Map.of("success", false, "message", "Not found"));
    }

    // ============================================================
    // Auth
    // ============================================================

    private void loginEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> body = readJsonBody(request);
        String email = strParam(body, "email");
        String password = strParam(body, "password");

        if (email.isBlank() || password.isBlank()) {
            writeJson(response, 400, Map.of("success", false, "message", "Email and password required"));
            return;
        }

        User user = userService.loginUser(email, password);
        if (user == null) {
            writeJson(response, 401, Map.of("success", false, "message", "Invalid credentials"));
            return;
        }
        if (!user.isAdmin()) {
            writeJson(response, 403, Map.of("success", false, "message", "Admin access required"));
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        writeJson(response, 200, Map.of("success", true, "user", publicUser(user)));
    }

    private void logoutEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        writeJson(response, 200, Map.of("success", true));
    }

    private void meEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SecurityUtil.getCurrentUser(request);
        if (user == null || !user.isAdmin()) {
            writeJson(response, 401, Map.of("success", false, "message", "Not authenticated"));
            return;
        }
        writeJson(response, 200, Map.of("success", true, "user", publicUser(user)));
    }

    private void registerEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> body = readJsonBody(request);
        String fullName = strParam(body, "fullName");
        String email = strParam(body, "email");
        String phone = strParam(body, "phone");
        String password = strParam(body, "password");
        String confirmPassword = strParam(body, "confirmPassword");
        String adminKey = strParam(body, "adminKey");

        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            writeJson(response, 400, Map.of("success", false, "message", "All fields are required"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            writeJson(response, 400, Map.of("success", false, "message", "Passwords do not match"));
            return;
        }

        if (password.length() < 8) {
            writeJson(response, 400, Map.of("success", false, "message", "Password must be at least 8 characters"));
            return;
        }

        // Validate admin secret key
        String expectedKey = System.getenv("FASHIONSTORE_ADMIN_KEY");
        if (expectedKey == null || expectedKey.isBlank()) {
            expectedKey = "FS_ADMIN_SECRET_2026";
        }

        if (!expectedKey.equals(adminKey)) {
            writeJson(response, 403, Map.of("success", false, "message", "Invalid admin secret key"));
            return;
        }

        // Check if email already exists
        if (userService.isEmailExists(email)) {
            writeJson(response, 409, Map.of("success", false, "message", "Email already registered"));
            return;
        }

        try {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPassword(password);
            user.setGender("other");
            user.setAddress("");
            user.setRole("admin");

            int userId = userService.registerUser(user);
            if (userId > 0) {
                writeJson(response, 201, Map.of("success", true, "message", "Admin account created successfully"));
            } else {
                writeJson(response, 500, Map.of("success", false, "message", "Failed to create admin account"));
            }
        } catch (Exception e) {
            logger.error("Error creating admin account: {}", e.getMessage(), e);
            writeJson(response, 500, Map.of("success", false, "message", "An error occurred"));
        }
    }

    // ============================================================
    // Dashboard & Stats
    // ============================================================

    private void dashboardEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ensureAdmin(request, response)) return;
        double totalRevenue = orderDAO.getTotalRevenue();
        int totalUsers = userDAO.getTotalUserCount();
        int totalOrders = orderDAO.getTotalOrderCount();
        int lowStockCount = productDAO.getLowStockProductCount(10);
        List<Order> recentOrders = orderDAO.getRecentOrders(10);
        writeJson(response, 200, Map.of(
                "success", true,
                "stats", Map.of("totalRevenue", totalRevenue, "totalUsers", totalUsers, "totalOrders", totalOrders, "lowStockCount", lowStockCount),
                "recentOrders", recentOrders.stream().map(this::publicOrder).toList()
        ));
    }

    private void statsEndpoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ensureAdmin(request, response)) return;
        double totalRevenue = orderDAO.getTotalRevenue();
        int totalUsers = userDAO.getTotalUserCount();
        int totalOrders = orderDAO.getTotalOrderCount();
        int lowStockCount = productDAO.getLowStockProductCount(10);
        writeJson(response, 200, Map.of(
                "success", true,
                "revenue", totalRevenue,
                "orders", totalOrders,
                "products", productDAO.getAllProducts().size(),
                "customers", totalUsers,
                "pending", orderDAO.getRecentOrders(1000).stream().filter(o -> "Pending".equalsIgnoreCase(o.getStatus())).count(),
                "lowStock", lowStockCount
        ));
    }

    // ============================================================
    // Orders
    // ============================================================

    private void routeOrders(HttpServletRequest request, HttpServletResponse response, String method, String id, String sub) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            int limit = parseInt(request.getParameter("limit"), 50);
            List<Order> orders = orderDAO.getRecentOrders(limit);
            orderItemDAO.batchLoadOrderItems(orders);
            writeJson(response, 200, Map.of("success", true, "orders", orders.stream().map(this::publicOrder).toList(), "count", orders.size()));
            return;
        }
        if ("GET".equals(method) && "recent".equals(id)) {
            int limit = parseInt(request.getParameter("limit"), 10);
            List<Order> orders = orderDAO.getRecentOrders(limit);
            orderItemDAO.batchLoadOrderItems(orders);
            writeJson(response, 200, Map.of("success", true, "orders", orders.stream().map(this::publicOrder).toList()));
            return;
        }
        if ("GET".equals(method) && id != null && !id.isBlank()) {
            Order order = orderDAO.getOrderById(parseInt(id, 0));
            if (order == null) { notFound(response); return; }
            order.setItems(orderItemDAO.getItemsByOrderId(order.getOrderId()));
            writeJson(response, 200, Map.of("success", true, "order", publicOrder(order)));
            return;
        }
        if ("PUT".equals(method) && id != null) {
            int orderId = parseInt(id, 0);
            if (orderId <= 0) { notFound(response); return; }
            String target = sub != null ? sub : "status";
            switch (target) {
                case "status" -> {
                    Map<String, Object> body = readJsonBody(request);
                    String status = strParam(body, "status");
                    if (status.isBlank()) { writeJson(response, 400, Map.of("success", false, "message", "Status required")); return; }
                    boolean ok = orderDAO.updateOrderStatus(orderId, status);
                    writeJson(response, ok ? 200 : 400, Map.of("success", ok, "message", ok ? "Updated" : "Failed"));
                }
                case "approve" -> {
                    boolean ok = orderDAO.updateOrderStatus(orderId, "Processing");
                    writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                }
                case "cancel", "refund" -> {
                    boolean ok = orderDAO.updateOrderStatus(orderId, "Cancelled");
                    writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                }
                case "ship" -> {
                    boolean ok = orderDAO.updateOrderStatus(orderId, "Shipped");
                    writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                }
                case "deliver" -> {
                    boolean ok = orderDAO.updateOrderStatus(orderId, "Delivered");
                    writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                }
                default -> notFound(response);
            }
            return;
        }
        notFound(response);
    }

    // ============================================================
    // Products
    // ============================================================

    private void routeProducts(HttpServletRequest request, HttpServletResponse response, String method, String id) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            List<Product> products = productDAO.getAllProducts();
            writeJson(response, 200, Map.of("success", true, "products", products.stream().map(this::publicProduct).toList(), "count", products.size()));
            return;
        }
        if ("GET".equals(method) && id != null) {
            Product p = productDAO.getProductById(parseInt(id, 0));
            if (p == null) { notFound(response); return; }
            writeJson(response, 200, Map.of("success", true, "product", publicProduct(p)));
            return;
        }
        if ("POST".equals(method) && id == null) {
            Map<String, Object> body = readJsonBody(request);
            Product p = bodyToProduct(body, true);
            int newId = productDAO.addProduct(p);
            if (newId > 0) {
                saveProductSizes(newId, body);
            }
            writeJson(response, newId > 0 ? 201 : 400, Map.of("success", newId > 0, "productId", newId));
            return;
        }
        if ("PUT".equals(method) && id != null) {
            int pid = parseInt(id, 0);
            Product existing = productDAO.getProductById(pid);
            if (existing == null) { notFound(response); return; }
            Map<String, Object> body = readJsonBody(request);
            Product p = bodyToProduct(body, false);
            p.setProductId(pid);
            boolean ok = productDAO.updateProduct(p);
            if (ok) saveProductSizes(pid, body);
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        if ("DELETE".equals(method) && id != null) {
            boolean ok = productDAO.deleteProduct(parseInt(id, 0));
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        notFound(response);
    }

    private void saveProductSizes(int productId, Map<String, Object> body) {
        Object sizesObj = body.get("sizes");
        if (sizesObj instanceof List<?> sizesList) {
            for (Object s : sizesList) {
                if (s == null) continue;
                String label = String.valueOf(s).trim();
                if (label.isEmpty()) continue;
                ProductSize ps = new ProductSize();
                ps.setProductId(productId);
                ps.setSizeLabel(label);
                ps.setStockQuantity(0);
                ps.setAvailable(true);
                productSizeDAO.addOrUpdateSize(ps);
            }
        }
    }

    // ============================================================
    // Users
    // ============================================================

    private void routeUsers(HttpServletRequest request, HttpServletResponse response, String method, String id, String sub) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            List<User> users = userDAO.getAllUsers();
            writeJson(response, 200, Map.of("success", true, "users", users.stream().map(this::publicUser).toList(), "count", users.size()));
            return;
        }
        if ("GET".equals(method) && "recent".equals(id)) {
            int limit = parseInt(request.getParameter("limit"), 10);
            List<User> users = userDAO.getAllUsers();
            writeJson(response, 200, Map.of("success", true, "users", users.stream().limit(limit).map(this::publicUser).toList()));
            return;
        }
        if ("GET".equals(method) && id != null && !"recent".equals(id)) {
            User u = userDAO.getUserById(parseInt(id, 0));
            if (u == null) { notFound(response); return; }
            writeJson(response, 200, Map.of("success", true, "user", publicUser(u)));
            return;
        }
        if ("PUT".equals(method) && id != null) {
            int uid = parseInt(id, 0);
            Map<String, Object> body = readJsonBody(request);
            User existing = userDAO.getUserById(uid);
            if (existing == null) { notFound(response); return; }
            Object roleObj = body.get("role");
            if (roleObj != null) {
                boolean ok = userDAO.updateUserRole(uid, String.valueOf(roleObj));
                writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                return;
            }
            Object blockedObj = body.get("blocked");
            if (blockedObj != null) {
                boolean blocked = Boolean.parseBoolean(String.valueOf(blockedObj));
                boolean ok = userDAO.updateUserRole(uid, blocked ? "disabled" : "user");
                writeJson(response, ok ? 200 : 400, Map.of("success", ok));
                return;
            }
            writeJson(response, 400, Map.of("success", false, "message", "No valid update field"));
            return;
        }
        if ("DELETE".equals(method) && id != null) {
            // Soft-delete by disabling
            boolean ok = userDAO.updateUserRole(parseInt(id, 0), "disabled");
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        notFound(response);
    }

    // ============================================================
    // Inventory
    // ============================================================

    private void routeInventory(HttpServletRequest request, HttpServletResponse response, String method, String id, String sub) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            List<Product> products = productDAO.getAllProducts();
            writeJson(response, 200, Map.of("success", true, "products", products.stream().map(this::publicProduct).toList()));
            return;
        }
        if ("GET".equals(method) && "low-stock".equals(id)) {
            List<Product> products = productDAO.getAllProducts();
            List<Product> low = products.stream().filter(p -> p.getStockQuantity() <= 5).toList();
            writeJson(response, 200, Map.of("success", true, "products", low.stream().map(this::publicProduct).toList()));
            return;
        }
        if ("PUT".equals(method) && id != null && "stock".equals(sub)) {
            int pid = parseInt(id, 0);
            Map<String, Object> body = readJsonBody(request);
            int stock = parseInt(String.valueOf(body.get("stock")), 0);
            boolean ok = productDAO.updateStock(pid, stock);
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        notFound(response);
    }

    // ============================================================
    // Categories
    // ============================================================

    private void routeCategories(HttpServletRequest request, HttpServletResponse response, String method, String id) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            List<Category> list = categoryDAO.getAllCategories();
            writeJson(response, 200, Map.of("success", true, "categories", list));
            return;
        }
        if ("POST".equals(method) && id == null) {
            Map<String, Object> body = readJsonBody(request);
            Category c = new Category();
            c.setCategoryName(strParam(body, "name"));
            c.setDescription(strParam(body, "description"));
            c.setActive(true);
            int newId = categoryDAO.addCategory(c);
            writeJson(response, newId > 0 ? 201 : 400, Map.of("success", newId > 0, "categoryId", newId));
            return;
        }
        if ("PUT".equals(method) && id != null) {
            int cid = parseInt(id, 0);
            Category existing = categoryDAO.getCategoryById(cid);
            if (existing == null) { notFound(response); return; }
            Map<String, Object> body = readJsonBody(request);
            existing.setCategoryName(strParam(body, "name"));
            existing.setDescription(strParam(body, "description"));
            boolean ok = categoryDAO.updateCategory(existing);
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        if ("DELETE".equals(method) && id != null) {
            boolean ok = categoryDAO.deleteCategory(parseInt(id, 0));
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        notFound(response);
    }

    // ============================================================
    // Coupons
    // ============================================================

    private void routeCoupons(HttpServletRequest request, HttpServletResponse response, String method, String id) throws IOException {
        if (!ensureAdmin(request, response)) return;
        if ("GET".equals(method) && id == null) {
            List<Coupon> list = couponDAO.getAllCoupons();
            writeJson(response, 200, Map.of("success", true, "coupons", list));
            return;
        }
        if ("POST".equals(method) && id == null) {
            Map<String, Object> body = readJsonBody(request);
            Coupon c = bodyToCoupon(body);
            boolean ok = couponDAO.addCoupon(c);
            writeJson(response, ok ? 201 : 400, Map.of("success", ok));
            return;
        }
        if ("PUT".equals(method) && id != null) {
            int cid = parseInt(id, 0);
            Coupon existing = couponDAO.getCouponById(cid);
            if (existing == null) { notFound(response); return; }
            Map<String, Object> body = readJsonBody(request);
            Coupon c = bodyToCoupon(body);
            c.setCouponId(cid);
            boolean ok = couponDAO.updateCoupon(c);
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        if ("DELETE".equals(method) && id != null) {
            boolean ok = couponDAO.deleteCoupon(parseInt(id, 0));
            writeJson(response, ok ? 200 : 400, Map.of("success", ok));
            return;
        }
        notFound(response);
    }

    // ============================================================
    // Helpers / mappers
    // ============================================================

    private boolean ensureAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SecurityUtil.getCurrentUser(request);
        if (user == null) {
            writeJson(response, 401, Map.of("success", false, "message", "Authentication required"));
            return false;
        }
        if (!user.isAdmin()) {
            writeJson(response, 403, Map.of("success", false, "message", "Admin access required"));
            return false;
        }
        return true;
    }

    private Map<String, Object> publicUser(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getUserId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("phone", u.getPhone());
        m.put("blocked", "disabled".equalsIgnoreCase(u.getRole()));
        m.put("orderCount", 0); // computed lazily if needed
        return m;
    }

    private Map<String, Object> publicProduct(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getProductId());
        m.put("name", p.getProductName());
        m.put("description", p.getDescription());
        m.put("price", p.getPrice());
        m.put("discount", p.getDiscountPercent());
        m.put("stock", p.getStockQuantity());
        m.put("imageUrl", p.getImageUrl());
        m.put("category", p.getCategoryName());
        m.put("categoryId", p.getCategoryId());
        m.put("status", p.isActive() ? "active" : "inactive");
        m.put("brand", p.getBrand());
        m.put("isNew", p.isNew());
        m.put("isSale", p.isSale());
        m.put("isTrending", p.isTrending());
        List<String> sizeLabels = new ArrayList<>();
        if (p.getSizes() != null) {
            for (ProductSize ps : p.getSizes()) sizeLabels.add(ps.getSizeLabel());
        }
        m.put("sizes", sizeLabels);
        return m;
    }

    private Map<String, Object> publicOrder(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getOrderId());
        m.put("userId", o.getUserId());
        m.put("customerName", o.getFullName());
        m.put("total", o.getTotalAmount());
        m.put("status", o.getStatus());
        m.put("paymentStatus", "pending");
        m.put("paymentMethod", o.getPaymentMethod());
        m.put("createdAt", o.getOrderDate() != null ? o.getOrderDate().getTime() : null);
        m.put("address", o.getAddress());
        m.put("city", o.getCity());
        m.put("state", o.getState());
        m.put("zip", o.getZip());
        m.put("phone", o.getPhone());
        List<Map<String, Object>> items = new ArrayList<>();
        if (o.getItems() != null) {
            for (OrderItem item : o.getItems()) {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("productId", item.getProductId());
                im.put("quantity", item.getQuantity());
                im.put("price", item.getPrice());
                im.put("sizeLabel", item.getSizeLabel());
                items.add(im);
            }
        }
        m.put("items", items);
        return m;
    }

    private Product bodyToProduct(Map<String, Object> body, boolean isNew) {
        Product p = new Product();
        p.setProductName(strParam(body, "name"));
        p.setDescription(strParam(body, "description"));
        p.setPrice(parseDouble(body.get("price"), 0.0));
        p.setDiscountPercent(parseDouble(body.get("discount"), 0.0));
        p.setImageUrl(strParam(body, "imageUrl"));
        p.setStockQuantity(parseInt(String.valueOf(body.get("stock")), 0));
        p.setBrand(strParam(body, "brand"));

        String status = strParam(body, "status");
        p.setActive("active".equalsIgnoreCase(status));

        // Category: try int first, else lookup by name
        Object catObj = body.get("category");
        int categoryId = 0;
        if (catObj != null) {
            try {
                categoryId = Integer.parseInt(String.valueOf(catObj).trim());
            } catch (NumberFormatException e) {
                String catName = String.valueOf(catObj).trim();
                for (Category c : categoryDAO.getAllCategories()) {
                    if (c.getCategoryName() != null && c.getCategoryName().equalsIgnoreCase(catName)) {
                        categoryId = c.getCategoryId();
                        break;
                    }
                }
            }
        }
        p.setCategoryId(categoryId);

        if (!isNew) {
            p.setNew(parseBoolean(body.get("isNew"), false));
            p.setSale(parseBoolean(body.get("isSale"), false));
            p.setTrending(parseBoolean(body.get("isTrending"), false));
        } else {
            p.setNew(parseBoolean(body.get("isNew"), false));
            p.setSale(parseBoolean(body.get("isSale"), false));
            p.setTrending(parseBoolean(body.get("isTrending"), false));
        }
        return p;
    }

    private Coupon bodyToCoupon(Map<String, Object> body) {
        Coupon c = new Coupon();
        c.setCode(strParam(body, "code"));
        c.setDescription(strParam(body, "description"));
        String dt = strParam(body, "discountType");
        c.setDiscountType(dt.isEmpty() ? "percentage" : dt);
        c.setDiscountValue(parseDouble(body.get("discountValue"), 0.0));
        c.setMinimumOrderAmount(parseDouble(body.get("minOrder"), 0.0));
        c.setMaximumDiscountAmount(null);
        Object maxUses = body.get("maxUses");
        c.setUsageLimit(maxUses != null ? parseInt(String.valueOf(maxUses), null) : null);
        c.setUserUsageLimit(1);
        c.setUsageCount(0);
        c.setActive(true);

        try {
            String expires = strParam(body, "expiresAt");
            if (!expires.isEmpty()) {
                java.time.LocalDate ld = java.time.LocalDate.parse(expires);
                c.setValidUntil(Timestamp.valueOf(ld.atTime(23, 59, 59)));
            } else {
                c.setValidUntil(new Timestamp(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
            }
        } catch (Exception e) {
            c.setValidUntil(new Timestamp(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        }
        c.setValidFrom(new Timestamp(System.currentTimeMillis()));
        return c;
    }

    private void applyCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.startsWith("http://localhost:5173")
                || origin.startsWith("http://127.0.0.1:5173"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,X-CSRF-Token");
            response.setHeader("Access-Control-Max-Age", "3600");
        }
    }

    private void writeJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(data));
    }

    private Map<String, Object> readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = request.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        String body = sb.toString().trim();
        if (body.isEmpty()) return new HashMap<>();
        try {
            Map<String, Object> parsed = JsonUtil.gson().fromJson(body,
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
            return parsed != null ? parsed : new HashMap<>();
        } catch (Exception e) {
            Map<String, Object> form = new HashMap<>();
            for (String pair : body.split("&")) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    form.put(java.net.URLDecoder.decode(pair.substring(0, idx), java.nio.charset.StandardCharsets.UTF_8),
                             java.net.URLDecoder.decode(pair.substring(idx + 1), java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            return form;
        }
    }

    private String strParam(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : String.valueOf(v).trim();
    }

    private int parseInt(String s, int defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return defaultVal; }
    }

    private Integer parseInt(String s, Integer defaultVal) {
        if (s == null || s.isBlank()) return defaultVal;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return defaultVal; }
    }

    private double parseDouble(Object v, double defaultVal) {
        if (v == null) return defaultVal;
        try { return Double.parseDouble(String.valueOf(v).trim()); } catch (NumberFormatException e) { return defaultVal; }
    }

    private boolean parseBoolean(Object v, boolean defaultVal) {
        if (v == null) return defaultVal;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private boolean isTrustedStateChangingRequest(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String local = request.getScheme() + "://" + request.getServerName()
                + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());

        if (origin != null && !origin.isBlank()) {
            return origin.equals(local)
                    || origin.startsWith("http://localhost:5173")
                    || origin.startsWith("http://127.0.0.1:5173");
        }
        if (referer != null && !referer.isBlank()) {
            return referer.startsWith(local)
                    || referer.startsWith("http://localhost:5173")
                    || referer.startsWith("http://127.0.0.1:5173");
        }
        return false;
    }
}
