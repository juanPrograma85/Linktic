package linktic.producto.Domain.Model;

public class ProductoNotFoundException extends RuntimeException {
    public ProductoNotFoundException(Long id) {
        super("Producto no encontrado con id: " + id);
    }
}
