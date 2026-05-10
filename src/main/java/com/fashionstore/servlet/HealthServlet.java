package com.fashionstore.servlet;

import com.fashionstore.daoimpl.UserDAOImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthServlet.class);
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject healthStatus = new JsonObject();
        healthStatus.addProperty("status", "UP");
        healthStatus.addProperty("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        healthStatus.addProperty("service", "FashionStore Backend");

        JsonObject checks = new JsonObject();

        checks.addProperty("database", checkDatabaseHealth());
        checks.addProperty("memory", checkMemoryHealth());
        checks.addProperty("disk", checkDiskHealth());

        healthStatus.add("checks", checks);

        boolean allHealthy = checks.entrySet().stream()
                .allMatch(entry -> "UP".equals(entry.getValue().getAsString()));

        if (!allHealthy) {
            healthStatus.addProperty("status", "DEGRADED");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        response.getWriter().write(gson.toJson(healthStatus));
    }

    private String checkDatabaseHealth() {
        try {
            UserDAOImpl userDAO = new UserDAOImpl();
            int userCount = userDAO.getTotalUserCount();
            if (userCount >= 0) {
                logger.debug("Database health check passed - userCount={}", userCount);
                return "UP";
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
        }
        return "DOWN";
    }

    private String checkMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsage = (double) usedMemory / maxMemory;

        if (memoryUsage < 0.8) {
            logger.debug("Memory health check passed - usage: {}%", String.format("%.2f", memoryUsage * 100));
            return "UP";
        } else if (memoryUsage < 0.9) {
            logger.warn("Memory health check degraded - usage: {}%", String.format("%.2f", memoryUsage * 100));
            return "DEGRADED";
        } else {
            logger.error("Memory health check critical - usage: {}%", String.format("%.2f", memoryUsage * 100));
            return "DOWN";
        }
    }

    private String checkDiskHealth() {
        try {
            Path path = Path.of("/");
            FileStore store = Files.getFileStore(path);
            long totalSpace = store.getTotalSpace();
            long freeSpace = store.getUsableSpace();
            double diskUsage = 1.0 - ((double) freeSpace / totalSpace);

            if (diskUsage < 0.8) {
                logger.debug("Disk health check passed - usage: {}%", String.format("%.2f", diskUsage * 100));
                return "UP";
            } else if (diskUsage < 0.9) {
                logger.warn("Disk health check degraded - usage: {}%", String.format("%.2f", diskUsage * 100));
                return "DEGRADED";
            } else {
                logger.error("Disk health check critical - usage: {}%", String.format("%.2f", diskUsage * 100));
                return "DOWN";
            }
        } catch (Exception e) {
            logger.error("Disk health check failed", e);
            return "DOWN";
        }
    }
}
