package linktic.inventario.Infrastructure.Controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductoDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("precio")
    private Double precio;

    public ProductoDto() {}

    public ProductoDto(Long id, String nombre, Double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "ProductoDto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                '}';
    }
}
