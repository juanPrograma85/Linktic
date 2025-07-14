package linktic.inventario.Infrastructure.Controller;

import linktic.inventario.Domain.Model.Inventario;
import linktic.inventario.Domain.Ports.InventarioRepository;
import linktic.inventario.Infrastructure.Client.ProductoServiceClient;
import linktic.inventario.Infrastructure.Controller.dto.InventarioCompleteResponse;
import linktic.inventario.Infrastructure.Controller.dto.InventarioUpdateRequest;
import linktic.inventario.Infrastructure.Controller.dto.ProductoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoServiceClient productoServiceClient;

    @InjectMocks
    private InventarioController inventarioController;

    private ProductoDto testProducto;
    private Inventario testInventario;

    @BeforeEach
    void setUp() {
        testProducto = new ProductoDto(1L, "Test Product", 100.00);
        testInventario = new Inventario();
        testInventario.setProductoId(1L);
        testInventario.setCantidad(50);
    }

    @Test
    void testGetCantidad_ProductExistsWithInventory_ReturnsCompleteResponse() {
        // Given
        Long productoId = 1L;
        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(testInventario));

        // When
        ResponseEntity<?> response = inventarioController.getCantidad(productoId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(InventarioCompleteResponse.class, response.getBody());
        
        InventarioCompleteResponse completeResponse = (InventarioCompleteResponse) response.getBody();
        assertEquals(productoId, completeResponse.getInventario().getProductoId());
        assertEquals(50, completeResponse.getInventario().getCantidad());
        assertEquals(testProducto.getId(), completeResponse.getProducto().getId());
        assertEquals(testProducto.getNombre(), completeResponse.getProducto().getNombre());
        assertEquals(testProducto.getPrecio(), completeResponse.getProducto().getPrecio());

        verify(productoServiceClient).getProducto(productoId);
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void testGetCantidad_ProductExistsWithoutInventory_ReturnsDefaultCantidad() {
        // Given
        Long productoId = 1L;
        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = inventarioController.getCantidad(productoId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(InventarioCompleteResponse.class, response.getBody());
        
        InventarioCompleteResponse completeResponse = (InventarioCompleteResponse) response.getBody();
        assertEquals(productoId, completeResponse.getInventario().getProductoId());
        assertEquals(0, completeResponse.getInventario().getCantidad()); // Default cantidad
        assertEquals(testProducto.getId(), completeResponse.getProducto().getId());

        verify(productoServiceClient).getProducto(productoId);
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void testGetCantidad_ProductNotExists_Returns404() {
        // Given
        Long productoId = 999L;
        when(productoServiceClient.getProducto(productoId)).thenReturn(null);

        // When
        ResponseEntity<?> response = inventarioController.getCantidad(productoId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("{\"error\": \"Producto no encontrado\"}", response.getBody());

        verify(productoServiceClient).getProducto(productoId);
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void testActualizarCantidad_ProductExistsWithExistingInventory_UpdatesInventory() {
        // Given
        Long productoId = 1L;
        Integer newCantidad = 75;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(newCantidad);

        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(testInventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(testInventario);

        // When
        ResponseEntity<?> response = inventarioController.actualizarCantidad(productoId, updateRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(InventarioCompleteResponse.class, response.getBody());
        
        InventarioCompleteResponse completeResponse = (InventarioCompleteResponse) response.getBody();
        assertEquals(productoId, completeResponse.getInventario().getProductoId());
        assertEquals(newCantidad, completeResponse.getInventario().getCantidad());

        verify(productoServiceClient).getProducto(productoId);
        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository).save(any(Inventario.class));
        
        // Verify that the cantidad was updated
        assertEquals(newCantidad, testInventario.getCantidad());
    }

    @Test
    void testActualizarCantidad_ProductExistsWithoutInventory_CreatesNewInventory() {
        // Given
        Long productoId = 2L;
        Integer newCantidad = 100;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(newCantidad);

        Inventario newInventario = new Inventario();
        newInventario.setProductoId(productoId);
        newInventario.setCantidad(newCantidad);

        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(newInventario);

        // When
        ResponseEntity<?> response = inventarioController.actualizarCantidad(productoId, updateRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(InventarioCompleteResponse.class, response.getBody());
        
        InventarioCompleteResponse completeResponse = (InventarioCompleteResponse) response.getBody();
        assertEquals(productoId, completeResponse.getInventario().getProductoId());
        assertEquals(newCantidad, completeResponse.getInventario().getCantidad());

        verify(productoServiceClient).getProducto(productoId);
        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void testActualizarCantidad_ProductNotExists_Returns404() {
        // Given
        Long productoId = 999L;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(50);

        when(productoServiceClient.getProducto(productoId)).thenReturn(null);

        // When
        ResponseEntity<?> response = inventarioController.actualizarCantidad(productoId, updateRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("{\"error\": \"Producto no encontrado\"}", response.getBody());

        verify(productoServiceClient).getProducto(productoId);
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void testActualizarCantidad_ZeroCantidad_UpdatesSuccessfully() {
        // Given
        Long productoId = 1L;
        Integer newCantidad = 0;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(newCantidad);

        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(testInventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(testInventario);

        // When
        ResponseEntity<?> response = inventarioController.actualizarCantidad(productoId, updateRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newCantidad, testInventario.getCantidad());

        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void testActualizarCantidad_NegativeCantidad_UpdatesSuccessfully() {
        // Given
        Long productoId = 1L;
        Integer newCantidad = -10;
        InventarioUpdateRequest updateRequest = new InventarioUpdateRequest();
        updateRequest.setCantidad(newCantidad);

        when(productoServiceClient.getProducto(productoId)).thenReturn(testProducto);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(testInventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(testInventario);

        // When
        ResponseEntity<?> response = inventarioController.actualizarCantidad(productoId, updateRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newCantidad, testInventario.getCantidad());

        verify(inventarioRepository).save(any(Inventario.class));
    }
}
