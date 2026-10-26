import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public class Ventas implements Serializable {
    private static final long serialVersionUID = 1L;
    int numero;
    LocalDateTime fecha;
    String cliente;
    String producto;
    int unidades;
    double precio;
    public Ventas(int numero, LocalDateTime fecha, String cliente, String producto, int unidades, double precio) {
        this.numero = numero;
        this.fecha = fecha;
        this.cliente = cliente;
        this.producto = producto;
        this.unidades = unidades;
        this.precio = precio;
    }

}
