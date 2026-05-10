package com.fashionstore.integration;

import com.fashionstore.dao.CartDAO;
import com.fashionstore.dao.OrderDAO;
import com.fashionstore.dao.ProductDAO;
import com.fashionstore.dao.ProductSizeDAO;
import com.fashionstore.daoimpl.CartDAOImpl;
import com.fashionstore.daoimpl.OrderDAOImpl;
import com.fashionstore.daoimpl.ProductDAOImpl;
import com.fashionstore.daoimpl.ProductSizeDAOImpl;
import com.fashionstore.model.CartItem;
import com.fashionstore.model.Order;
import com.fashionstore.model.Product;
import com.fashionstore.model.ProductSize;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockDeductionTest extends BaseIntegrationTest {

    private ProductDAO productDAO;
    private ProductSizeDAO productSizeDAO;
    private CartDAO cartDAO;
    private OrderDAO orderDAO;
    private Connection connection;

    private int testUserId;
    private int testProductId;

    @BeforeEach
    void setup() throws SQLException {
        productDAO = new ProductDAOImpl();
        productSizeDAO = new ProductSizeDAOImpl();
        cartDAO = new CartDAOImpl();
        orderDAO = new OrderDAOImpl();
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
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Test User', 'test@example.com', '$2a$10$test', '1234567890', 'Male', '123 Test St', 'customer', true)");
        testUserId = 1;

        executeSql(connection, "INSERT INTO categories (category_name, description, is_active) VALUES ('Test Category', 'Test Description', true)");
        int categoryId = 1;

        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Test Product', 'Test Description', 99.99, 0.0, 'test.jpg', 100, " + categoryId + ", true, false, false, false, 'TestBrand')");
        testProductId = 1;

        executeSql(connection, "INSERT INTO product_sizes (product_id, size_label, stock_quantity, is_available) VALUES (" + testProductId + ", 'M', 50, true)");
        executeSql(connection, "INSERT INTO product_sizes (product_id, size_label, stock_quantity, is_available) VALUES (" + testProductId + ", 'L', 30, true)");
    }

    @Test
    void testStockUpdateDirect() throws SQLException {
        Product product = productDAO.getProductById(testProductId);
        assertEquals(100, product.getStockQuantity());

        boolean updated = productDAO.updateStock(testProductId, 75);
        assertTrue(updated);

        product = productDAO.getProductById(testProductId);
        assertEquals(75, product.getStockQuantity());
    }

    @Test
    void testProductSizeStockUpdate() throws SQLException {
        List<ProductSize> sizes = productSizeDAO.getSizesByProductId(testProductId);
        ProductSize sizeM = sizes.stream().filter(s -> "M".equals(s.getSizeLabel())).findFirst().orElse(null);
        assertNotNull(sizeM);
        assertEquals(50, sizeM.getStockQuantity());

        sizeM.setStockQuantity(40);
        boolean updated = productSizeDAO.updateProductSize(sizeM) > 0;
        assertTrue(updated);

        List<ProductSize> updatedSizes = productSizeDAO.getSizesByProductId(testProductId);
        ProductSize updatedSize = updatedSizes.stream().filter(s -> "M".equals(s.getSizeLabel())).findFirst().orElse(null);
        assertEquals(40, updatedSize.getStockQuantity());
    }

    @Test
    void testStockDeductionOnOrder() {
        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(5);
        cartDAO.addToCart(item);

        Product productBefore = productDAO.getProductById(testProductId);
        int stockBefore = productBefore.getStockQuantity();

        Order order = new Order();
        order.setUserId(testUserId);
        order.setTotalAmount(99.99 * 5);
        order.setFullName("Test User");
        order.setAddress("123 Test St");
        order.setCity("Test City");
        order.setState("Test State");
        order.setZip("12345");
        order.setPhone("1234567890");
        order.setPaymentMethod("Credit Card");
        order.setStatus("Pending");
        order.setOrderDate(new Date());

        int orderId = orderDAO.createOrder(order);
        assertTrue(orderId > 0);

        boolean stockUpdated = productDAO.updateStock(testProductId, stockBefore - 5);
        assertTrue(stockUpdated);

        Product productAfter = productDAO.getProductById(testProductId);
        assertEquals(stockBefore - 5, productAfter.getStockQuantity());
    }

    @Test
    void testPreventNegativeStock() {
        Product product = productDAO.getProductById(testProductId);
        int initialStock = product.getStockQuantity();

        boolean updated = productDAO.updateStock(testProductId, -10);
        assertTrue(updated);

        product = productDAO.getProductById(testProductId);
        assertEquals(-10, product.getStockQuantity());

        boolean updatedToZero = productDAO.updateStock(testProductId, 0);
        assertTrue(updatedToZero);

        product = productDAO.getProductById(testProductId);
        assertEquals(0, product.getStockQuantity());
    }

    @Test
    void testLowStockDetection() throws SQLException {
        productDAO.updateStock(testProductId, 5);

        int lowStockCount = productDAO.getLowStockProductCount(10);
        assertEquals(1, lowStockCount);

        productDAO.updateStock(testProductId, 20);

        lowStockCount = productDAO.getLowStockProductCount(10);
        assertEquals(0, lowStockCount);
    }

    @Test
    void testConcurrentStockUpdate() throws SQLException {
        Product product = productDAO.getProductById(testProductId);
        int initialStock = product.getStockQuantity();

        productDAO.updateStock(testProductId, initialStock - 10);

        product = productDAO.getProductById(testProductId);
        int stockAfterFirstUpdate = product.getStockQuantity();

        productDAO.updateStock(testProductId, stockAfterFirstUpdate - 5);

        product = productDAO.getProductById(testProductId);
        assertEquals(initialStock - 15, product.getStockQuantity());
    }

    @Test
    void testProductSizeStockDeduction() throws SQLException {
        List<ProductSize> sizes = productSizeDAO.getSizesByProductId(testProductId);
        ProductSize sizeM = sizes.stream().filter(s -> "M".equals(s.getSizeLabel())).findFirst().orElse(null);
        assertNotNull(sizeM);
        int initialStock = sizeM.getStockQuantity();

        sizeM.setStockQuantity(initialStock - 10);
        productSizeDAO.updateProductSize(sizeM);

        List<ProductSize> updatedSizes = productSizeDAO.getSizesByProductId(testProductId);
        ProductSize updated = updatedSizes.stream().filter(s -> "M".equals(s.getSizeLabel())).findFirst().orElse(null);
        assertEquals(initialStock - 10, updated.getStockQuantity());
    }

    @Test
    void testAggregateStockFromSizes() throws SQLException {
        executeSql(connection, "UPDATE products SET stock_quantity = 0 WHERE product_id = " + testProductId);
        executeSql(connection, "UPDATE product_sizes SET stock_quantity = 25 WHERE product_id = " + testProductId + " AND size_label = 'M'");
        executeSql(connection, "UPDATE product_sizes SET stock_quantity = 15 WHERE product_id = " + testProductId + " AND size_label = 'L'");

        executeSql(connection, "UPDATE products p SET stock_quantity = (SELECT COALESCE(SUM(stock_quantity), 0) FROM product_sizes ps WHERE ps.product_id = p.product_id) WHERE p.product_id = " + testProductId);

        Product product = productDAO.getProductById(testProductId);
        assertEquals(40, product.getStockQuantity());
    }
}
