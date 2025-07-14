package linktic.producto.Domain.Services;

import linktic.producto.Domain.Model.Producto;
import linktic.producto.Domain.Model.ProductoNotFoundException;
import linktic.producto.Domain.Ports.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Producto createProducto(Producto producto) {
        logger.info("Creating product: {}", producto.getNombre());
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        
        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        
        try {
            Producto savedProducto = repository.save(producto);
            logger.info("Product created successfully with ID: {}", savedProducto.getId());
            return savedProducto;
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage());
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Producto getProductoById(Long id) {
        logger.info("Fetching product with ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        try {
            Optional<Producto> producto = repository.findById(id);
            if (producto.isPresent()) {
                logger.info("Product found: {}", producto.get().getNombre());
                return producto.get();
            } else {
                logger.warn("Product not found with ID: {}", id);
                throw new ProductoNotFoundException(id);
            }
        } catch (ProductoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching product with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch product", e);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public List<Producto> getAllProductos(int page, int size) {
        logger.info("Fetching products list - page: {}, size: {}", page, size);
        
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        try {
            List<Producto> productos = repository.findAll(page, size);
            logger.info("Retrieved {} products", productos.size());
            return productos;
        } catch (Exception e) {
            logger.error("Error fetching products list: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch products", e);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Producto updateProducto(Long id, Producto producto) {
        logger.info("Updating product with ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        
        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        
        try {
            // Verify product exists
            repository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
            
            producto.setId(id);
            Producto updatedProducto = repository.save(producto);
            logger.info("Product updated successfully: {}", updatedProducto.getNombre());
            return updatedProducto;
        } catch (ProductoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating product with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void deleteProducto(Long id) {
        logger.info("Deleting product with ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        try {
            // Verify product exists
            repository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
            
            repository.deleteById(id);
            logger.info("Product deleted successfully with ID: {}", id);
        } catch (ProductoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete product", e);
        }
    }
}
