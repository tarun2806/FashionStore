package com.fashionstore.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise-grade security hardening filter
 * Implements rate limiting, brute-force protection, and security headers
 */
public class SecurityHardeningFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityHardeningFilter.class);
    
    // Rate limiting configurations
    private static final int MAX_REQUESTS_PER_MINUTE = 1000;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCKOUT_MINUTES = 15;
    private static final int MAX_FAILED_REQUESTS = 50;
    
    // Rate limiting storage
    private final Map<String, RequestTracker> requestTrackers = new ConcurrentHashMap<>();
    private final Map<String, LoginTracker> loginTrackers = new ConcurrentHashMap<>();
    private final Map<String, FailedRequestTracker> failedRequestTrackers = new ConcurrentHashMap<>();
    
    // Suspicious activity patterns
    private static final Set<String> SUSPICIOUS_PATHS = Set.of(
        "/admin", "/api/admin", "/api/internal", "/system", "/config"
    );
    
    private static final Set<String> SENSITIVE_OPERATIONS = Set.of(
        "DELETE", "PUT", "PATCH", "admin", "delete", "update", "modify"
    );
    
    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("SecurityHardeningFilter initialized");
        // Start cleanup thread for expired trackers
        startCleanupThread();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Apply security hardening
            if (!applySecurityMeasures(httpRequest, httpResponse)) {
                return; // Security measure blocked the request
            }
            
            // Continue with the request
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Security filter error: {}", e.getMessage(), e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Apply comprehensive security measures
     */
    private boolean applySecurityMeasures(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 1. Rate limiting check
        if (!checkRateLimit(clientIP, path, method)) {
            logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIP, path);
            response.sendError(429, "Rate limit exceeded");
            return false;
        }
        
        // 2. Brute-force protection for login attempts
        if (path.contains("/login") || path.contains("/auth")) {
            if (!checkBruteForceProtection(clientIP, userAgent)) {
                logger.warn("Brute force protection triggered for IP: {}", clientIP);
                response.sendError(429, "Too many login attempts");
                return false;
            }
        }
        
        // 3. Suspicious activity detection
        if (detectSuspiciousActivity(request, clientIP, userAgent)) {
            logger.warn("Suspicious activity detected from IP: {} on path: {}", clientIP, path);
            // Log suspicious activity but don't block immediately
            trackSuspiciousActivity(clientIP, path, userAgent);
        }
        
        // 4. Apply security headers
        applySecurityHeaders(response);
        
        // 5. Validate session security
        if (!validateSessionSecurity(request, response)) {
            return false;
        }
        
        // 6. Check for common attack patterns
        if (detectAttackPatterns(request)) {
            logger.warn("Attack pattern detected from IP: {} on path: {}", clientIP, path);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return false;
        }
        
        // 7. Validate request size
        if (!validateRequestSize(request)) {
            logger.warn("Request size exceeded from IP: {}", clientIP);
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request too large");
            return false;
        }
        
        return true;
    }
    
    /**
     * Rate limiting implementation
     */
    private boolean checkRateLimit(String clientIP, String path, String method) {
        String key = clientIP + ":" + path;
        RequestTracker tracker = requestTrackers.computeIfAbsent(key, k -> new RequestTracker());
        
        return tracker.allowRequest();
    }
    
    /**
     * Brute-force protection for login attempts
     */
    private boolean checkBruteForceProtection(String clientIP, String userAgent) {
        String key = clientIP + ":" + userAgent;
        LoginTracker tracker = loginTrackers.computeIfAbsent(key, k -> new LoginTracker());
        
        return tracker.allowLoginAttempt();
    }
    
    /**
     * Detect suspicious activity patterns
     */
    private boolean detectSuspiciousActivity(HttpServletRequest request, String clientIP, String userAgent) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Check for suspicious paths
        boolean suspiciousPath = SUSPICIOUS_PATHS.stream().anyMatch(path::contains);
        
        // Check for sensitive operations on non-admin users
        boolean sensitiveOp = SENSITIVE_OPERATIONS.stream()
            .anyMatch(op -> path.toLowerCase().contains(op.toLowerCase()) || method.equalsIgnoreCase(op));
        
        // Check for unusual user agents
        boolean suspiciousUA = userAgent == null || userAgent.isEmpty() || 
            userAgent.length() < 10 || userAgent.contains("bot") || userAgent.contains("crawler");
        
        // Check for rapid requests to sensitive areas
        FailedRequestTracker failedTracker = failedRequestTrackers.computeIfAbsent(clientIP, k -> new FailedRequestTracker());
        boolean highFailureRate = failedTracker.getFailureRate() > 0.5; // More than 50% failure rate
        
        return suspiciousPath || (sensitiveOp && suspiciousUA) || highFailureRate;
    }
    
    /**
     * Track suspicious activity
     */
    private void trackSuspiciousActivity(String clientIP, String path, String userAgent) {
        FailedRequestTracker tracker = failedRequestTrackers.computeIfAbsent(clientIP, k -> new FailedRequestTracker());
        tracker.recordSuspiciousActivity(path, userAgent);
        
        // Log to audit system
        logger.info("Suspicious activity logged - IP: {}, Path: {}, UA: {}", clientIP, path, userAgent);
    }
    
    /**
     * Apply security headers
     */
    private void applySecurityHeaders(HttpServletResponse response) {
        // Content Security Policy
        // SECURITY FIX: Remove 'unsafe-inline' and 'unsafe-eval' to prevent XSS attacks
        // Exploit scenario: Attacker injects malicious script via XSS vulnerability
        // Impact: Cross-site scripting attacks can steal cookies, session tokens, or redirect users
        // Remediation: Remove unsafe directives and use nonce/hash-based CSP for legitimate inline scripts
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' https://cdn.jsdelivr.net https://www.gstatic.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'");
        
        // Other security headers
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=(), payment=(), usb=(), interest-cohort=()");
        
        // HSTS (only in production)
        if (isProductionEnvironment()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        }
        
        // Remove server information
        response.setHeader("Server", "");
    }
    
    /**
     * Validate session security
     */
    private boolean validateSessionSecurity(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Check session fixation
            String sessionID = session.getId();
            String lastSessionID = (String) session.getAttribute("lastSessionID");
            
            if (lastSessionID != null && !lastSessionID.equals(sessionID)) {
                // Session ID changed - possible fixation attempt
                logger.warn("Session ID change detected for session: {}", sessionID);
                session.invalidate();
                return false;
            }
            
            // Update last session ID
            session.setAttribute("lastSessionID", sessionID);
            
            // Check session age
            long sessionAge = System.currentTimeMillis() - session.getCreationTime();
            long maxSessionAge = 30 * 60 * 1000; // 30 minutes
            
            if (sessionAge > maxSessionAge) {
                logger.info("Session expired due to age: {}", sessionID);
                session.invalidate();
                return false;
            }
            
            // Check concurrent sessions
            String userID = (String) session.getAttribute("userID");
            if (userID != null && !validateConcurrentSessions(userID, sessionID)) {
                logger.warn("Concurrent session limit exceeded for user: {}", userID);
                session.invalidate();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate concurrent sessions
     */
    private boolean validateConcurrentSessions(String userID, String sessionID) {
        // Implementation would check against a session registry
        // For now, allow up to 3 concurrent sessions per user
        return true; // Simplified for this implementation
    }
    
    /**
     * Detect common attack patterns
     */
    private boolean detectAttackPatterns(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String path = request.getRequestURI();
        
        // Check for SQL injection patterns
        if (queryString != null && containsSQLInjection(queryString)) {
            return true;
        }
        
        // Check for XSS patterns
        if (queryString != null && containsXSS(queryString)) {
            return true;
        }
        
        // Check for path traversal
        if (containsPathTraversal(path)) {
            return true;
        }
        
        // Check for command injection
        if (queryString != null && containsCommandInjection(queryString)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Validate request size
     */
    private boolean validateRequestSize(HttpServletRequest request) {
        int contentLength = request.getContentLength();
        
        // Limit request size to 10MB
        int maxRequestSize = 10 * 1024 * 1024;
        
        return contentLength <= maxRequestSize;
    }
    
    /**
     * SQL injection detection
     */
    private boolean containsSQLInjection(String input) {
        String[] sqlPatterns = {
            "('('|.*'|')|;|\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT|SELECT|UNION|UPDATE)\\b)",
            "(\\b(OR|AND)\\b\\s+\\d+\\s*=\\s*\\d+)",
            "(\\b(OR|AND)\\b\\s+['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+['\"]?)",
            "(--|#|\\/\\*|\\*\\/)",
            "(\\b(SCRIPT|JAVASCRIPT|VBSCRIPT|ONLOAD|ONERROR)\\b)"
        };
        
        String upperInput = input.toUpperCase();
        for (String pattern : sqlPatterns) {
            if (upperInput.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * XSS detection
     */
    private boolean containsXSS(String input) {
        String[] xssPatterns = {
            "(<script[^>]*>.*?</script>)",
            "(javascript:)",
            "(on\\w+\\s*=)",
            "(<iframe[^>]*>)",
            "(<object[^>]*>)",
            "(<embed[^>]*>)",
            "(<link[^>]*>)",
            "(<meta[^>]*>)",
            "(<style[^>]*>.*?</style>)",
            "(<img[^>]*on\\w+[^>]*>)"
        };
        
        for (String pattern : xssPatterns) {
            if (input.toLowerCase().matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Path traversal detection
     */
    private boolean containsPathTraversal(String path) {
        String[] traversalPatterns = {
            "(\\.\\.)",
            "(/|\\\\)etc(/|\\\\)passwd",
            "(/|\\\\)bin(/|\\\\)",
            "(/|\\\\)usr(/|\\\\)",
            "(%2e%2e)",
            "(%2f)",
            "(%5c)"
        };
        
        for (String pattern : traversalPatterns) {
            if (path.toLowerCase().matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Command injection detection
     */
    private boolean containsCommandInjection(String input) {
        String[] commandPatterns = {
            "(;|\\||&)",
            "(\\$\\()",
            "(\\`[^\\`]*\\`)",
            "(\\$\\{[^}]*\\})",
            "(\\b(curl|wget|nc|netcat|telnet|ssh|ftp)\\b)",
            "(\\b(rm|mv|cp|chmod|chown)\\b)"
        };
        
        for (String pattern : commandPatterns) {
            if (input.toLowerCase().matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Check if running in production environment
     */
    private boolean isProductionEnvironment() {
        String env = System.getProperty("spring.profiles.active", "development");
        String envVar = System.getenv("SPRING_PROFILES_ACTIVE");
        if (envVar != null) {
            env = envVar;
        }
        return "production".equals(env);
    }
    
    /**
     * Start cleanup thread for expired trackers
     */
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Clean up every minute
                    cleanupExpiredTrackers();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        cleanupThread.setDaemon(true);
        cleanupThread.setName("SecurityTrackerCleanup");
        cleanupThread.start();
    }
    
    /**
     * Clean up expired trackers
     */
    private void cleanupExpiredTrackers() {
        long now = System.currentTimeMillis();
        
        // Clean up request trackers
        requestTrackers.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(now));
        
        // Clean up login trackers
        loginTrackers.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(now));
        
        // Clean up failed request trackers
        failedRequestTrackers.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(now));
    }
    
    /**
     * Request tracker for rate limiting
     */
    private static class RequestTracker {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private volatile long lastReset = System.currentTimeMillis();
        private static final long RESET_INTERVAL = 60 * 1000; // 1 minute
        
        public boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            if (now - lastReset > RESET_INTERVAL) {
                synchronized (this) {
                    if (now - lastReset > RESET_INTERVAL) {
                        requestCount.set(0);
                        lastReset = now;
                    }
                }
            }
            
            return requestCount.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
        }
        
        public boolean isExpired(long now) {
            return now - lastReset > RESET_INTERVAL * 5; // Expire after 5 minutes
        }
    }
    
    /**
     * Login tracker for brute-force protection
     */
    private static class LoginTracker {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private volatile long lockoutUntil = 0;
        private volatile long lastAttempt = System.currentTimeMillis();
        
        public boolean allowLoginAttempt() {
            long now = System.currentTimeMillis();
            
            if (now < lockoutUntil) {
                return false; // Still locked out
            }
            
            lastAttempt = now;
            int attempts = attemptCount.incrementAndGet();
            
            if (attempts > MAX_LOGIN_ATTEMPTS) {
                lockoutUntil = now + (LOGIN_LOCKOUT_MINUTES * 60 * 1000);
                attemptCount.set(0); // Reset after lockout
                return false;
            }
            
            return true;
        }
        
        public boolean isExpired(long now) {
            return now - lastAttempt > 24 * 60 * 60 * 1000; // Expire after 24 hours
        }
    }
    
    /**
     * Failed request tracker for suspicious activity detection
     */
    private static class FailedRequestTracker {
        private final AtomicInteger totalRequests = new AtomicInteger(0);
        private final AtomicInteger failedRequests = new AtomicInteger(0);
        private volatile long lastActivity = System.currentTimeMillis();
        private final List<String> suspiciousActivities = new ArrayList<>();
        
        public void recordSuspiciousActivity(String path, String userAgent) {
            totalRequests.incrementAndGet();
            failedRequests.incrementAndGet();
            lastActivity = System.currentTimeMillis();
            
            // Keep only recent activities
            synchronized (suspiciousActivities) {
                suspiciousActivities.add(path + "|" + userAgent);
                if (suspiciousActivities.size() > 100) {
                    suspiciousActivities.remove(0);
                }
            }
        }
        
        public double getFailureRate() {
            int total = totalRequests.get();
            int failed = failedRequests.get();
            return total > 0 ? (double) failed / total : 0.0;
        }
        
        public boolean isExpired(long now) {
            return now - lastActivity > 60 * 60 * 1000; // Expire after 1 hour
        }
    }
    
    @Override
    public void destroy() {
        logger.info("SecurityHardeningFilter destroyed");
        // Cleanup resources
        requestTrackers.clear();
        loginTrackers.clear();
        failedRequestTrackers.clear();
    }
}
