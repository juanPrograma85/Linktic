package linktic.inventario.Infrastructure.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.inventario.Infrastructure.Controller.dto.ProductoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductoServiceClient productoServiceClient;

    private final String TEST_URL = "http://localhost:8080";
    private final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productoServiceClient, "productoServiceUrl", TEST_URL);
        ReflectionTestUtils.setField(productoServiceClient, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(productoServiceClient, "validateExists", true);
        ReflectionTestUtils.setField(productoServiceClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(productoServiceClient, "objectMapper", objectMapper);
    }

    @Test
    void testExistsProducto_ProductExists_ReturnsTrue() {
        // Given
        Long productoId = 1L;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // When
        boolean result = productoServiceClient.existsProducto(productoId);

        // Then
        assertTrue(result);
        verify(restTemplate).exchange(
                eq(TEST_URL + "/api/productos/" + productoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void testExistsProducto_ProductNotExists_ReturnsFalse() {
        // Given
        Long productoId = 999L;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // When
        boolean result = productoServiceClient.existsProducto(productoId);

        // Then
        assertFalse(result);
    }

    @Test
    void testExistsProducto_ServiceThrowsException_ReturnsFalse() {
        // Given
        Long productoId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection error"));

        // When
        boolean result = productoServiceClient.existsProducto(productoId);

        // Then
        assertFalse(result);
    }

    @Test
    void testExistsProducto_ValidationDisabled_ReturnsTrue() {
        // Given
        Long productoId = 1L;
        ReflectionTestUtils.setField(productoServiceClient, "validateExists", false);

        // When
        boolean result = productoServiceClient.existsProducto(productoId);

        // Then
        assertTrue(result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testGetProducto_ProductExists_ReturnsProductDto() throws Exception {
        // Given
        Long productoId = 1L;
        String responseBody = "{\"id\":1,\"nombre\":\"Test Product\",\"precio\":100.0}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        ProductoDto expectedProducto = new ProductoDto(1L, "Test Product", 100.0);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
        when(objectMapper.readValue(responseBody, ProductoDto.class))
                .thenReturn(expectedProducto);

        // When
        ProductoDto result = productoServiceClient.getProducto(productoId);

        // Then
        assertNotNull(result);
        assertEquals(expectedProducto.getId(), result.getId());
        assertEquals(expectedProducto.getNombre(), result.getNombre());
        assertEquals(expectedProducto.getPrecio(), result.getPrecio());

        verify(restTemplate).exchange(
                eq(TEST_URL + "/api/productos/" + productoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper).readValue(responseBody, ProductoDto.class);
    }

    @Test
    void testGetProducto_ProductNotExists_ReturnsNull() {
        // Given
        Long productoId = 999L;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // When
        ProductoDto result = productoServiceClient.getProducto(productoId);

        // Then
        assertNull(result);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void testGetProducto_ServiceThrowsException_ReturnsNull() {
        // Given
        Long productoId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection error"));

        // When
        ProductoDto result = productoServiceClient.getProducto(productoId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetProducto_JsonParsingError_ReturnsNull() throws Exception {
        // Given
        Long productoId = 1L;
        String responseBody = "invalid json";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
        when(objectMapper.readValue(responseBody, ProductoDto.class))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // When
        ProductoDto result = productoServiceClient.getProducto(productoId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetProducto_ServerError_ReturnsNull() {
        // Given
        Long productoId = 1L;
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // When
        ProductoDto result = productoServiceClient.getProducto(productoId);

        // Then
        assertNull(result);
        verifyNoInteractions(objectMapper);
    }
}
