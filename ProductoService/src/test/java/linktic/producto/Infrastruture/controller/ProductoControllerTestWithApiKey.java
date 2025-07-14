package linktic.producto.Infrastruture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.producto.Domain.Model.Producto;
import linktic.producto.Domain.Model.ProductoNotFoundException;
import linktic.producto.Domain.Services.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductoController.class)
@ActiveProfiles("test")
class ProductoControllerTestWithApiKey {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "test-api-key-12345";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto;
    private Producto productoUpdated;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("99.99"));

        productoUpdated = new Producto();
        productoUpdated.setId(1L);
        productoUpdated.setNombre("Producto Test Updated");
        productoUpdated.setPrecio(new BigDecimal("149.99"));
    }

    @Test
    void testCreateProducto_Success() throws Exception {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto");
        newProducto.setPrecio(new BigDecimal("50.00"));

        when(productoService.createProducto(any(Producto.class))).thenReturn(producto);

        // When & Then
        mockMvc.perform(post("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Producto Test"))
                .andExpect(jsonPath("$.precio").value(99.99));

        verify(productoService, times(1)).createProducto(any(Producto.class));
    }

    @Test
    void testCreateProducto_ServiceThrowsException() throws Exception {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto");
        newProducto.setPrecio(new BigDecimal("50.00"));

        when(productoService.createProducto(any(Producto.class)))
                .thenThrow(new IllegalArgumentException("Product name cannot be null or empty"));

        // When & Then
        mockMvc.perform(post("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isInternalServerError());

        verify(productoService, times(1)).createProducto(any(Producto.class));
    }

    @Test
    void testCreateProducto_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).createProducto(any(Producto.class));
    }

    @Test
    void testGetProductoById_Success() throws Exception {
        // Given
        when(productoService.getProductoById(1L)).thenReturn(producto);

        // When & Then
        mockMvc.perform(get("/api/productos/1")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Producto Test"))
                .andExpect(jsonPath("$.precio").value(99.99));

        verify(productoService, times(1)).getProductoById(1L);
    }

    @Test
    void testGetProductoById_NotFound() throws Exception {
        // Given
        when(productoService.getProductoById(999L))
                .thenThrow(new ProductoNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/productos/999")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).getProductoById(999L);
    }

    @Test
    void testGetProductoById_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos/invalid")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).getProductoById(any(Long.class));
    }

    @Test
    void testListProductos_Success() throws Exception {
        // Given
        List<Producto> productos = Arrays.asList(producto, productoUpdated);
        when(productoService.getAllProductos(0, 10)).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Producto Test"))
                .andExpect(jsonPath("$[1].nombre").value("Producto Test Updated"));

        verify(productoService, times(1)).getAllProductos(0, 10);
    }

    @Test
    void testListProductos_WithCustomPagination() throws Exception {
        // Given
        List<Producto> productos = Collections.singletonList(producto);
        when(productoService.getAllProductos(1, 5)).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productoService, times(1)).getAllProductos(1, 5);
    }

    @Test
    void testListProductos_EmptyList() throws Exception {
        // Given
        when(productoService.getAllProductos(0, 10)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productoService, times(1)).getAllProductos(0, 10);
    }

    @Test
    void testListProductos_InvalidPaginationParams() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .param("page", "invalid")
                .param("size", "invalid"))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).getAllProductos(anyInt(), anyInt());
    }

    @Test
    void testUpdateProducto_Success() throws Exception {
        // Given
        when(productoService.updateProducto(eq(1L), any(Producto.class))).thenReturn(productoUpdated);

        // When & Then
        mockMvc.perform(put("/api/productos/1")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoUpdated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Producto Test Updated"))
                .andExpect(jsonPath("$.precio").value(149.99));

        verify(productoService, times(1)).updateProducto(eq(1L), any(Producto.class));
    }

    @Test
    void testUpdateProducto_NotFound() throws Exception {
        // Given
        when(productoService.updateProducto(eq(999L), any(Producto.class)))
                .thenThrow(new ProductoNotFoundException(999L));

        // When & Then
        mockMvc.perform(put("/api/productos/999")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoUpdated)))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).updateProducto(eq(999L), any(Producto.class));
    }

    @Test
    void testUpdateProducto_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/productos/invalid")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoUpdated)))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).updateProducto(any(Long.class), any(Producto.class));
    }

    @Test
    void testUpdateProducto_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/productos/1")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).updateProducto(any(Long.class), any(Producto.class));
    }

    @Test
    void testDeleteProducto_Success() throws Exception {
        // Given
        doNothing().when(productoService).deleteProducto(1L);

        // When & Then
        mockMvc.perform(delete("/api/productos/1")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).deleteProducto(1L);
    }

    @Test
    void testDeleteProducto_NotFound() throws Exception {
        // Given
        doThrow(new ProductoNotFoundException(999L)).when(productoService).deleteProducto(999L);

        // When & Then
        mockMvc.perform(delete("/api/productos/999")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).deleteProducto(999L);
    }

    @Test
    void testDeleteProducto_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/productos/invalid")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).deleteProducto(any(Long.class));
    }

    @Test
    void testDeleteProducto_ServiceException() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error")).when(productoService).deleteProducto(1L);

        // When & Then
        mockMvc.perform(delete("/api/productos/1")
                .header(API_KEY_HEADER, API_KEY_VALUE))
                .andExpect(status().isInternalServerError());

        verify(productoService, times(1)).deleteProducto(1L);
    }

    // Security tests - without API key
    @Test
    void testCreateProducto_Unauthorized() throws Exception {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto");
        newProducto.setPrecio(new BigDecimal("50.00"));

        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).createProducto(any(Producto.class));
    }

    @Test
    void testGetProductoById_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).getProductoById(any(Long.class));
    }

    @Test
    void testListProductos_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).getAllProductos(anyInt(), anyInt());
    }

    @Test
    void testUpdateProducto_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoUpdated)))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).updateProducto(any(Long.class), any(Producto.class));
    }

    @Test
    void testDeleteProducto_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).deleteProducto(any(Long.class));
    }

    @Test
    void testInvalidApiKey() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/productos/1")
                .header(API_KEY_HEADER, "invalid-api-key"))
                .andExpect(status().isUnauthorized());

        verify(productoService, never()).getProductoById(any(Long.class));
    }
}
