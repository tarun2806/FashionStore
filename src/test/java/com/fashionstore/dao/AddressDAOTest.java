package com.fashionstore.dao;

import com.fashionstore.daoimpl.AddressDAOImpl;
import com.fashionstore.daoimpl.UserDAOImpl;
import com.fashionstore.model.Address;
import com.fashionstore.model.User;
import com.fashionstore.test.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddressDAOTest extends BaseIntegrationTest {

    private AddressDAO addressDAO;
    private UserDAO userDAO;
    private Connection connection;

    private int testUserId;

    @BeforeEach
    void setup() throws SQLException {
        addressDAO = new AddressDAOImpl();
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
    void testAddAddress() {
        Address address = new Address();
        address.setUserId(testUserId);
        address.setAddressType("shipping");
        address.setFullName("John Doe");
        address.setPhone("9876543210");
        address.setAddressLine1("456 Test Street");
        address.setAddressLine2("Apt 123");
        address.setCity("Test City");
        address.setState("Test State");
        address.setPostalCode("12345");
        address.setCountry("USA");
        address.setDefault(true);

        boolean added = addressDAO.addAddress(address);
        assertTrue(added);

        List<Address> addresses = addressDAO.getAddressesByUserId(testUserId);
        assertEquals(1, addresses.size());
        assertEquals("John Doe", addresses.get(0).getFullName());
        assertEquals("shipping", addresses.get(0).getAddressType());
        assertTrue(addresses.get(0).isDefault());
    }

    @Test
    void testGetAddressesByUserId() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'User1', '1111111111', 'Addr1', 'Apt1', 'City1', 'State1', '11111', 'USA', true)");
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'billing', 'User2', '2222222222', 'Addr2', 'Apt2', 'City2', 'State2', '22222', 'USA', false)");

        List<Address> addresses = addressDAO.getAddressesByUserId(testUserId);
        assertEquals(2, addresses.size());
    }

    @Test
    void testGetAddressById() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Test Name', '5555555555', '123 Main St', '', 'TestCity', 'TS', '54321', 'USA', true)");

        Address address = addressDAO.getAddressById(1, testUserId);
        assertNotNull(address);
        assertEquals("Test Name", address.getFullName());
        assertEquals("shipping", address.getAddressType());
        assertEquals("123 Main St", address.getAddressLine1());
    }

    @Test
    void testGetAddressById_WrongUser() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Test Name', '5555555555', '123 Main St', '', 'TestCity', 'TS', '54321', 'USA', true)");

        Address address = addressDAO.getAddressById(1, 999);
        assertNull(address);
    }

    @Test
    void testUpdateAddress() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Original Name', '5555555555', '123 Main St', '', 'TestCity', 'TS', '54321', 'USA', true)");

        Address address = addressDAO.getAddressById(1, testUserId);
        address.setFullName("Updated Name");
        address.setCity("Updated City");
        address.setPostalCode("99999");

        boolean updated = addressDAO.updateAddress(address);
        assertTrue(updated);

        Address retrieved = addressDAO.getAddressById(1, testUserId);
        assertEquals("Updated Name", retrieved.getFullName());
        assertEquals("Updated City", retrieved.getCity());
        assertEquals("99999", retrieved.getPostalCode());
    }

    @Test
    void testDeleteAddress() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Delete Me', '5555555555', '123 Delete St', '', 'DelCity', 'DS', '11111', 'USA', true)");

        boolean deleted = addressDAO.deleteAddress(1, testUserId);
        assertTrue(deleted);

        List<Address> addresses = addressDAO.getAddressesByUserId(testUserId);
        assertTrue(addresses.isEmpty());
    }

    @Test
    void testDeleteAddress_WrongUser() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Delete Me', '5555555555', '123 Delete St', '', 'DelCity', 'DS', '11111', 'USA', true)");

        boolean deleted = addressDAO.deleteAddress(1, 999);
        assertFalse(deleted);

        List<Address> addresses = addressDAO.getAddressesByUserId(testUserId);
        assertEquals(1, addresses.size());
    }

    @Test
    void testGetDefaultAddress() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Default Ship', '1111111111', 'Ship St', '', 'ShipCity', 'SS', '11111', 'USA', true)");
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'billing', 'Default Bill', '2222222222', 'Bill St', '', 'BillCity', 'BS', '22222', 'USA', false)");

        Address defaultShipping = addressDAO.getDefaultAddress(testUserId, "shipping");
        assertNotNull(defaultShipping);
        assertEquals("Default Ship", defaultShipping.getFullName());
        assertTrue(defaultShipping.isDefault());

        Address defaultBilling = addressDAO.getDefaultAddress(testUserId, "billing");
        assertNull(defaultBilling);
    }

    @Test
    void testSetDefaultAddress() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Addr1', '1111111111', 'St1', '', 'City1', 'S1', '11111', 'USA', true)");
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Addr2', '2222222222', 'St2', '', 'City2', 'S2', '22222', 'USA', false)");

        boolean setDefault = addressDAO.setDefaultAddress(2, testUserId);
        assertTrue(setDefault);

        Address addr1 = addressDAO.getAddressById(1, testUserId);
        assertFalse(addr1.isDefault());

        Address addr2 = addressDAO.getAddressById(2, testUserId);
        assertTrue(addr2.isDefault());
    }

    @Test
    void testGetAddressCount() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Count1', '1111111111', 'St1', '', 'City1', 'S1', '11111', 'USA', true)");
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'billing', 'Count2', '2222222222', 'St2', '', 'City2', 'S2', '22222', 'USA', false)");
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'both', 'Count3', '3333333333', 'St3', '', 'City3', 'S3', '33333', 'USA', false)");

        int count = addressDAO.getAddressCount(testUserId);
        assertEquals(3, count);
    }

    @Test
    void testAddressExists() throws SQLException {
        executeSql(connection, "INSERT INTO addresses (user_id, address_type, full_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default) VALUES (" + testUserId + ", 'shipping', 'Exists Test', '5555555555', '123 Exists St', '', 'ExistsCity', 'ES', '54321', 'USA', true)");

        assertTrue(addressDAO.addressExists(1, testUserId));
        assertFalse(addressDAO.addressExists(1, 999));
        assertFalse(addressDAO.addressExists(999, testUserId));
    }

    @Test
    void testGetFullAddress() {
        Address address = new Address();
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Apt 4B");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("USA");

        String fullAddress = address.getFullAddress();
        assertEquals("123 Main Street, Apt 4B, New York, NY - 10001, USA", fullAddress);
    }

    @Test
    void testGetFullAddress_NoLine2() {
        Address address = new Address();
        address.setAddressLine1("456 Oak Avenue");
        address.setAddressLine2("");
        address.setCity("Los Angeles");
        address.setState("CA");
        address.setPostalCode("90001");
        address.setCountry("USA");

        String fullAddress = address.getFullAddress();
        assertEquals("456 Oak Avenue, Los Angeles, CA - 90001, USA", fullAddress);
    }
}
