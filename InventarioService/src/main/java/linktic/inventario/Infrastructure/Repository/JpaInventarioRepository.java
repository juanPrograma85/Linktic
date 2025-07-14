package linktic.inventario.Infrastructure.Repository;

import linktic.inventario.Domain.Model.Inventario;
import linktic.inventario.Domain.Ports.InventarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaInventarioRepository extends JpaRepository<Inventario, Long>, InventarioRepository {
    @Override
    Optional<Inventario> findByProductoId(Long productoId);
}
