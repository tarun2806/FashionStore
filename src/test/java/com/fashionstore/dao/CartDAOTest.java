package com.fashionstore.dao;

import com.fashionstore.daoimpl.CartDAOImpl;
import com.fashionstore.daoimpl.CategoryDAOImpl;
import com.fashionstore.daoimpl.ProductDAOImpl;
import com.fashionstore.daoimpl.UserDAOImpl;
import com.fashionstore.model.CartItem;
import com.fashionstore.model.Category;
import com.fashionstore.model.Product;
import com.fashionstore.model.User;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartDAOTest extends BaseIntegrationTest {

    private CartDAO cartDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private Connection connection;

    private int testUserId;
    private int testProductId;

    @BeforeEach
    void setup() throws SQLException {
        cartDAO = new CartDAOImpl();
        userDAO = new UserDAOImpl();
        productDAO = new ProductDAOImpl();
        categoryDAO = new CategoryDAOImpl();
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
        user.setFullName("Test User");
        user.setEmail("test@example.com");
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
        product.setDiscountPercent(0.0);
        product.setImageUrl("test.jpg");
        product.setStockQuantity(100);
        product.setCategoryId(categoryId);
        product.setActive(true);
        product.setNew(false);
        product.setSale(false);
        product.setTrending(false);
        product.setBrand("TestBrand");
        testProductId = productDAO.addProduct(product);
    }

    @Test
    void testAddToCart() {
        CartItem item = new CartItem();
        item.setUserId(testUserId);
        item.setProductId(testProductId);
        item.setSizeLabel("M");
        item.setQuantity(2);

        int cartItemId = cartDAO.addToCart(item);
        assertTrue(cartItemId > 0);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals("M", items.get(0).getSizeLabel());
    }

    @Test
    void testGetCartItemsByUserId() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'L', 3)");

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(1, items.size());
        assertEquals(testUserId, items.get(0).getUserId());
        assertEquals(testProductId, items.get(0).getProductId());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals("L", items.get(0).getSizeLabel());
    }

    @Test
    void testGetCartItemsByUserId_EmptyCart() {
        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertTrue(items.isEmpty());
    }

    @Test
    void testUpdateQuantity() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'M', 2)");

        boolean updated = cartDAO.updateQuantity(1, testUserId, 5);
        assertTrue(updated);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(5, items.get(0).getQuantity());
    }

    @Test
    void testUpdateQuantity_WrongUser() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'M', 2)");

        boolean updated = cartDAO.updateQuantity(1, 999, 5);
        assertFalse(updated);
    }

    @Test
    void testRemoveCartItem() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'S', 1)");

        boolean removed = cartDAO.removeCartItem(1, testUserId);
        assertTrue(removed);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertTrue(items.isEmpty());
    }

    @Test
    void testRemoveCartItem_WrongUser() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'S', 1)");

        boolean removed = cartDAO.removeCartItem(1, 999);
        assertFalse(removed);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(1, items.size());
    }

    @Test
    void testClearCartByUserId() throws SQLException {
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'M', 2)");
        executeSql(connection, "INSERT INTO cart_items (user_id, product_id, size_label, quantity) VALUES (" + testUserId + ", " + testProductId + ", 'L', 1)");

        boolean cleared = cartDAO.clearCartByUserId(testUserId);
        assertTrue(cleared);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertTrue(items.isEmpty());
    }

    @Test
    void testMultipleItemsInCart() throws SQLException {
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

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(2, items.size());
    }

    @Test
    void testAddSameProductDifferentSizes() throws SQLException {
        CartItem item1 = new CartItem();
        item1.setUserId(testUserId);
        item1.setProductId(testProductId);
        item1.setSizeLabel("M");
        item1.setQuantity(2);
        cartDAO.addToCart(item1);

        CartItem item2 = new CartItem();
        item2.setUserId(testUserId);
        item2.setProductId(testProductId);
        item2.setSizeLabel("L");
        item2.setQuantity(1);
        cartDAO.addToCart(item2);

        List<CartItem> items = cartDAO.getCartItemsByUserId(testUserId);
        assertEquals(2, items.size());
    }
}
