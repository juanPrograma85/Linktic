package linktic.inventario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class InventarioServiceApp {

    private static final Logger logger = LoggerFactory.getLogger(InventarioServiceApp.class);

    public static void main(String[] args) {
        logger.info("Iniciando Inventario Service...");
        SpringApplication.run(InventarioServiceApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("=================================");
        logger.info("  INVENTARIO SERVICE INICIADO");
        logger.info("  Puerto: 8081");
        logger.info("  Swagger UI: http://localhost:8081/swagger-ui/index.html");
        logger.info("  Health Check: http://localhost:8081/health");
        logger.info("=================================");
    }
}