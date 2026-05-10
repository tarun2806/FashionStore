package com.fashionstore.integration;

import com.fashionstore.dao.*;
import com.fashionstore.daoimpl.*;
import com.fashionstore.model.*;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFlowTest extends BaseIntegrationTest {

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private CartDAO cartDAO;
    private OrderDAO orderDAO;
    private AddressDAO addressDAO;
    private Connection connection;

    private int testUserId;
    private int testProductId;

    @BeforeEach
    void setup() throws SQLException {
        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();
        categoryDAO = new CategoryDAOImpl();
        cartDAO = new CartDAOImpl();
        orderDAO = new OrderDAOImpl();
        addressDAO = new AddressDAOImpl();
        connection = getTestConnection();
        cleanDatabase();

        setupTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void setupTestData() throws SQLException {
        User user = new User();
        user.setFullName("Test Customer");
        user.setEmail("customer@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test St");
        user.setRole("customer");
        testUserId = userDAO.registerUser(user);

        Category category = new Category();
        category.setCategoryName("Test Category");
        category.setDescription("Test Description");
        category.setActive(true);
        int categoryId = categoryDAO.addCategory(category);

        Product product = new Product();
        product.setProductName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setDiscountPercent(10.0);
        product.setImageUrl("test.jpg");
        product.setStockQuantity(100);
        product.setCategoryId(categoryId);
        product.setActive(true);
        product.setNew(false);
        product.setSale(false);
        product.setTrending(false);
        product.setBrand("TestBrand");
        testProductId = productDAO.addProduct(product);

        Address address = new Address();
        address.setUserId(testUserId);
        address.setAddressType("shipping");
        address.setFullName("Test Customer");
        address.setPhone("1234567890");
        address.setAddressLine1("456 Shipping St");
        address.setAddressLine2("Apt 123");
        address.setCity("Test City");
        address.setState("Test State");
        address.setPostalCode("12345");
        address.setCountry("USA");
        address.setDefault(true);
        addressDAO.addAddress(address);
    }

    @Test
    void testCompleteCheckoutFlow() {
        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(2);
        int cartItemId = cartDAO.addToCart(item);
        assertTrue(cartItemId > 0);

        List<CartItem> cartItems = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(1, cartItems.size());
        assertEquals(2, cartItems.get(0).getQuantity());

        double expectedTotal = cartItems.get(0).getPrice() * cartItems.get(0).getQuantity();

        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(expectedTotal);
        order.setFullName("Test Customer");
        order.setAddress("456 Shipping St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("1234567890");
        order.setPaymentMethod("Credit Card");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);
        assertTrue(orderId > 0);

        Order createdOrder = orderDAO.getOrderById(orderId);
        assertNotNull(createdOrder);
        assertEquals(testUserId, createdOrder.getUserId());
        assertEquals(expectedTotal, createdOrder.getTotalAmount(), 0.01);
        assertEquals("Pending", createdOrder.getStatus());

        boolean cartCleared = cartDAO.clearCartByUserId(testUserId);
        assertTrue(cartCleared);

        List<CartItem> cartAfterCheckout = cartDAO.getCartItemsByUserId(testUserId);
        assertTrue(cartAfterCheckout.isEmpty());
    }

    @Test
    void testCheckoutWithMultipleItems() throws SQLException {
        Category category2 = new Category();
        category2.setCategoryName("Cat2");
        category2.setDescription("Desc2");
        category2.setActive(true);
        int cat2Id = categoryDAO.addCategory(category2);

        Product product2 = new Product();
        product2.setProductName("Product2");
        product2.setDescription("Desc2");
        product2.setPrice(149.99);
        product2.setDiscountPercent(0.0);
        product2.setImageUrl("prod2.jpg");
        product2.setStockQuantity(50);
        product2.setCategoryId(cat2Id);
        product2.setActive(true);
        product2.setNew(false);
        product2.setSale(false);
        product2.setTrending(false);
        product2.setBrand("Brand2");
        int prod2Id = productDAO.addProduct(product2);

        CartItem item1 = new CartItem();
        item1.setUserId(testUserId);
        item1.setProductId(testProductId);
        item1.setSizeLabel("M");
        item1.setQuantity(2);
        cartDAO.addToCart(item1);

        CartItem item2 = new CartItem();
        item2.setUserId(testUserId);
        item2.setProductId(prod2Id);
        item2.setSizeLabel("L");
        item2.setQuantity(1);
        cartDAO.addToCart(item2);

        List<CartItem> cartItems = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(2, cartItems.size());

        double expectedTotal = (99.99 * 2) + 149.99;

        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(expectedTotal);
        order.setFullName("Test Customer");
        order.setAddress("456 Shipping St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("1234567890");
        order.setPaymentMethod("PayPal");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);
        assertTrue(orderId > 0);

        Order createdOrder = orderDAO.getOrderById(orderId);
        assertEquals(expectedTotal, createdOrder.getTotalAmount(), 0.01);
    }

    @Test
    void testOrderStatusTransition() {
        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(1);
        cartDAO.addToCart(item);

        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(99.99);
        order.setFullName("Test Customer");
        order.setAddress("456 Shipping St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("1234567890");
        order.setPaymentMethod("Credit Card");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);

        assertTrue(orderDAO.updateOrderStatus(orderId, "Processing"));
        Order orderAfterUpdate = orderDAO.getOrderById(orderId);
        assertEquals("Processing", orderAfterUpdate.getStatus());

        assertTrue(orderDAO.updateOrderStatus(orderId, "Shipped"));
        orderAfterUpdate = orderDAO.getOrderById(orderId);
        assertEquals("Shipped", orderAfterUpdate.getStatus());

        assertTrue(orderDAO.updateOrderStatus(orderId, "Delivered"));
        orderAfterUpdate = orderDAO.getOrderById(orderId);
        assertEquals("Delivered", orderAfterUpdate.getStatus());
    }

    @Test
    void testCheckoutWithDiscount() throws SQLException {
        executeSql(connection, "UPDATE products SET discount_percent = 20.0 WHERE product_id = " + testProductId);

        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(2);
        cartDAO.addToCart(item);

        List<CartItem> cartItems = cartDAO.getCartItemsByUserId(testUserId);
        double discountedPrice = cartItems.get(0).getPrice();
        double expectedTotal = discountedPrice * 2;

        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(expectedTotal);
        order.setFullName("Test Customer");
        order.setAddress("456 Shipping St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("1234567890");
        order.setPaymentMethod("Credit Card");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);
        assertTrue(orderId > 0);

        Order createdOrder = orderDAO.getOrderById(orderId);
        assertTrue(createdOrder.getTotalAmount() < (99.99 * 2));
    }

    @Test
    void testOrderHistory() {
        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(1);
        cartDAO.addToCart(item);

        Order order1 = new Order();
        order1.setUserId(testUserId);
        order1.setTotalAmount(99.99);
        order1.setFullName("Test Customer");
        order1.setAddress("456 Shipping St");
        order1.setCity("Test City");
        order1.setState("Test State");
        order1.setZip("12345");
        order1.setPhone("1234567890");
        order1.setPaymentMethod("Credit Card");
        order1.setStatus("Pending");
        order1.setOrderDate(new Date());
        int orderId1 = orderDAO.createOrder(order1);

        cartDAO.clearCartByUserId(testUserId);

        CartItem item2 = new CartItem();
        item2.setUserId(testUserId);
        item2.setProductId(testProductId);
        item2.setSizeLabel("L");
        item2.setQuantity(1);
        cartDAO.addToCart(item2);

        Order order2 = new Order();
        order2.setUserId(testUserId);
        order2.setTotalAmount(99.99);
        order2.setFullName("Test Customer");
        order2.setAddress("456 Shipping St");
        order2.setCity("Test City");
        order2.setState("Test State");
        order2.setZip("12345");
        order2.setPhone("1234567890");
        order2.setPaymentMethod("PayPal");
        order2.setStatus("Shipped");
        order2.setOrderDate(new Date());
        int orderId2 = orderDAO.createOrder(order2);

        List<Order> userOrders = orderDAO.getOrdersByUserId(testUserId);
        assertEquals(2, userOrders.size());
    }
}
