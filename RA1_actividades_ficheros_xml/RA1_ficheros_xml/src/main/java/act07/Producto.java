package act07;

import java.io.Serializable;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private double precio;

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }
}
