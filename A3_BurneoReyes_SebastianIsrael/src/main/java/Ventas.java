import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ventas ventas = (Ventas) o;

        // Formateamos ambas fechas para compararlas SIN los nanosegundos
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String miFecha = this.fecha.format(formato);
        String otraFecha = ventas.fecha.format(formato);

        return numero == ventas.numero &&
                unidades == ventas.unidades &&
                Double.compare(ventas.precio, precio) == 0 &&
                miFecha.equals(otraFecha) &&  // Comparamos las fechas formateadas
                cliente.equals(ventas.cliente) &&
                producto.equals(ventas.producto);
    }
}
