package linktic.inventario.Domain.Ports;

import linktic.inventario.Domain.Model.Inventario;

import java.util.Optional;

public interface InventarioRepository {
    Optional<Inventario> findByProductoId(Long productoId);
    Inventario save(Inventario inventario);
}
