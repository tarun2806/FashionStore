package com.fashionstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

public class RequestTracingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestTracingFilter.class);
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String START_TIME_KEY = "requestStartTime";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestTracingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        MDC.put(START_TIME_KEY, String.valueOf(startTime));

        String correlationId = (String) httpRequest.getAttribute(CORRELATION_ID_KEY);
        String requestId = (String) httpRequest.getAttribute(REQUEST_ID_KEY);
        String userId = (String) httpRequest.getAttribute("userId");
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String userAgent = httpRequest.getHeader("User-Agent");
        String remoteAddr = httpRequest.getRemoteAddr();

        try {
            logger.info("Request received - correlationId={}, requestId={}, userId={}, method={}, uri={}, remoteAddr={}, userAgent={}",
                    correlationId, requestId, userId, method, uri, remoteAddr, userAgent);

            chain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            int status = ((jakarta.servlet.http.HttpServletResponse) response).getStatus();

            logger.info("Request completed - correlationId={}, requestId={}, userId={}, method={}, uri={}, status={}, duration={}ms",
                    correlationId, requestId, userId, method, uri, status, duration);

            if (status >= 400) {
                logger.warn("Request failed - correlationId={}, requestId={}, userId={}, method={}, uri={}, status={}, duration={}ms",
                        correlationId, requestId, userId, method, uri, status, duration);
            }

            if (status >= 500) {
                logger.error("Server error - correlationId={}, requestId={}, userId={}, method={}, uri={}, status={}, duration={}ms",
                        correlationId, requestId, userId, method, uri, status, duration);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Request exception - correlationId={}, requestId={}, userId={}, method={}, uri={}, duration={}ms, exception={}",
                    correlationId, requestId, userId, method, uri, duration, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(START_TIME_KEY);
        }
    }

    @Override
    public void destroy() {
        logger.info("RequestTracingFilter destroyed");
    }
}
