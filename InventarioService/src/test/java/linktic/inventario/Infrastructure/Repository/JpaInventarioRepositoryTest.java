package linktic.inventario.Infrastructure.Repository;

import linktic.inventario.Domain.Model.Inventario;
import linktic.inventario.Domain.Ports.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaInventarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventarioRepository inventarioRepository;

    private Inventario testInventario;

    @BeforeEach
    void setUp() {
        testInventario = new Inventario();
        testInventario.setProductoId(1L);
        testInventario.setCantidad(100);
    }

    @Test
    void testSave_NewInventario_SavesSuccessfully() {
        // When
        Inventario savedInventario = inventarioRepository.save(testInventario);

        // Then
        assertNotNull(savedInventario);
        assertEquals(testInventario.getProductoId(), savedInventario.getProductoId());
        assertEquals(testInventario.getCantidad(), savedInventario.getCantidad());
        
        // Verify it was actually saved to the database
        entityManager.flush();
        Inventario foundInventario = entityManager.find(Inventario.class, savedInventario.getProductoId());
        assertNotNull(foundInventario);
        assertEquals(testInventario.getCantidad(), foundInventario.getCantidad());
    }

    @Test
    void testFindByProductoId_ExistingInventario_ReturnsInventario() {
        // Given
        entityManager.persistAndFlush(testInventario);

        // When
        Optional<Inventario> result = inventarioRepository.findByProductoId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testInventario.getProductoId(), result.get().getProductoId());
        assertEquals(testInventario.getCantidad(), result.get().getCantidad());
    }

    @Test
    void testFindByProductoId_NonExistingInventario_ReturnsEmpty() {
        // When
        Optional<Inventario> result = inventarioRepository.findByProductoId(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdate_ExistingInventario_UpdatesSuccessfully() {
        // Given
        Inventario savedInventario = entityManager.persistAndFlush(testInventario);
        Integer newCantidad = 200;

        // When
        savedInventario.setCantidad(newCantidad);
        Inventario updatedInventario = inventarioRepository.save(savedInventario);

        // Then
        assertEquals(newCantidad, updatedInventario.getCantidad());
        
        // Verify the update persisted
        entityManager.flush();
        entityManager.clear(); // Clear the persistence context
        Inventario foundInventario = entityManager.find(Inventario.class, savedInventario.getProductoId());
        assertEquals(newCantidad, foundInventario.getCantidad());
    }

    @Test
    void testSave_ZeroCantidad_SavesSuccessfully() {
        // Given
        testInventario.setCantidad(0);

        // When
        Inventario savedInventario = inventarioRepository.save(testInventario);

        // Then
        assertNotNull(savedInventario);
        assertEquals(0, savedInventario.getCantidad());
    }

    @Test
    void testSave_NegativeCantidad_SavesSuccessfully() {
        // Given
        testInventario.setCantidad(-50);

        // When
        Inventario savedInventario = inventarioRepository.save(testInventario);

        // Then
        assertNotNull(savedInventario);
        assertEquals(-50, savedInventario.getCantidad());
    }

    @Test
    void testSave_MultipleInventarios_SavesAll() {
        // Given
        Inventario inventario1 = new Inventario();
        inventario1.setProductoId(1L);
        inventario1.setCantidad(100);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(200);

        // When
        inventarioRepository.save(inventario1);
        inventarioRepository.save(inventario2);
        entityManager.flush();

        // Then
        Optional<Inventario> found1 = inventarioRepository.findByProductoId(1L);
        Optional<Inventario> found2 = inventarioRepository.findByProductoId(2L);
        
        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(100, found1.get().getCantidad());
        assertEquals(200, found2.get().getCantidad());
    }

    @Test
    void testFindByProductoId_LargeProductoId_HandlesCorrectly() {
        // Given
        Long largeProductoId = Long.MAX_VALUE;
        testInventario.setProductoId(largeProductoId);
        entityManager.persistAndFlush(testInventario);

        // When
        Optional<Inventario> result = inventarioRepository.findByProductoId(largeProductoId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(largeProductoId, result.get().getProductoId());
    }
}
