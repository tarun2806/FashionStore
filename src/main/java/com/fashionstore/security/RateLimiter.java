package com.fashionstore.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    
    private static final Map<String, RateLimitEntry> rateLimits = new ConcurrentHashMap<>();
    private static final Map<String, FailedLoginEntry> failedLogins = new ConcurrentHashMap<>();
    private static final long CLEANUP_INTERVAL = 60000; // 1 minute
    private static volatile long lastCleanup = System.currentTimeMillis();
    
    // QA Test Mode - enabled via environment variable
    private static final boolean QA_MODE = Boolean.parseBoolean(System.getenv().getOrDefault("QA_MODE", "false"));
    
    // Rate limits per endpoint
    private static final int LOGIN_ATTEMPTS_PER_MINUTE = QA_MODE ? 1000 : 5;
    private static final int REGISTRATION_ATTEMPTS_PER_MINUTE = QA_MODE ? 1000 : 3;
    private static final int GENERAL_REQUESTS_PER_MINUTE = QA_MODE ? 10000 : 100;
    private static final int PASSWORD_RESET_ATTEMPTS_PER_MINUTE = QA_MODE ? 1000 : 3;
    
    // Account lockout settings
    private static final int MAX_FAILED_ATTEMPTS = QA_MODE ? 1000 : 5;
    private static final long LOCKOUT_DURATION = QA_MODE ? 0 : 15 * 60 * 1000; // Disabled in QA mode
    
    private static class RateLimitEntry {
        final AtomicInteger count;
        final long windowStart;
        
        RateLimitEntry() {
            this.count = new AtomicInteger(1);
            this.windowStart = System.currentTimeMillis();
        }
        
        boolean isExpired(long windowMs) {
            return System.currentTimeMillis() - windowStart > windowMs;
        }
    }
    
    private static class FailedLoginEntry {
        final AtomicInteger failedAttempts;
        final long firstFailureTime;
        volatile long lockoutUntil;
        
        FailedLoginEntry() {
            this.failedAttempts = new AtomicInteger(1);
            this.firstFailureTime = System.currentTimeMillis();
            this.lockoutUntil = 0;
        }
        
        boolean isLockedOut() {
            return System.currentTimeMillis() < lockoutUntil;
        }
        
        void recordFailure() {
            int attempts = failedAttempts.incrementAndGet();
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION;
                logger.warn("Account locked out due to {} failed attempts", attempts);
            }
        }
        
        void resetOnSuccessfulLogin() {
            failedAttempts.set(0);
            lockoutUntil = 0;
        }
    }
    
    public static boolean checkRateLimit(HttpServletRequest request, String endpoint) {
        String key = getClientKey(request, endpoint);
        int maxRequests = getMaxRequests(endpoint);
        long windowMs = 60000; // 1 minute window
        
        cleanupExpiredEntries();
        
        RateLimitEntry entry = rateLimits.get(key);
        if (entry == null || entry.isExpired(windowMs)) {
            rateLimits.put(key, new RateLimitEntry());
            return true;
        }
        
        int currentCount = entry.count.incrementAndGet();
        if (currentCount > maxRequests) {
            logger.warn("Rate limit exceeded for key: {}, endpoint: {}, count: {}", key, endpoint, currentCount);
            return false;
        }
        
        return true;
    }
    
    private static String getClientKey(HttpServletRequest request, String endpoint) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return endpoint + ":" + ip + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }
    
    private static int getMaxRequests(String endpoint) {
        if (endpoint.contains("/login")) return LOGIN_ATTEMPTS_PER_MINUTE;
        if (endpoint.contains("/register")) return REGISTRATION_ATTEMPTS_PER_MINUTE;
        if (endpoint.contains("/forgot-password")) return PASSWORD_RESET_ATTEMPTS_PER_MINUTE;
        return GENERAL_REQUESTS_PER_MINUTE;
    }
    
    private static void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_INTERVAL) {
            return;
        }
        
        lastCleanup = now;
        rateLimits.entrySet().removeIf(entry -> entry.getValue().isExpired(60000));
        logger.debug("Rate limiter cleanup completed, active entries: {}", rateLimits.size());
    }
    
    public static boolean resetRateLimit(HttpServletRequest request, String endpoint) {
        String key = getClientKey(request, endpoint);
        rateLimits.remove(key);
        return true;
    }
    
    /**
     * Check if an account is locked out due to failed login attempts
     */
    public static boolean isAccountLockedOut(String email) {
        if (email == null) return false;
        
        FailedLoginEntry entry = failedLogins.get(email.toLowerCase());
        return entry != null && entry.isLockedOut();
    }
    
    /**
     * Record a failed login attempt
     */
    public static void recordFailedLogin(String email) {
        if (email == null) return;
        
        String key = email.toLowerCase();
        FailedLoginEntry entry = failedLogins.get(key);
        if (entry == null) {
            entry = new FailedLoginEntry();
            failedLogins.put(key, entry);
        } else {
            entry.recordFailure();
        }
        
        logger.warn("Failed login attempt recorded for: {}, attempts: {}", email, entry.failedAttempts.get());
    }
    
    /**
     * Reset failed login attempts on successful login
     */
    public static void resetFailedLogins(String email) {
        if (email == null) return;
        
        FailedLoginEntry entry = failedLogins.remove(email.toLowerCase());
        if (entry != null) {
            entry.resetOnSuccessfulLogin();
        }
    }
}
