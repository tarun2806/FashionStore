package com.fashionstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    @Mock
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPublicPathAccess() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/home");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testProtectedPathWithoutSession() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/profile");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse).sendRedirect(eq("/FashionStore/login"));
        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testProtectedPathWithValidSession() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/profile");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpResponse, never()).sendRedirect(anyString());
    }

    @Test
    void testAdminPathWithoutAdminRole() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/admin/dashboard");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("customer");

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse).sendRedirect(eq("/FashionStore/home"));
        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testAdminPathWithAdminRole() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/admin/dashboard");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn("admin");

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpResponse, never()).sendRedirect(anyString());
    }

    @Test
    void testLoginPathAllowed() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/login");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testRegisterPathAllowed() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/register");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testStaticAssetsAllowed() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/assets/css/style.css");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testApiAdminPathWithoutAuth() throws ServletException, IOException {
        when(httpRequest.getRequestURI()).thenReturn("/FashionStore/api/admin/dashboard");
        when(httpRequest.getContextPath()).thenReturn("/FashionStore");
        when(httpRequest.getSession(false)).thenReturn(null);

        AuthFilter authFilter = new AuthFilter();
        authFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "authFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        authFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED));
        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
    }
}
