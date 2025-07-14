package linktic.inventario.Infrastructure.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.inventario.Infrastructure.Controller.dto.ProductoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductoServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceClient.class);

    @Value("${producto-service.url}")
    private String productoServiceUrl;

    @Value("${producto-service.api-key}")
    private String apiKey;
    
    @Value("${producto-service.validate-exists:true}")
    private boolean validateExists;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean existsProducto(Long id) {
        // Skip validation if disabled (useful for development/testing)
        if (!validateExists) {
            logger.debug("Validación de producto deshabilitada para ID: {}", id);
            return true;
        }
        
        logger.debug("Validando existencia del producto ID: {} en {}", id, productoServiceUrl);
        
        try {
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-API-KEY", apiKey);
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            String url = productoServiceUrl + "/api/productos/" + id;
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            boolean exists = response.getStatusCode().is2xxSuccessful();
            logger.debug("Producto ID: {} {} en el servicio externo", id, exists ? "encontrado" : "no encontrado");
            return exists;
        } catch (Exception e) {
            logger.error("Error al validar producto ID: {} - {}", id, e.getMessage());
            return false;
        }
    }

    public ProductoDto getProducto(Long id) {
        logger.debug("Obteniendo información completa del producto ID: {}", id);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", apiKey);
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            String url = productoServiceUrl + "/api/productos/" + id;
            
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                ProductoDto producto = objectMapper.readValue(response.getBody(), ProductoDto.class);
                logger.debug("Producto obtenido: {}", producto);
                return producto;
            } else {
                logger.warn("No se pudo obtener el producto ID: {} - Status: {}", id, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al obtener producto ID: {} - {}", id, e.getMessage());
            return null;
        }
    }
}