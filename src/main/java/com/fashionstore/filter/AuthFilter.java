package com.fashionstore.filter;

import com.fashionstore.model.User;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Restricts authenticated-only areas while keeping catalog, auth, password reset,
 * search, payment callbacks, and static assets reachable without a session.
 */
public class AuthFilter implements Filter {

    /** Paths that are public without trailing-slash normalization issues (exact match after normalize). */
    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/",
            "/home",
            "/products",
            "/product",
            "/login",
            "/register",
            "/logout",
            "/404",
            "/error",
            "/forgot-password",
            "/reset-password",
            "/search",
            "/success",
            "/payment",
            "/index.jsp",
            "/admin/register"
    );

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/assets/",
            "/api/admin/login",
            "/api/admin/register"
    );

    /** Admin JSON API: handles its own auth + role check via AdminApiController. */
    private static final String ADMIN_API_PREFIX = "/api/admin/";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getDispatcherType() == DispatcherType.ERROR) {
            chain.doFilter(request, response);
            return;
        }

        String contextPath = req.getContextPath();
        String relativePath = normalizePath(req.getRequestURI().substring(contextPath.length()));

        if (isPublicPath(relativePath)) {
            if (shouldRedirectAuthenticatedAwayFromAuthForms(req, relativePath)) {
                resp.sendRedirect(contextPath + "/home");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        boolean isLoggedIn = (user != null);

        if (!isLoggedIn) {
            boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"Please login to continue.\",\"redirect\":\""
                        + contextPath + "/login\"}");
            } else {
                resp.sendRedirect(contextPath + "/login");
            }
            return;
        }

        boolean isAdminPath = "/admin".equals(relativePath) || relativePath.startsWith("/admin/");
        if (isAdminPath && !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin only.");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isPublicPath(String relativePath) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (relativePath.startsWith(prefix)) {
                return true;
            }
        }
        if (relativePath.startsWith(ADMIN_API_PREFIX)) {
            return true; // AdminApiController enforces its own auth + role check
        }
        return PUBLIC_EXACT_PATHS.contains(relativePath)
                || relativePath.startsWith("/search/");
    }

    /**
     * Covers {@code /search/suggestions} and any future {@code /search/...} endpoints.
     */
    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        int semi = path.indexOf(';');
        if (semi >= 0) {
            path = path.substring(0, semi);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean shouldRedirectAuthenticatedAwayFromAuthForms(HttpServletRequest req, String relativePath) {
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            return false;
        }
        if (!"/login".equals(relativePath) && !"/register".equals(relativePath)) {
            return false;
        }
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        User user = (User) session.getAttribute("user");
        return user != null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
