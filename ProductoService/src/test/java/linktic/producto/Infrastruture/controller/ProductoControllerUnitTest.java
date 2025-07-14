package linktic.producto.Infrastruture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import linktic.producto.Domain.Model.Producto;
import linktic.producto.Domain.Model.ProductoNotFoundException;
import linktic.producto.Domain.Services.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerUnitTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

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
    void testCreateProducto_Success() {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto");
        newProducto.setPrecio(new BigDecimal("50.00"));

        when(productoService.createProducto(any(Producto.class))).thenReturn(producto);

        // When
        ResponseEntity<Producto> response = productoController.create(newProducto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Producto Test", response.getBody().getNombre());
        assertEquals(new BigDecimal("99.99"), response.getBody().getPrecio());

        verify(productoService, times(1)).createProducto(newProducto);
    }

    @Test
    void testCreateProducto_ServiceThrowsException() {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto");
        newProducto.setPrecio(new BigDecimal("50.00"));

        when(productoService.createProducto(any(Producto.class)))
                .thenThrow(new IllegalArgumentException("Product name cannot be null or empty"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productoController.create(newProducto);
        });

        verify(productoService, times(1)).createProducto(newProducto);
    }

    @Test
    void testGetProductoById_Success() {
        // Given
        when(productoService.getProductoById(1L)).thenReturn(producto);

        // When
        ResponseEntity<Producto> response = productoController.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Producto Test", response.getBody().getNombre());
        assertEquals(new BigDecimal("99.99"), response.getBody().getPrecio());

        verify(productoService, times(1)).getProductoById(1L);
    }

    @Test
    void testGetProductoById_NotFound() {
        // Given
        when(productoService.getProductoById(999L))
                .thenThrow(new ProductoNotFoundException(999L));

        // When & Then
        assertThrows(ProductoNotFoundException.class, () -> {
            productoController.getById(999L);
        });

        verify(productoService, times(1)).getProductoById(999L);
    }

    @Test
    void testListProductos_Success() {
        // Given
        List<Producto> productos = Arrays.asList(producto, productoUpdated);
        when(productoService.getAllProductos(0, 10)).thenReturn(productos);

        // When
        ResponseEntity<List<Producto>> response = productoController.list(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Producto Test", response.getBody().get(0).getNombre());
        assertEquals("Producto Test Updated", response.getBody().get(1).getNombre());

        verify(productoService, times(1)).getAllProductos(0, 10);
    }

    @Test
    void testListProductos_WithCustomPagination() {
        // Given
        List<Producto> productos = Collections.singletonList(producto);
        when(productoService.getAllProductos(1, 5)).thenReturn(productos);

        // When
        ResponseEntity<List<Producto>> response = productoController.list(1, 5);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(productoService, times(1)).getAllProductos(1, 5);
    }

    @Test
    void testListProductos_EmptyList() {
        // Given
        when(productoService.getAllProductos(0, 10)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<Producto>> response = productoController.list(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(productoService, times(1)).getAllProductos(0, 10);
    }

    @Test
    void testListProductos_DefaultParameters() {
        // Given
        List<Producto> productos = Arrays.asList(producto);
        when(productoService.getAllProductos(0, 10)).thenReturn(productos);

        // When
        ResponseEntity<List<Producto>> response = productoController.list(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoService, times(1)).getAllProductos(0, 10);
    }

    @Test
    void testUpdateProducto_Success() {
        // Given
        when(productoService.updateProducto(eq(1L), any(Producto.class))).thenReturn(productoUpdated);

        // When
        ResponseEntity<Producto> response = productoController.update(1L, productoUpdated);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Producto Test Updated", response.getBody().getNombre());
        assertEquals(new BigDecimal("149.99"), response.getBody().getPrecio());

        verify(productoService, times(1)).updateProducto(eq(1L), any(Producto.class));
    }

    @Test
    void testUpdateProducto_NotFound() {
        // Given
        when(productoService.updateProducto(eq(999L), any(Producto.class)))
                .thenThrow(new ProductoNotFoundException(999L));

        // When & Then
        assertThrows(ProductoNotFoundException.class, () -> {
            productoController.update(999L, productoUpdated);
        });

        verify(productoService, times(1)).updateProducto(eq(999L), any(Producto.class));
    }

    @Test
    void testUpdateProducto_ServiceThrowsException() {
        // Given
        when(productoService.updateProducto(eq(1L), any(Producto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productoController.update(1L, productoUpdated);
        });

        verify(productoService, times(1)).updateProducto(eq(1L), any(Producto.class));
    }

    @Test
    void testDeleteProducto_Success() {
        // Given
        doNothing().when(productoService).deleteProducto(1L);

        // When
        ResponseEntity<Void> response = productoController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(productoService, times(1)).deleteProducto(1L);
    }

    @Test
    void testDeleteProducto_NotFound() {
        // Given
        doThrow(new ProductoNotFoundException(999L)).when(productoService).deleteProducto(999L);

        // When & Then
        assertThrows(ProductoNotFoundException.class, () -> {
            productoController.delete(999L);
        });

        verify(productoService, times(1)).deleteProducto(999L);
    }

    @Test
    void testDeleteProducto_ServiceException() {
        // Given
        doThrow(new RuntimeException("Database error")).when(productoService).deleteProducto(1L);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productoController.delete(1L);
        });

        verify(productoService, times(1)).deleteProducto(1L);
    }

    @Test
    void testControllerConstructor() {
        // Given
        ProductoService mockService = mock(ProductoService.class);

        // When
        ProductoController controller = new ProductoController(mockService);

        // Then
        assertNotNull(controller);
    }

    @Test
    void testGetProductoById_VerifyLogMessage() {
        // Given
        Long productId = 42L;
        when(productoService.getProductoById(productId)).thenReturn(producto);

        // When
        ResponseEntity<Producto> response = productoController.getById(productId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoService, times(1)).getProductoById(productId);
    }

    @Test
    void testCreateProducto_VerifyLogMessage() {
        // Given
        Producto newProducto = new Producto();
        newProducto.setNombre("Log Test Product");
        newProducto.setPrecio(new BigDecimal("25.00"));

        when(productoService.createProducto(newProducto)).thenReturn(producto);

        // When
        ResponseEntity<Producto> response = productoController.create(newProducto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productoService, times(1)).createProducto(newProducto);
    }

    @Test
    void testListProductos_VerifyLogMessage() {
        // Given
        when(productoService.getAllProductos(2, 5)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<Producto>> response = productoController.list(2, 5);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoService, times(1)).getAllProductos(2, 5);
    }

    @Test
    void testUpdateProducto_VerifyLogMessage() {
        // Given
        Long productId = 123L;
        when(productoService.updateProducto(eq(productId), any(Producto.class))).thenReturn(productoUpdated);

        // When
        ResponseEntity<Producto> response = productoController.update(productId, productoUpdated);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoService, times(1)).updateProducto(eq(productId), any(Producto.class));
    }

    @Test
    void testDeleteProducto_VerifyLogMessage() {
        // Given
        Long productId = 456L;
        doNothing().when(productoService).deleteProducto(productId);

        // When
        ResponseEntity<Void> response = productoController.delete(productId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productoService, times(1)).deleteProducto(productId);
    }
}
