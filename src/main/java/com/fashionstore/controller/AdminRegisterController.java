package com.fashionstore.controller;

import com.fashionstore.model.User;
import com.fashionstore.security.CSRFProtection;
import com.fashionstore.security.RateLimiter;
import com.fashionstore.service.UserService;
import com.fashionstore.util.AuditLogger;
import com.fashionstore.validation.Validator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Admin registration endpoint.
 * Accessible without login but protected by an admin secret key.
 * The secret key is read from the FASHIONSTORE_ADMIN_KEY environment variable
 * and falls back to a default for local development.
 */
@WebServlet("/admin/register")
public class AdminRegisterController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService;

    private static final String DEFAULT_ADMIN_KEY = "FS_ADMIN_SECRET_2026";

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Rate limiting
        if (!RateLimiter.checkRateLimit(request, "/admin/register")) {
            request.setAttribute("error", "Too many attempts. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                   .forward(request, response);
            return;
        }

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String adminKey = request.getParameter("adminKey");

        // Validate admin secret key
        String expectedKey = System.getenv("FASHIONSTORE_ADMIN_KEY");
        if (expectedKey == null || expectedKey.isBlank()) {
            expectedKey = DEFAULT_ADMIN_KEY;
        }

        if (!expectedKey.equals(adminKey)) {
            request.setAttribute("error", "Invalid admin secret key.");
            request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                   .forward(request, response);
            return;
        }

        // Centralized validation
        Validator validator = Validator.create()
            .validateName(fullName, "Full name")
            .validateEmail(email, "Email")
            .validatePhone(phone, "Phone")
            .validatePassword(password, "Password")
            .validateMatch(password, confirmPassword, "Passwords");

        if (validator.hasErrors()) {
            request.setAttribute("error", validator.getFirstError());
            request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                   .forward(request, response);
            return;
        }

        // Check if email already exists
        if (userService.isEmailExists(email)) {
            request.setAttribute("error", "An account with this email already exists.");
            request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                   .forward(request, response);
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
                AuditLogger.log("ADMIN_REGISTERED", "New admin registered: " + email, String.valueOf(userId), request);
                RateLimiter.resetRateLimit(request, "/admin/register");

                // Auto-login the new admin
                User createdUser = userService.getUserById(userId);
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", createdUser.getUserId());
                session.setAttribute("user", createdUser);
                CSRFProtection.generateToken(request);

                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                request.setAttribute("error", "Registration failed. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                       .forward(request, response);
            }

        } catch (Exception e) {
            request.setAttribute("error", "An error occurred during registration.");
            request.getRequestDispatcher("/WEB-INF/views/admin-register.jsp")
                   .forward(request, response);
        }
    }
}
