package com.fashionstore.dao;

import com.fashionstore.daoimpl.UserDAOImpl;
import com.fashionstore.model.User;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends BaseIntegrationTest {

    private UserDAO userDAO;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        userDAO = new UserDAOImpl();
        connection = getTestConnection();
        cleanDatabase();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testRegisterUser() throws SQLException {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test Street");
        user.setRole("customer");

        int userId = userDAO.registerUser(user);
        assertTrue(userId > 0);

        User retrieved = userDAO.getUserById(userId);
        assertNotNull(retrieved);
        assertEquals("John Doe", retrieved.getFullName());
        assertEquals("john@example.com", retrieved.getEmail());
        assertTrue(BCrypt.checkpw("password123", retrieved.getPassword()));
        assertEquals("customer", retrieved.getRole());
    }

    @Test
    void testGetUserById() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Jane Doe', 'jane@example.com', '$2a$10$test', '9876543210', 'Female', '456 Test Ave', 'customer', true)");

        User user = userDAO.getUserById(1);
        assertNotNull(user);
        assertEquals("Jane Doe", user.getFullName());
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        User user = userDAO.getUserById(99999);
        assertNull(user);
    }

    @Test
    void testGetUserByEmail() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Test User', 'test@example.com', '$2a$10$test', '5551234567', 'Other', '789 Test Blvd', 'customer', true)");

        User user = userDAO.getUserByEmail("test@example.com");
        assertNotNull(user);
        assertEquals("Test User", user.getFullName());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        User user = userDAO.getUserByEmail("nonexistent@example.com");
        assertNull(user);
    }

    @Test
    void testLoginUser() throws SQLException {
        String hashedPassword = BCrypt.hashpw("testpassword", BCrypt.gensalt());
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Login User', 'login@example.com', '" + hashedPassword + "', '5551234567', 'Male', 'Login Addr', 'customer', true)");

        User user = userDAO.loginUser("login@example.com", "testpassword");
        assertNotNull(user);
        assertEquals("Login User", user.getFullName());
        assertTrue(user.isCustomer());
    }

    @Test
    void testLoginUser_WrongPassword() throws SQLException {
        String hashedPassword = BCrypt.hashpw("testpassword", BCrypt.gensalt());
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Login User', 'login@example.com', '" + hashedPassword + "', '5551234567', 'Male', 'Login Addr', 'customer', true)");

        User user = userDAO.loginUser("login@example.com", "wrongpassword");
        assertNull(user);
    }

    @Test
    void testIsEmailExists() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Email Test', 'emailexists@example.com', '$2a$10$test', '5551234567', 'Male', 'Email Addr', 'customer', true)");

        assertTrue(userDAO.isEmailExists("emailexists@example.com"));
        assertFalse(userDAO.isEmailExists("notexists@example.com"));
    }

    @Test
    void testGetAllUsers() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('User1', 'user1@example.com', '$2a$10$test', '1111111111', 'Male', 'Addr1', 'customer', true)");
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('User2', 'user2@example.com', '$2a$10$test', '2222222222', 'Female', 'Addr2', 'admin', true)");

        List<User> users = userDAO.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Original Name', 'original@example.com', '$2a$10$test', '3333333333', 'Male', 'Original Addr', 'customer', true)");

        User user = userDAO.getUserById(1);
        user.setFullName("Updated Name");
        user.setPhone("9999999999");

        boolean updated = userDAO.updateUser(user);
        assertTrue(updated);

        User retrieved = userDAO.getUserById(1);
        assertEquals("Updated Name", retrieved.getFullName());
        assertEquals("9999999999", retrieved.getPhone());
    }

    @Test
    void testChangePassword() throws SQLException {
        String oldPassword = BCrypt.hashpw("oldpassword", BCrypt.gensalt());
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Password User', 'password@example.com', '" + oldPassword + "', '5551234567', 'Male', 'Password Addr', 'customer', true)");

        boolean changed = userDAO.changePassword(1, "newpassword");
        assertTrue(changed);

        User user = userDAO.getUserById(1);
        assertFalse(BCrypt.checkpw("oldpassword", user.getPassword()));
        assertTrue(BCrypt.checkpw("newpassword", user.getPassword()));
    }

    @Test
    void testUpdateUserRole() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Role User', 'role@example.com', '$2a$10$test', '5555555555', 'Female', 'Role Addr', 'customer', true)");

        boolean updated = userDAO.updateUserRole(1, "admin");
        assertTrue(updated);

        User user = userDAO.getUserById(1);
        assertEquals("admin", user.getRole());
        assertTrue(user.isAdmin());
    }

    @Test
    void testGetTotalUserCount() throws SQLException {
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Count1', 'count1@example.com', '$2a$10$test', '6666666666', 'Male', 'Count1 Addr', 'customer', true)");
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Count2', 'count2@example.com', '$2a$10$test', '7777777777', 'Female', 'Count2 Addr', 'customer', true)");
        executeSql(connection, "INSERT INTO users (full_name, email, password, phone, gender, address, role, is_active) VALUES ('Count3', 'count3@example.com', '$2a$10$test', '8888888888', 'Other', 'Count3 Addr', 'customer', true)");

        int count = userDAO.getTotalUserCount();
        assertEquals(3, count);
    }

    @Test
    void testDuplicateEmail() throws SQLException {
        User user1 = new User();
        user1.setFullName("First User");
        user1.setEmail("duplicate@example.com");
        user1.setPassword("password123");
        user1.setRole("customer");
        userDAO.registerUser(user1);

        User user2 = new User();
        user2.setFullName("Second User");
        user2.setEmail("duplicate@example.com");
        user2.setPassword("password456");
        user2.setRole("customer");

        int userId = userDAO.registerUser(user2);
        assertEquals(-1, userId);
    }
}
