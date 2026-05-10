package com.fashionstore.filter;

import com.fashionstore.security.CSRFProtection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CSRFFilterTest {

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
    void testGetRequestAddsToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("GET");
        when(httpRequest.getSession(true)).thenReturn(session);

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(session).setAttribute(eq("csrfToken"), anyString());
        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testPostRequestWithValidToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("valid-token-123");
        when(httpRequest.getParameter("csrfToken")).thenReturn("valid-token-123");

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testPostRequestWithInvalidToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("valid-token-123");
        when(httpRequest.getParameter("csrfToken")).thenReturn("invalid-token-456");

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
        verify(httpResponse).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testPostRequestWithoutToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("valid-token-123");
        when(httpRequest.getParameter("csrfToken")).thenReturn(null);

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
        verify(httpResponse).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testPutRequestWithValidToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("PUT");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("valid-token-123");
        when(httpRequest.getParameter("csrfToken")).thenReturn("valid-token-123");

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testDeleteRequestWithValidToken() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("DELETE");
        when(httpRequest.getSession(false)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("valid-token-123");
        when(httpRequest.getParameter("csrfToken")).thenReturn("valid-token-123");

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
    }

    @Test
    void testGetRequestWithoutValidation() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("GET");
        when(httpRequest.getSession(true)).thenReturn(session);

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(session).setAttribute(eq("csrfToken"), anyString());
        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpRequest, never()).getParameter("csrfToken");
    }

    @Test
    void testHeadRequestWithoutValidation() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("HEAD");
        when(httpRequest.getSession(true)).thenReturn(session);

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpRequest, never()).getParameter("csrfToken");
    }

    @Test
    void testOptionsRequestWithoutValidation() throws ServletException, IOException {
        when(httpRequest.getMethod()).thenReturn("OPTIONS");
        when(httpRequest.getSession(true)).thenReturn(session);

        CSRFFilter csrfFilter = new CSRFFilter();
        csrfFilter.init(new FilterConfig() {
            @Override
            public String getFilterName() { return "csrfFilter"; }
            @Override
            public ServletContext getServletContext() { return mock(ServletContext.class); }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return null; }
        });

        csrfFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(filterChain).doFilter(httpRequest, httpResponse);
        verify(httpRequest, never()).getParameter("csrfToken");
    }
}
