package linktic.inventario.Infrastructure.Controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InventarioUpdateRequest {
    
    @JsonProperty("cantidad")
    private Integer cantidad;

    public InventarioUpdateRequest() {}

    public InventarioUpdateRequest(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "InventarioUpdateRequest{" +
                "cantidad=" + cantidad +
                '}';
    }
}
