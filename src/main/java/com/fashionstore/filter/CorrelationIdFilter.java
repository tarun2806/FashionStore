package com.fashionstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

public class CorrelationIdFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CorrelationIdFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }

        String requestId = generateRequestId();

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        httpRequest.setAttribute(CORRELATION_ID_MDC_KEY, correlationId);
        httpRequest.setAttribute(REQUEST_ID_MDC_KEY, requestId);

        try {
            logger.debug("Request started - correlationId={}, requestId={}, method={}, uri={}",
                    correlationId, requestId, httpRequest.getMethod(), httpRequest.getRequestURI());

            chain.doFilter(request, response);

            logger.debug("Request completed - correlationId={}, requestId={}, method={}, uri={}",
                    correlationId, requestId, httpRequest.getMethod(), httpRequest.getRequestURI());
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    @Override
    public void destroy() {
        logger.info("CorrelationIdFilter destroyed");
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
