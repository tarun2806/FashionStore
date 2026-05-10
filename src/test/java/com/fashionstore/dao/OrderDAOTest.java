package com.fashionstore.dao;

import com.fashionstore.daoimpl.OrderDAOImpl;
import com.fashionstore.daoimpl.UserDAOImpl;
import com.fashionstore.model.Order;
import com.fashionstore.model.User;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest extends BaseIntegrationTest {

    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private Connection connection;

    private int testUserId;

    @BeforeEach
    void setup() throws SQLException {
        orderDAO = new OrderDAOImpl();
        userDAO = new UserDAOImpl();
        connection = getTestConnection();
        cleanDatabase();

        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test St");
        user.setRole("customer");
        testUserId = userDAO.registerUser(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testCreateOrder() {
        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(199.99);
        order.setFullName("John Doe");
        order.setAddress("456 Order St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("9876543210");
        order.setPaymentMethod("Credit Card");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);
        assertTrue(orderId > 0);

        Order retrieved = orderDAO.getOrderById(orderId);
        assertNotNull(retrieved);
        assertEquals(testUserId, retrieved.getUserId());
        assertEquals(199.99, retrieved.getTotalAmount());
        assertEquals("John Doe", retrieved.getFullName());
        assertEquals("Pending", retrieved.getStatus());
    }

    @Test
    void testGetOrderById() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 299.99, 'Jane Doe', '789 Order Ave', 'Order City', 'Order State', '54321', '5555555555', 'PayPal', 'Shipped', NOW())");

        Order order = orderDAO.getOrderById(1);
        assertNotNull(order);
        assertEquals(testUserId, order.getUserId());
        assertEquals(299.99, order.getTotalAmount());
        assertEquals("Jane Doe", order.getFullName());
        assertEquals("Shipped", order.getStatus());
    }

    @Test
    void testGetOrderById_NotFound() {
        Order order = orderDAO.getOrderById(99999);
        assertNull(order);
    }

    @Test
    void testGetOrdersByUserId() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'User1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'User2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");

        List<Order> orders = orderDAO.getOrdersByUserId(testUserId);
        assertEquals(2, orders.size());
    }

    @Test
    void testGetOrdersByUserId_NoOrders() {
        List<Order> orders = orderDAO.getOrdersByUserId(testUserId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void testGetAllOrders() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Order1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'Order2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");

        List<Order> orders = orderDAO.getAllOrders();
        assertEquals(2, orders.size());
    }

    @Test
    void testUpdateOrderStatus() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Status Test', 'Addr', 'City', 'State', '11111', '1111111111', 'Card', 'Pending', NOW())");

        boolean updated = orderDAO.updateOrderStatus(1, "Shipped");
        assertTrue(updated);

        Order order = orderDAO.getOrderById(1);
        assertEquals("Shipped", order.getStatus());
    }

    @Test
    void testGetTotalRevenue() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Rev1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'Rev2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 300.0, 'Rev3', 'Addr3', 'City3', 'State3', '33333', '3333333333', 'Card3', 'Delivered', NOW())");

        double revenue = orderDAO.getTotalRevenue();
        assertEquals(600.0, revenue, 0.01);
    }

    @Test
    void testGetTotalOrderCount() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Count1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'Count2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 300.0, 'Count3', 'Addr3', 'City3', 'State3', '33333', '3333333333', 'Card3', 'Delivered', NOW())");

        int count = orderDAO.getTotalOrderCount();
        assertEquals(3, count);
    }

    @Test
    void testGetRecentOrders() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Recent1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'Recent2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 300.0, 'Recent3', 'Addr3', 'City3', 'State3', '33333', '3333333333', 'Card3', 'Delivered', NOW())");

        List<Order> recentOrders = orderDAO.getRecentOrders(2);
        assertEquals(2, recentOrders.size());
    }

    @Test
    void testGetOrdersInLastDays() throws SQLException {
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 100.0, 'Last1', 'Addr1', 'City1', 'State1', '11111', '1111111111', 'Card1', 'Pending', NOW())");
        executeSql(connection, "INSERT INTO orders (user_id, total_amount, full_name, address, city, state, zip, phone, payment_method, status, order_date) VALUES (" + testUserId + ", 200.0, 'Last2', 'Addr2', 'City2', 'State2', '22222', '2222222222', 'Card2', 'Shipped', NOW())");

        List<Order> recentOrders = orderDAO.getOrdersInLastDays(7);
        assertEquals(2, recentOrders.size());
    }
}
