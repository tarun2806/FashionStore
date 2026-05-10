package com.fashionstore.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.util.concurrent.TimeUnit;

public class MetricsRegistry {

    private static MetricsRegistry instance;
    private final MeterRegistry registry;

    private MetricsRegistry() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    public static synchronized MetricsRegistry getInstance() {
        if (instance == null) {
            instance = new MetricsRegistry();
        }
        return instance;
    }

    public MeterRegistry getRegistry() {
        return registry;
    }

    public Counter counter(String name, String... tags) {
        return Counter.builder(name).tags(tags).register(registry);
    }

    public Timer timer(String name, String... tags) {
        return Timer.builder(name).tags(tags).register(registry);
    }

    public Gauge gauge(String name, Runnable gauge, String... tags) {
        return Gauge.builder(name, gauge, g -> 1).tags(tags).register(registry);
    }

    public void recordLoginAttempt(boolean success) {
        counter("login.attempts", "success", String.valueOf(success)).increment();
    }

    public void recordCheckout(String status) {
        counter("checkout.attempts", "status", status).increment();
    }

    public void recordPayment(String method, String status) {
        counter("payment.attempts", "method", method, "status", status).increment();
    }

    public void recordApiCall(String endpoint, String method, int status) {
        counter("api.calls", "endpoint", endpoint, "method", method, "status", String.valueOf(status)).increment();
    }

    public void recordDatabaseQuery(String operation, String table, long duration) {
        counter("database.queries", "operation", operation, "table", table).increment();
        timer("database.query.duration", "operation", operation, "table", table).record(duration, TimeUnit.MILLISECONDS);
    }

    public void recordCacheOperation(String operation, boolean hit) {
        counter("cache.operations", "operation", operation, "hit", String.valueOf(hit)).increment();
    }

    public void recordInventoryChange(String productId, int change) {
        counter("inventory.changes", "product", productId).increment();
    }

    public Timer startRequestTimer(String endpoint, String method) {
        return timer("http.request.duration", "endpoint", endpoint, "method", method);
    }

    public String scrape() {
        if (registry instanceof PrometheusMeterRegistry) {
            return ((PrometheusMeterRegistry) registry).scrape();
        }
        return "";
    }
}
