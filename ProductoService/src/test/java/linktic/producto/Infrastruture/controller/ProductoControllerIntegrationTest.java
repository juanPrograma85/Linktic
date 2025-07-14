package linktic.producto.Infrastruture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.producto.Domain.Model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductoControllerIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "test-api-key-12345";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void testCreateAndGetProducto_IntegrationFlow() throws Exception {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Integration Test Product");
        newProducto.setPrecio(new BigDecimal("75.50"));

        // When - Create product
        String response = mockMvc.perform(post("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Integration Test Product"))
                .andExpect(jsonPath("$.precio").value(75.50))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        Producto createdProducto = objectMapper.readValue(response, Producto.class);
        Long productId = createdProducto.getId();

        // Then - Get the created product
        mockMvc.perform(get("/api/productos/" + productId)
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.nombre").value("Integration Test Product"))
                .andExpect(jsonPath("$.precio").value(75.50));
    }

    @Test
    @WithMockUser
    void testCreateUpdateDeleteProducto_FullFlow() throws Exception {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Full Flow Test Product");
        newProducto.setPrecio(new BigDecimal("100.00"));

        // Step 1: Create product
        String createResponse = mockMvc.perform(post("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Producto createdProducto = objectMapper.readValue(createResponse, Producto.class);
        Long productId = createdProducto.getId();

        // Step 2: Update product
        Producto updatedProducto = new Producto();
        updatedProducto.setNombre("Updated Full Flow Test Product");
        updatedProducto.setPrecio(new BigDecimal("150.00"));

        mockMvc.perform(put("/api/productos/" + productId)
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProducto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.nombre").value("Updated Full Flow Test Product"))
                .andExpect(jsonPath("$.precio").value(150.00));

        // Step 3: Verify update with GET
        mockMvc.perform(get("/api/productos/" + productId)
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Updated Full Flow Test Product"))
                .andExpect(jsonPath("$.precio").value(150.00));

        // Step 4: Delete product
        mockMvc.perform(delete("/api/productos/" + productId)
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNoContent());

        // Step 5: Verify deletion
        mockMvc.perform(get("/api/productos/" + productId)
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testListProductos_WithMultipleProducts() throws Exception {
        // Given - Create multiple products
        for (int i = 1; i <= 3; i++) {
            Producto producto = new Producto();
            producto.setNombre("Test Product " + i);
            producto.setPrecio(new BigDecimal(String.valueOf(10.00 * i)));

            mockMvc.perform(post("/api/productos")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(producto)))
                    .andExpect(status().isCreated());
        }

        // When & Then - List products
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser
    void testListProductos_WithPagination() throws Exception {
        // Given - Create multiple products
        for (int i = 1; i <= 5; i++) {
            Producto producto = new Producto();
            producto.setNombre("Pagination Test Product " + i);
            producto.setPrecio(new BigDecimal(String.valueOf(20.00 * i)));

            mockMvc.perform(post("/api/productos")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(producto)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Test pagination
        mockMvc.perform(get("/api/productos")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/productos")
                .param("page", "1")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void testCreateProducto_WithInvalidData() throws Exception {
        // Test with null name
        Producto invalidProducto = new Producto();
        invalidProducto.setNombre(null);
        invalidProducto.setPrecio(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/productos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProducto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateProducto_NotFound() throws Exception {
        // Given
        Producto updateProducto = new Producto();
        updateProducto.setNombre("Non-existent Product");
        updateProducto.setPrecio(new BigDecimal("99.99"));

        // When & Then
        mockMvc.perform(put("/api/productos/99999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProducto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteProducto_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/productos/99999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
