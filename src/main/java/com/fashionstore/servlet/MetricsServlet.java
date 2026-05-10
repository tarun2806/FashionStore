package com.fashionstore.servlet;

import com.fashionstore.metrics.MetricsRegistry;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; version=0.0.4");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(MetricsRegistry.getInstance().scrape());
    }
}
