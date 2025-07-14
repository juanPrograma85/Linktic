package linktic.producto.Infrastruture.controller;

import linktic.producto.Domain.Ports.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final ProductoRepository repository;

    public HealthController(ProductoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Health check requested");

        Map<String, Object> healthStatus = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();

        // Basic application health
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("service", "ProductoService");
        healthStatus.put("version", "1.0.0");

        // Database connectivity check
        try {
            long count = repository.findAll(0, 1).size(); // Simple query to test DB

            Map<String, Object> databaseCheck = new HashMap<>();
            databaseCheck.put("status", "UP");
            databaseCheck.put("details", "Database connection successful");
            checks.put("database", databaseCheck);

            logger.debug("Database health check passed");
        } catch (Exception e) {
            Map<String, Object> databaseCheck = new HashMap<>();
            databaseCheck.put("status", "DOWN");
            databaseCheck.put("details", "Database connection failed: " + e.getMessage());
            checks.put("database", databaseCheck);

            healthStatus.put("status", "DOWN");
            logger.error("Database health check failed: {}", e.getMessage());
        }

        // Memory check
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("max", formatBytes(maxMemory));
        memoryInfo.put("total", formatBytes(totalMemory));
        memoryInfo.put("used", formatBytes(usedMemory));
        memoryInfo.put("free", formatBytes(freeMemory));
        memoryInfo.put("usage_percentage", String.format("%.2f%%", (double) usedMemory / maxMemory * 100));

        Map<String, Object> memoryCheck = new HashMap<>();
        memoryCheck.put("status", "UP");
        memoryCheck.put("details", memoryInfo);
        checks.put("memory", memoryCheck);

        // Final result
        healthStatus.put("checks", checks);
        HttpStatus status = "UP".equals(healthStatus.get("status")) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        logger.info("Health check completed with status: {}", healthStatus.get("status"));
        return new ResponseEntity<>(healthStatus, status);
    }

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        logger.debug("Liveness probe requested");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Application is alive");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        logger.debug("Readiness probe requested");
        Map<String, Object> response = new HashMap<>();

        try {
            // Test database connectivity
            repository.findAll(0, 1);
            response.put("status", "UP");
            response.put("message", "Application is ready to serve traffic");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Application is not ready: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            logger.error("Readiness check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "ProductoService");
        info.put("version", "1.0.0");
        info.put("description", "Microservicio para gestión de productos");
        info.put("apiKeyRequired", true);
        info.put("apiKeyHeader", "X-API-KEY");
        info.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(info);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
