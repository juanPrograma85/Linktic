package linktic.inventario.Infrastructure.Controller;

import linktic.inventario.Infrastructure.Client.ProductoServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private ProductoServiceClient productoServiceClient;

    @InjectMocks
    private HealthController healthController;

    @Test
    void testHealth_ReturnsHealthStatus() throws Exception {
        // Mock successful database connection
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        
        // Mock successful product service call
        when(productoServiceClient.existsProducto(anyLong())).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("inventario-service", response.getBody().get("service"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void testHealth_DatabaseCheck() throws Exception {
        // Mock successful database connection
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        
        // Mock product service call
        when(productoServiceClient.existsProducto(anyLong())).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertNotNull(response.getBody());
        Map<String, Object> database = (Map<String, Object>) response.getBody().get("database");
        assertNotNull(database);
        assertEquals("UP", database.get("status"));
        assertEquals("H2 Database", database.get("type"));
    }

    @Test
    void testHealth_ExternalServicesCheck() throws Exception {
        // Mock database connection
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        
        // Mock successful product service call
        when(productoServiceClient.existsProducto(anyLong())).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertNotNull(response.getBody());
        Map<String, Object> externalServices = (Map<String, Object>) response.getBody().get("externalServices");
        assertNotNull(externalServices);
        Map<String, Object> productoService = (Map<String, Object>) externalServices.get("productoService");
        assertNotNull(productoService);
        assertEquals("UP", productoService.get("status"));
    }

    @Test
    void testHealth_DatabaseFailure() throws Exception {
        // Mock failed database connection
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));
        
        // Mock product service call
        when(productoServiceClient.existsProducto(anyLong())).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertNotNull(response.getBody());
        assertEquals("DOWN", response.getBody().get("status"));
        Map<String, Object> database = (Map<String, Object>) response.getBody().get("database");
        assertEquals("DOWN", database.get("status"));
    }
}
