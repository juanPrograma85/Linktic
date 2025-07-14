package linktic.producto.Infrastruture.repository;

import linktic.producto.Domain.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductoRepository extends JpaRepository<Producto, Long> {



}
