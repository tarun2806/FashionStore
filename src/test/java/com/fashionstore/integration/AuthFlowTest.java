package com.fashionstore.integration;

import com.fashionstore.dao.UserDAO;
import com.fashionstore.daoimpl.UserDAOImpl;
import com.fashionstore.model.User;
import com.fashionstore.service.UserService;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AuthFlowTest extends BaseIntegrationTest {

    private UserDAO userDAO;
    private UserService userService;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        userDAO = new UserDAOImpl();
        userService = new UserService();
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
    void testCompleteRegistrationLoginFlow() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("auth@example.com");
        user.setPassword("SecurePass123!");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test Street");
        user.setRole("customer");

        int userId = userDAO.registerUser(user);
        assertTrue(userId > 0);

        User registeredUser = userDAO.getUserById(userId);
        assertNotNull(registeredUser);
        assertEquals("auth@example.com", registeredUser.getEmail());

        User loggedInUser = userService.loginUser("auth@example.com", "SecurePass123!");
        assertNotNull(loggedInUser);
        assertEquals(userId, loggedInUser.getUserId());
        assertEquals("Test User", loggedInUser.getFullName());
        assertTrue(loggedInUser.isCustomer());
    }

    @Test
    void testLoginWithWrongPassword() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("wrongpass@example.com");
        user.setPassword("CorrectPass123!");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test Street");
        user.setRole("customer");

        int userId = userDAO.registerUser(user);
        assertTrue(userId > 0);

        User loggedInUser = userService.loginUser("wrongpass@example.com", "WrongPass123!");
        assertNull(loggedInUser);
    }

    @Test
    void testLoginWithNonExistentEmail() {
        User loggedInUser = userService.loginUser("nonexistent@example.com", "SomePass123!");
        assertNull(loggedInUser);
    }

    @Test
    void testEmailUniquenessCheck() {
        User user1 = new User();
        user1.setFullName("User One");
        user1.setEmail("unique@example.com");
        user1.setPassword("Pass123!");
        user1.setPhone("1111111111");
        user1.setGender("Male");
        user1.setAddress("Addr1");
        user1.setRole("customer");

        int userId1 = userDAO.registerUser(user1);
        assertTrue(userId1 > 0);

        assertTrue(userDAO.isEmailExists("unique@example.com"));
        assertFalse(userDAO.isEmailExists("notexists@example.com"));

        User user2 = new User();
        user2.setFullName("User Two");
        user2.setEmail("unique@example.com");
        user2.setPassword("Pass456!");
        user2.setPhone("2222222222");
        user2.setGender("Female");
        user2.setAddress("Addr2");
        user2.setRole("customer");

        int userId2 = userDAO.registerUser(user2);
        assertEquals(-1, userId2);
    }

    @Test
    void testPasswordChangeFlow() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("password@example.com");
        user.setPassword("OldPass123!");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("123 Test Street");
        user.setRole("customer");

        int userId = userDAO.registerUser(user);
        assertTrue(userId > 0);

        User loggedInUser = userService.loginUser("password@example.com", "OldPass123!");
        assertNotNull(loggedInUser);

        boolean passwordChanged = userDAO.changePassword(userId, "NewPass456!");
        assertTrue(passwordChanged);

        User loginWithOldPass = userService.loginUser("password@example.com", "OldPass123!");
        assertNull(loginWithOldPass);

        User loginWithNewPass = userService.loginUser("password@example.com", "NewPass456!");
        assertNotNull(loginWithNewPass);
    }

    @Test
    void testAdminRoleFlow() {
        User admin = new User();
        admin.setFullName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("AdminPass123!");
        admin.setPhone("1234567890");
        admin.setGender("Male");
        admin.setAddress("Admin Street");
        admin.setRole("admin");

        int userId = userDAO.registerUser(admin);
        assertTrue(userId > 0);

        User loggedInAdmin = userService.loginUser("admin@example.com", "AdminPass123!");
        assertNotNull(loggedInAdmin);
        assertTrue(loggedInAdmin.isAdmin());
        assertFalse(loggedInAdmin.isCustomer());
    }

    @Test
    void testUserRoleUpgrade() {
        User user = new User();
        user.setFullName("Regular User");
        user.setEmail("regular@example.com");
        user.setPassword("RegularPass123!");
        user.setPhone("1234567890");
        user.setGender("Male");
        user.setAddress("Regular Street");
        user.setRole("customer");

        int userId = userDAO.registerUser(user);
        assertTrue(userId > 0);

        User userBefore = userDAO.getUserById(userId);
        assertTrue(userBefore.isCustomer());
        assertFalse(userBefore.isAdmin());

        boolean upgraded = userDAO.updateUserRole(userId, "admin");
        assertTrue(upgraded);

        User userAfter = userDAO.getUserById(userId);
        assertTrue(userAfter.isAdmin());
        assertFalse(userAfter.isCustomer());
    }
}
