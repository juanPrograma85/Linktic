package linktic.producto.Infrastruture.controller;

import linktic.producto.Domain.Model.ProductoNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleProductoNotFoundException() {
        // Given
        Long productId = 999L;
        ProductoNotFoundException exception = new ProductoNotFoundException(productId);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleProductoNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Producto no encontrado con id: " + productId, body.get("message"));
        assertEquals("/api/productos", body.get("path"));
        
        // Verify timestamp is recent (within last minute)
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");
        assertTrue(timestamp.isAfter(LocalDateTime.now().minusMinutes(1)));
        assertTrue(timestamp.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
        assertEquals("/api/productos", body.get("path"));
        
        // Verify timestamp is recent (within last minute)
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");
        assertTrue(timestamp.isAfter(LocalDateTime.now().minusMinutes(1)));
        assertTrue(timestamp.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void testHandleGenericException_NullPointerException() {
        // Given
        Exception exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("An unexpected error occurred", body.get("message"));
    }

    @Test
    void testHandleGenericException_IllegalArgumentException() {
        // Given
        Exception exception = new IllegalArgumentException("Invalid argument provided");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("An unexpected error occurred", body.get("message"));
    }
}
