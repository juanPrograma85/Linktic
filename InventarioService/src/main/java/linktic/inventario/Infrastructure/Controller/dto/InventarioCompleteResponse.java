package linktic.inventario.Infrastructure.Controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InventarioCompleteResponse {
    
    @JsonProperty("inventario")
    private InventarioInfo inventario;
    
    @JsonProperty("producto")
    private ProductoDto producto;

    public InventarioCompleteResponse() {}

    public InventarioCompleteResponse(InventarioInfo inventario, ProductoDto producto) {
        this.inventario = inventario;
        this.producto = producto;
    }

    public InventarioInfo getInventario() {
        return inventario;
    }

    public void setInventario(InventarioInfo inventario) {
        this.inventario = inventario;
    }

    public ProductoDto getProducto() {
        return producto;
    }

    public void setProducto(ProductoDto producto) {
        this.producto = producto;
    }

    public static class InventarioInfo {
        @JsonProperty("productoId")
        private Long productoId;
        
        @JsonProperty("cantidad")
        private Integer cantidad;

        public InventarioInfo() {}

        public InventarioInfo(Long productoId, Integer cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        @Override
        public String toString() {
            return "InventarioInfo{" +
                    "productoId=" + productoId +
                    ", cantidad=" + cantidad +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "InventarioCompleteResponse{" +
                "inventario=" + inventario +
                ", producto=" + producto +
                '}';
    }
}
