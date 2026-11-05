import java.time.LocalDate;
import java.util.ArrayList;

public class Pedido {
    private int numero;
    private LocalDate fecha;
    private Cliente cliente;
    private ArrayList<LineaPedido> lineasPedido =new ArrayList<>();

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setLineasPedido(ArrayList<LineaPedido> lineasPedido) {
        this.lineasPedido = lineasPedido;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public ArrayList<LineaPedido>getLineasPedido() {
        return lineasPedido;
    }

    public Pedido(){}

    public Pedido( int numero,LocalDate fecha, Cliente cliente, ArrayList<LineaPedido> lineasPedido) {
        this.fecha = fecha;
        this.numero = numero;
        this.cliente = cliente;
        this.lineasPedido = lineasPedido;
    }
    public void addLinea(LineaPedido linea) {
        lineasPedido.add(linea);
    }
    @Override
    public String toString(){
        return "" ;
    }


}
