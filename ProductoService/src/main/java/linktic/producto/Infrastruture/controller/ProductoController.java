package linktic.producto.Infrastruture.controller;

import linktic.producto.Domain.Model.Producto;
import linktic.producto.Domain.Services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<Producto> create(@RequestBody Producto producto) {
        logger.info("Creating new product: {}", producto.getNombre());
        Producto savedProducto = productoService.createProducto(producto);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getById(@PathVariable Long id) {
        logger.info("Fetching product with ID: {}", id);
        Producto producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching products list - page: {}, size: {}", page, size);
        List<Producto> productos = productoService.getAllProductos(page, size);
        return ResponseEntity.ok(productos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> update(@PathVariable Long id, @RequestBody Producto producto) {
        logger.info("Updating product with ID: {}", id);
        Producto updatedProducto = productoService.updateProducto(id, producto);
        return ResponseEntity.ok(updatedProducto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("Deleting product with ID: {}", id);
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }
}
