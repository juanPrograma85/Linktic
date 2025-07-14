package linktic.inventario.Infrastructure.Controller;

import linktic.inventario.Domain.Model.Inventario;
import linktic.inventario.Domain.Ports.InventarioRepository;
import linktic.inventario.Infrastructure.Client.ProductoServiceClient;
import linktic.inventario.Infrastructure.Controller.dto.InventarioCompleteResponse;
import linktic.inventario.Infrastructure.Controller.dto.InventarioUpdateRequest;
import linktic.inventario.Infrastructure.Controller.dto.ProductoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/inventarios")
public class InventarioController {

    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);
    
    private final InventarioRepository inventarioRepository;
    private final ProductoServiceClient productoClient;

    public InventarioController(InventarioRepository inventarioRepository,
                                ProductoServiceClient productoClient) {
        this.inventarioRepository = inventarioRepository;
        this.productoClient = productoClient;
    }

    // GET cantidad disponible
    @GetMapping("/{productoId}")
    public ResponseEntity<?> getCantidad(@PathVariable Long productoId) {
        logger.info("Solicitando cantidad de inventario para producto ID: {}", productoId);
        
        // Get product information from external service
        ProductoDto producto = productoClient.getProducto(productoId);
        if (producto == null) {
            logger.warn("Producto no encontrado: ID {}", productoId);
            return ResponseEntity.status(404).body("{\"error\": \"Producto no encontrado\"}");
        }

        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        
        InventarioCompleteResponse.InventarioInfo inventarioInfo;
        if (!inventarioOpt.isPresent()) {
            logger.info("No existe inventario para producto ID: {}, devolviendo cantidad por defecto 0", productoId);
            inventarioInfo = new InventarioCompleteResponse.InventarioInfo(productoId, 0);
        } else {
            Inventario inventario = inventarioOpt.get();
            logger.info("Inventario encontrado para producto ID: {} - Cantidad: {}", productoId, inventario.getCantidad());
            inventarioInfo = new InventarioCompleteResponse.InventarioInfo(productoId, inventario.getCantidad());
        }

        InventarioCompleteResponse response = new InventarioCompleteResponse(inventarioInfo, producto);
        return ResponseEntity.ok(response);
    }

    // PUT actualizar cantidad
    @PutMapping("/{productoId}")
    public ResponseEntity<?> actualizarCantidad(@PathVariable Long productoId, @RequestBody InventarioUpdateRequest updateRequest) {
        logger.info("Actualizando inventario para producto ID: {} - Nueva cantidad: {}", productoId, updateRequest.getCantidad());
        
        // Get product information from external service
        ProductoDto producto = productoClient.getProducto(productoId);
        if (producto == null) {
            logger.warn("Producto no encontrado al actualizar inventario: ID {}", productoId);
            return ResponseEntity.status(404).body("{\"error\": \"Producto no encontrado\"}");
        }

        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseGet(() -> {
                    logger.info("Creando nuevo inventario para producto ID: {}", productoId);
                    Inventario nuevo = new Inventario();
                    nuevo.setProductoId(productoId);
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        Integer cantidadAnterior = inventario.getCantidad();
        inventario.setCantidad(updateRequest.getCantidad());
        inventarioRepository.save(inventario);

        logger.info("[Evento] Inventario actualizado para producto {} - Cantidad anterior: {} - Nueva cantidad: {}", 
                   productoId, cantidadAnterior, updateRequest.getCantidad());

        InventarioCompleteResponse.InventarioInfo inventarioInfo = new InventarioCompleteResponse.InventarioInfo(productoId, inventario.getCantidad());
        InventarioCompleteResponse response = new InventarioCompleteResponse(inventarioInfo, producto);
        
        return ResponseEntity.ok(response);
    }
}