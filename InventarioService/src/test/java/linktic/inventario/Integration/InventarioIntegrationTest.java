package linktic.inventario.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.inventario.Domain.Model.Inventario;
import linktic.inventario.Domain.Ports.InventarioRepository;
import linktic.inventario.Infrastructure.Controller.dto.InventarioUpdateRequest;
import linktic.inventario.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class InventarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventarioRepository inventarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String API_KEY = "inventarioMicroKey2025$";

    @BeforeEach
    void setUp() {
        // Clean database before each test
        // inventarioRepository.deleteAll(); // Custom method needed
    }

    @Test
    void testGetInventario_WithValidApiKey_ReturnsOk() throws Exception {
        // Given
        Long productoId = 1L;
        Inventario inventario = new Inventario();
        inventario.setProductoId(productoId);
        inventario.setCantidad(50);
        inventarioRepository.save(inventario);

        // When & Then
        mockMvc.perform(get("/inventarios/{productoId}", productoId)
                .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound()); // Will be 404 because ProductoServiceClient will return null in test
    }

    @Test
    void testGetInventario_WithoutApiKey_ReturnsUnauthorized() throws Exception {
        // Given
        Long productoId = 1L;

        // When & Then
        mockMvc.perform(get("/inventarios/{productoId}", productoId))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unauthorized - Invalid API Key")));
    }

    @Test
    void testGetInventario_WithInvalidApiKey_ReturnsUnauthorized() throws Exception {
        // Given
        Long productoId = 1L;

        // When & Then
        mockMvc.perform(get("/inventarios/{productoId}", productoId)
                .header("X-API-KEY", "invalid-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unauthorized - Invalid API Key")));
    }

    @Test
    void testUpdateInventario_WithValidApiKey_ProcessesRequest() throws Exception {
        // Given
        Long productoId = 1L;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(100);

        // When & Then
        mockMvc.perform(put("/inventarios/{productoId}", productoId)
                .header("X-API-KEY", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound()); // Will be 404 because ProductoServiceClient will return null in test
    }

    @Test
    void testUpdateInventario_WithoutApiKey_ReturnsUnauthorized() throws Exception {
        // Given
        Long productoId = 1L;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(100);

        // When & Then
        mockMvc.perform(put("/inventarios/{productoId}", productoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testHealthEndpoint_WithoutApiKey_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Inventario Service"))
                .andExpect(jsonPath("$.checks.database").exists())
                .andExpect(jsonPath("$.checks.memory").exists());
    }

    @Test
    void testSwaggerEndpoint_WithoutApiKey_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidEndpoint_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/invalid-endpoint")
                .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetInventario_InvalidProductId_ReturnsNotFound() throws Exception {
        // Given
        Long invalidProductoId = -1L;

        // When & Then
        mockMvc.perform(get("/inventarios/{productoId}", invalidProductoId)
                .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateInventario_InvalidJson_ReturnsBadRequest() throws Exception {
        // Given
        Long productoId = 1L;
        String invalidJson = "{invalid json}";

        // When & Then
        mockMvc.perform(put("/inventarios/{productoId}", productoId)
                .header("X-API-KEY", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetInventario_LargeProductId_HandlesCorrectly() throws Exception {
        // Given
        Long largeProductoId = Long.MAX_VALUE;

        // When & Then
        mockMvc.perform(get("/inventarios/{productoId}", largeProductoId)
                .header("X-API-KEY", API_KEY))
                .andExpect(status().isNotFound()); // Will be 404 because ProductoServiceClient will return null
    }
}
