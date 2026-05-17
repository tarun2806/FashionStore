package com.fashionstore.controller;

import com.fashionstore.model.User;
import com.fashionstore.security.CSRFProtection;
import com.fashionstore.service.RecommendationEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for recommendation engine and personalization
 * Handles intelligent product recommendations and user personalization
 */
@WebServlet("/api/recommendations/*")
public class RecommendationEngineController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationEngineController.class);
    private RecommendationEngineService recommendationService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        recommendationService = new RecommendationEngineService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check user authentication
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("customerAuth") : null;
        
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if ("/homepage".equals(pathInfo)) {
                getHomepageRecommendations(request, response, user);
            } else if ("/pdp".equals(pathInfo)) {
                getPDPRecommendations(request, response, user);
            } else if ("/cart".equals(pathInfo)) {
                getCartRecommendations(request, response, user);
            } else if ("/checkout".equals(pathInfo)) {
                getCheckoutRecommendations(request, response, user);
            } else if ("/product".equals(pathInfo)) {
                getProductRecommendations(request, response, user);
            } else if ("/category".equals(pathInfo)) {
                getCategoryRecommendations(request, response, user);
            } else if ("/similar".equals(pathInfo)) {
                getSimilarProducts(request, response, user);
            } else if ("/also-bought".equals(pathInfo)) {
                getAlsoBoughtProducts(request, response, user);
            } else if ("/trending".equals(pathInfo)) {
                getTrendingProducts(request, response, user);
            } else if ("/personalized".equals(pathInfo)) {
                getPersonalizedRecommendations(request, response, user);
            } else if ("/collaborative".equals(pathInfo)) {
                getCollaborativeRecommendations(request, response, user);
            } else if ("/wishlist-based".equals(pathInfo)) {
                getWishlistBasedRecommendations(request, response, user);
            } else if ("/browsing-history".equals(pathInfo)) {
                getBrowsingHistoryRecommendations(request, response, user);
            } else if ("/purchase-history".equals(pathInfo)) {
                getPurchaseHistoryRecommendations(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error in RecommendationEngineController doGet: {}", e.getMessage(), e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check user authentication
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("customerAuth") : null;
        
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // CSRF validation for POST requests
        if (!CSRFProtection.validateRequest(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if ("/feedback".equals(pathInfo)) {
                submitRecommendationFeedback(request, response, user);
            } else if ("/update-preferences".equals(pathInfo)) {
                updateRecommendationPreferences(request, response, user);
            } else if ("/track-click".equals(pathInfo)) {
                trackRecommendationClick(request, response, user);
            } else if ("/track-impression".equals(pathInfo)) {
                trackRecommendationImpression(request, response, user);
            } else if ("/track-purchase".equals(pathInfo)) {
                trackRecommendationPurchase(request, response, user);
            } else if ("/refresh".equals(pathInfo)) {
                refreshRecommendations(request, response, user);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error in RecommendationEngineController doPost: {}", e.getMessage(), e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getHomepageRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        String algorithm = request.getParameter("algorithm"); // collaborative, content_based, hybrid, trending
        
        Map<String, Object> recommendations = recommendationService.getHomepageRecommendations(
            user.getUserId(), limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getPDPRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int productId = parseIntParameter(request.getParameter("productId"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 8);
        String algorithm = request.getParameter("algorithm");
        
        if (productId <= 0) {
            sendErrorResponse(response, "Product ID is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<String, Object> recommendations = recommendationService.getPDPRecommendations(
            user.getUserId(), productId, limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getCartRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 6);
        String algorithm = request.getParameter("algorithm");
        
        Map<String, Object> recommendations = recommendationService.getCartRecommendations(
            user.getUserId(), limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getCheckoutRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 4);
        String algorithm = request.getParameter("algorithm");
        
        Map<String, Object> recommendations = recommendationService.getCheckoutRecommendations(
            user.getUserId(), limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getProductRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int productId = parseIntParameter(request.getParameter("productId"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 10);
        String algorithm = request.getParameter("algorithm");
        
        if (productId <= 0) {
            sendErrorResponse(response, "Product ID is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<String, Object> recommendations = recommendationService.getProductRecommendations(
            user.getUserId(), productId, limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getCategoryRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int categoryId = parseIntParameter(request.getParameter("categoryId"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        String algorithm = request.getParameter("algorithm");
        
        Map<String, Object> recommendations = recommendationService.getCategoryRecommendations(
            user.getUserId(), categoryId, limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getSimilarProducts(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int productId = parseIntParameter(request.getParameter("productId"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 8);
        
        if (productId <= 0) {
            sendErrorResponse(response, "Product ID is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<String, Object> recommendations = recommendationService.getSimilarProducts(
            user.getUserId(), productId, limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getAlsoBoughtProducts(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int productId = parseIntParameter(request.getParameter("productId"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 8);
        
        if (productId <= 0) {
            sendErrorResponse(response, "Product ID is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Map<String, Object> recommendations = recommendationService.getAlsoBoughtProducts(
            user.getUserId(), productId, limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getTrendingProducts(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        String category = request.getParameter("category");
        String timeRange = request.getParameter("timeRange"); // day, week, month
        
        Map<String, Object> recommendations = recommendationService.getTrendingProducts(
            user.getUserId(), limit, category, timeRange);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getPersonalizedRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        String context = request.getParameter("context"); // homepage, pdp, cart
        
        Map<String, Object> recommendations = recommendationService.getPersonalizedRecommendations(
            user.getUserId(), limit, context);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getCollaborativeRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        String algorithm = request.getParameter("algorithm"); // user_based, item_based
        
        Map<String, Object> recommendations = recommendationService.getCollaborativeRecommendations(
            user.getUserId(), limit, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getWishlistBasedRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        
        Map<String, Object> recommendations = recommendationService.getWishlistBasedRecommendations(
            user.getUserId(), limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getBrowsingHistoryRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        
        Map<String, Object> recommendations = recommendationService.getBrowsingHistoryRecommendations(
            user.getUserId(), limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void getPurchaseHistoryRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        int limit = parseIntParameter(request.getParameter("limit"), 12);
        
        Map<String, Object> recommendations = recommendationService.getPurchaseHistoryRecommendations(
            user.getUserId(), limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("recommendations", recommendations);
        
        sendJsonResponse(response, data);
    }

    private void submitRecommendationFeedback(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        try {
            Map<String, Object> feedbackData = objectMapper.readValue(request.getReader(), Map.class);
            
            Map<String, Object> result = recommendationService.submitRecommendationFeedback(user.getUserId(), feedbackData);
            
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.get("success"));
            data.put("message", result.get("message"));
            
            sendJsonResponse(response, data);
        } catch (Exception e) {
            logger.error("Error parsing feedback data: {}", e.getMessage(), e);
            sendErrorResponse(response, "Invalid feedback data", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void updateRecommendationPreferences(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        try {
            Map<String, Object> preferences = objectMapper.readValue(request.getReader(), Map.class);
            
            Map<String, Object> result = recommendationService.updateRecommendationPreferences(user.getUserId(), preferences);
            
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.get("success"));
            data.put("message", result.get("message"));
            
            sendJsonResponse(response, data);
        } catch (Exception e) {
            logger.error("Error parsing preferences data: {}", e.getMessage(), e);
            sendErrorResponse(response, "Invalid preferences data", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void trackRecommendationClick(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        try {
            Map<String, Object> clickData = objectMapper.readValue(request.getReader(), Map.class);
            
            Map<String, Object> result = recommendationService.trackRecommendationClick(user.getUserId(), clickData);
            
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.get("success"));
            data.put("message", result.get("message"));
            
            sendJsonResponse(response, data);
        } catch (Exception e) {
            logger.error("Error parsing click data: {}", e.getMessage(), e);
            sendErrorResponse(response, "Invalid click data", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void trackRecommendationImpression(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        try {
            Map<String, Object> impressionData = objectMapper.readValue(request.getReader(), Map.class);
            
            Map<String, Object> result = recommendationService.trackRecommendationImpression(user.getUserId(), impressionData);
            
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.get("success"));
            data.put("message", result.get("message"));
            
            sendJsonResponse(response, data);
        } catch (Exception e) {
            logger.error("Error parsing impression data: {}", e.getMessage(), e);
            sendErrorResponse(response, "Invalid impression data", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void trackRecommendationPurchase(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        try {
            Map<String, Object> purchaseData = objectMapper.readValue(request.getReader(), Map.class);
            
            Map<String, Object> result = recommendationService.trackRecommendationPurchase(user.getUserId(), purchaseData);
            
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.get("success"));
            data.put("message", result.get("message"));
            
            sendJsonResponse(response, data);
        } catch (Exception e) {
            logger.error("Error parsing purchase data: {}", e.getMessage(), e);
            sendErrorResponse(response, "Invalid purchase data", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void refreshRecommendations(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        
        String context = request.getParameter("context");
        String algorithm = request.getParameter("algorithm");
        
        Map<String, Object> result = recommendationService.refreshRecommendations(user.getUserId(), context, algorithm);
        
        Map<String, Object> data = new HashMap<>();
        data.put("success", result.get("success"));
        data.put("message", result.get("message"));
        if (result.containsKey("recommendations")) {
            data.put("recommendations", result.get("recommendations"));
        }
        
        sendJsonResponse(response, data);
    }

    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(data));
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private int parseIntParameter(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
