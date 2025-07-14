package linktic.producto.Infrastruture.repository;

import linktic.producto.Domain.Model.Producto;
import linktic.producto.Domain.Ports.ProductoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaProductoRepository implements ProductoRepository {
    private final SpringDataProductoRepository jpaRepo;

    public JpaProductoRepository(SpringDataProductoRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Producto save(Producto producto) {
        return jpaRepo.save(producto);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Producto> findAll(int page, int size) {
        // Validate parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        return jpaRepo.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}
