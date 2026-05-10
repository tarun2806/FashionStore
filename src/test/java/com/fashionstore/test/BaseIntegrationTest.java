package com.fashionstore.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
public abstract class BaseIntegrationTest {

    protected static MySQLContainer<?> mysqlContainer;

    protected static String testDbUrl;
    protected static String testDbUser;
    protected static String testDbPassword;

    @BeforeAll
    static void setupTestDatabase() {
        mysqlContainer = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0")
                .asCompatibleSubstituteFor("mysql")
        )
            .withDatabaseName("fashionstore_test")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("schema.sql")
            .withReuse(true);

        mysqlContainer.start();

        testDbUrl = mysqlContainer.getJdbcUrl();
        testDbUser = mysqlContainer.getUsername();
        testDbPassword = mysqlContainer.getPassword();

        System.setProperty("FASHIONSTORE_DB_URL", testDbUrl);
        System.setProperty("FASHIONSTORE_DB_USER", testDbUser);
        System.setProperty("FASHIONSTORE_DB_PASSWORD", testDbPassword);
        System.setProperty("redis.enabled", "false");
        System.setProperty("csrf.enabled", "false");
    }

    @AfterAll
    static void teardownTestDatabase() {
        if (mysqlContainer != null && mysqlContainer.isRunning()) {
            mysqlContainer.stop();
        }
    }

    protected Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(testDbUrl, testDbUser, testDbPassword);
    }

    protected void executeSql(String sql) throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    protected void executeSql(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    protected void cleanDatabase() throws SQLException {
        executeSql("SET FOREIGN_KEY_CHECKS = 0");
        executeSql("TRUNCATE TABLE order_items");
        executeSql("TRUNCATE TABLE orders");
        executeSql("TRUNCATE TABLE cart_items");
        executeSql("TRUNCATE TABLE wishlist_items");
        executeSql("TRUNCATE TABLE saved_items");
        executeSql("TRUNCATE TABLE product_sizes");
        executeSql("TRUNCATE TABLE products");
        executeSql("TRUNCATE TABLE categories");
        executeSql("TRUNCATE TABLE addresses");
        executeSql("TRUNCATE TABLE users");
        executeSql("TRUNCATE TABLE password_reset_tokens");
        executeSql("TRUNCATE TABLE coupons");
        executeSql("TRUNCATE TABLE coupon_usage");
        executeSql("TRUNCATE TABLE reviews");
        executeSql("TRUNCATE TABLE search_history");
        executeSql("TRUNCATE TABLE recently_viewed");
        executeSql("TRUNCATE TABLE payments");
        executeSql("TRUNCATE TABLE payment_methods");
        executeSql("TRUNCATE TABLE payment_transactions");
        executeSql("SET FOREIGN_KEY_CHECKS = 1");
    }
}
