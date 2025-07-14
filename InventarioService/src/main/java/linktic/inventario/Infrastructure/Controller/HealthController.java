package linktic.inventario.Infrastructure.Controller;

import linktic.inventario.Infrastructure.Client.ProductoServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProductoServiceClient productoServiceClient;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("Verificando estado de salud del servicio");
        
        Map<String, Object> health = new HashMap<>();
        health.put("service", "inventario-service");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        
        // Check database connectivity
        Map<String, Object> database = checkDatabase();
        health.put("database", database);
        
        // Check external service connectivity
        Map<String, Object> externalServices = checkExternalServices();
        health.put("externalServices", externalServices);
        
        // Determine overall status
        boolean isHealthy = "UP".equals(database.get("status")) && 
                           "UP".equals(((Map<String, Object>) externalServices.get("productoService")).get("status"));
        
        health.put("status", isHealthy ? "UP" : "DOWN");
        
        logger.info("Estado de salud verificado: {}", isHealthy ? "SALUDABLE" : "CON PROBLEMAS");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "inventario-service");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                dbHealth.put("status", isValid ? "UP" : "DOWN");
                dbHealth.put("type", "H2 Database");
                if (isValid) {
                    dbHealth.put("details", "Database connection successful");
                } else {
                    dbHealth.put("details", "Database connection failed validation");
                }
            }
        } catch (Exception e) {
            logger.error("Error checking database health: {}", e.getMessage());
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        return dbHealth;
    }

    private Map<String, Object> checkExternalServices() {
        Map<String, Object> services = new HashMap<>();
        
        // Check producto service
        Map<String, Object> productoServiceHealth = new HashMap<>();
        try {
            // Try to validate a dummy product to test connectivity
            boolean canConnect = productoServiceClient.existsProducto(999L); // Dummy ID
            productoServiceHealth.put("status", "UP");
            productoServiceHealth.put("details", "Producto service is reachable");
        } catch (Exception e) {
            logger.warn("Producto service health check failed: {}", e.getMessage());
            productoServiceHealth.put("status", "DOWN");
            productoServiceHealth.put("error", e.getMessage());
        }
        
        services.put("productoService", productoServiceHealth);
        return services;
    }
}
