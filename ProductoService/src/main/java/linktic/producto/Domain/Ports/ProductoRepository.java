package linktic.producto.Domain.Ports;

import linktic.producto.Domain.Model.Producto;

import java.util.List;
import java.util.Optional;
public interface ProductoRepository {

    Producto save(Producto producto);
    Optional<Producto> findById(Long id);
    List<Producto> findAll(int page, int size);
    void deleteById(Long id);
}
