package com.fashionstore.dao;

import com.fashionstore.daoimpl.CategoryDAOImpl;
import com.fashionstore.daoimpl.ProductDAOImpl;
import com.fashionstore.model.Category;
import com.fashionstore.model.Product;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest extends BaseIntegrationTest {

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        productDAO = new ProductDAOImpl();
        categoryDAO = new CategoryDAOImpl();
        connection = getTestConnection();
        cleanDatabase();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private int createTestCategory() throws SQLException {
        Category category = new Category();
        category.setCategoryName("Test Category");
        category.setDescription("Test Description");
        category.setActive(true);
        return categoryDAO.addCategory(category);
    }

    @Test
    void testAddProduct() throws SQLException {
        int categoryId = createTestCategory();

        Product product = new Product();
        product.setProductName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setDiscountPercent(10.0);
        product.setImageUrl("test.jpg");
        product.setStockQuantity(100);
        product.setCategoryId(categoryId);
        product.setActive(true);
        product.setNew(true);
        product.setSale(false);
        product.setTrending(false);
        product.setBrand("TestBrand");

        int productId = productDAO.addProduct(product);
        assertTrue(productId > 0);

        Product retrieved = productDAO.getProductById(productId);
        assertNotNull(retrieved);
        assertEquals("Test Product", retrieved.getProductName());
        assertEquals(99.99, retrieved.getPrice());
        assertEquals(100, retrieved.getStockQuantity());
        assertTrue(retrieved.isActive());
        assertTrue(retrieved.isNew());
    }

    @Test
    void testGetProductById() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Test Product', 'Description', 149.99, 5.0, 'test.jpg', 50, " + categoryId + ", true, false, true, false, 'BrandName')");

        Product product = productDAO.getProductById(1);
        assertNotNull(product);
        assertEquals("Test Product", product.getProductName());
        assertEquals(149.99, product.getPrice());
        assertTrue(product.isSale());
    }

    @Test
    void testGetProductById_NotFound() {
        Product product = productDAO.getProductById(99999);
        assertNull(product);
    }

    @Test
    void testGetAllProducts() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product1', 'Desc1', 100.0, 0.0, 'img1.jpg', 10, " + categoryId + ", true, true, false, false, 'Brand1')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product2', 'Desc2', 200.0, 0.0, 'img2.jpg', 20, " + categoryId + ", true, false, true, false, 'Brand2')");

        List<Product> products = productDAO.getAllProducts();
        assertEquals(2, products.size());
    }

    @Test
    void testUpdateProduct() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Original Name', 'Original Desc', 100.0, 0.0, 'orig.jpg', 50, " + categoryId + ", true, false, false, false, 'OrigBrand')");

        Product product = productDAO.getProductById(1);
        product.setProductName("Updated Name");
        product.setPrice(150.0);
        product.setStockQuantity(75);

        boolean updated = productDAO.updateProduct(product);
        assertTrue(updated);

        Product retrieved = productDAO.getProductById(1);
        assertEquals("Updated Name", retrieved.getProductName());
        assertEquals(150.0, retrieved.getPrice());
        assertEquals(75, retrieved.getStockQuantity());
    }

    @Test
    void testDeleteProduct() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Delete Me', 'Delete Desc', 100.0, 0.0, 'del.jpg', 50, " + categoryId + ", true, false, false, false, 'DelBrand')");

        boolean deleted = productDAO.deleteProduct(1);
        assertTrue(deleted);

        Product product = productDAO.getProductById(1);
        assertNull(product);
    }

    @Test
    void testSearchProducts() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Blue Shirt', 'A blue shirt', 50.0, 0.0, 'blue.jpg', 100, " + categoryId + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Red Pants', 'Red pants', 75.0, 0.0, 'red.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandB')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Blue Jeans', 'Blue jeans', 80.0, 0.0, 'jeans.jpg', 30, " + categoryId + ", true, false, false, false, 'BrandC')");

        List<Product> blueProducts = productDAO.searchProducts("blue");
        assertEquals(2, blueProducts.size());

        List<Product> redProducts = productDAO.searchProducts("red");
        assertEquals(1, redProducts.size());
    }

    @Test
    void testGetFilteredProducts() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Cheap Product', 'Cheap', 25.0, 0.0, 'cheap.jpg', 100, " + categoryId + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Expensive Product', 'Expensive', 500.0, 0.0, 'exp.jpg', 10, " + categoryId + ", true, false, false, false, 'BrandB')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Medium Product', 'Medium', 100.0, 0.0, 'med.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandC')");

        List<Product> filtered = productDAO.getFilteredProducts(150, null);
        assertEquals(2, filtered.size());
    }

    @Test
    void testUpdateStock() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Stock Product', 'Stock test', 100.0, 0.0, 'stock.jpg', 100, " + categoryId + ", true, false, false, false, 'BrandA')");

        boolean updated = productDAO.updateStock(1, 50);
        assertTrue(updated);

        Product product = productDAO.getProductById(1);
        assertEquals(50, product.getStockQuantity());
    }

    @Test
    void testGetLowStockProductCount() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Low Stock 1', 'Low1', 10.0, 0.0, 'low1.jpg', 5, " + categoryId + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Low Stock 2', 'Low2', 20.0, 0.0, 'low2.jpg', 3, " + categoryId + ", true, false, false, false, 'BrandB')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('High Stock', 'High', 30.0, 0.0, 'high.jpg', 100, " + categoryId + ", true, false, false, false, 'BrandC')");

        int lowStockCount = productDAO.getLowStockProductCount(10);
        assertEquals(2, lowStockCount);
    }

    @Test
    void testGetProductsByCategory() throws SQLException {
        int cat1 = createTestCategory();
        executeSql(connection, "INSERT INTO categories (category_name, description, is_active) VALUES ('Category2', 'Desc2', true)");
        int cat2 = 2;

        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Cat1 Product1', 'Desc1', 10.0, 0.0, 'c1p1.jpg', 50, " + cat1 + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Cat1 Product2', 'Desc2', 20.0, 0.0, 'c1p2.jpg', 50, " + cat1 + ", true, false, false, false, 'BrandB')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Cat2 Product1', 'Desc3', 30.0, 0.0, 'c2p1.jpg', 50, " + cat2 + ", true, false, false, false, 'BrandC')");

        List<Product> cat1Products = productDAO.getProductsByCategory(cat1, 0, 10);
        assertEquals(2, cat1Products.size());

        List<Product> cat2Products = productDAO.getProductsByCategory(cat2, 0, 10);
        assertEquals(1, cat2Products.size());
    }

    @Test
    void testGetFeaturedProducts() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Featured1', 'Feat1', 10.0, 0.0, 'f1.jpg', 50, " + categoryId + ", true, true, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Featured2', 'Feat2', 20.0, 0.0, 'f2.jpg', 50, " + categoryId + ", true, true, false, false, 'BrandB')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Not Featured', 'NotFeat', 30.0, 0.0, 'nf.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandC')");

        List<Product> featured = productDAO.getFeaturedProducts(10);
        assertEquals(2, featured.size());
    }

    @Test
    void testCountProducts() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product1', 'Desc1', 10.0, 0.0, 'p1.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product2', 'Desc2', 20.0, 0.0, 'p2.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandB')");

        int count = productDAO.countProducts(null, null, null);
        assertEquals(2, count);
    }

    @Test
    void testGetProductCount() throws SQLException {
        int categoryId = createTestCategory();
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product1', 'Desc1', 10.0, 0.0, 'p1.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandA')");
        executeSql(connection, "INSERT INTO products (product_name, description, price, discount_percent, image_url, stock_quantity, category_id, is_active, is_new, is_sale, is_trending, brand) VALUES ('Product2', 'Desc2', 20.0, 0.0, 'p2.jpg', 50, " + categoryId + ", true, false, false, false, 'BrandB')");

        int count = productDAO.getProductCount(null, null);
        assertEquals(2, count);
    }
}
