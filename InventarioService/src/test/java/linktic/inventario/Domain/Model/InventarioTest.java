package linktic.inventario.Domain.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
    }

    @Test
    void testSetAndGetProductoId() {
        // Given
        Long expectedProductoId = 1L;

        // When
        inventario.setProductoId(expectedProductoId);

        // Then
        assertEquals(expectedProductoId, inventario.getProductoId());
    }

    @Test
    void testSetAndGetCantidad() {
        // Given
        Integer expectedCantidad = 100;

        // When
        inventario.setCantidad(expectedCantidad);

        // Then
        assertEquals(expectedCantidad, inventario.getCantidad());
    }

    @Test
    void testInventarioCreation() {
        // Given & When
        Inventario newInventario = new Inventario();

        // Then
        assertNull(newInventario.getProductoId());
        assertNull(newInventario.getCantidad());
    }

    @Test
    void testInventarioWithZeroCantidad() {
        // Given
        Long productoId = 1L;
        Integer cantidad = 0;

        // When
        inventario.setProductoId(productoId);
        inventario.setCantidad(cantidad);

        // Then
        assertEquals(productoId, inventario.getProductoId());
        assertEquals(cantidad, inventario.getCantidad());
    }

    @Test
    void testInventarioWithNegativeCantidad() {
        // Given
        Long productoId = 1L;
        Integer cantidad = -10;

        // When
        inventario.setProductoId(productoId);
        inventario.setCantidad(cantidad);

        // Then
        assertEquals(productoId, inventario.getProductoId());
        assertEquals(cantidad, inventario.getCantidad());
    }

    @Test
    void testInventarioWithLargeCantidad() {
        // Given
        Long productoId = 999L;
        Integer cantidad = Integer.MAX_VALUE;

        // When
        inventario.setProductoId(productoId);
        inventario.setCantidad(cantidad);

        // Then
        assertEquals(productoId, inventario.getProductoId());
        assertEquals(cantidad, inventario.getCantidad());
    }
}
