public class LineaPedido {
    private String articulo;
    private int unidades;
    private double precioUnitario;

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getArticulo() {
        return articulo;
    }

    public int getUnidades() {
        return unidades;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }


    public LineaPedido() {
    }

    public LineaPedido(String articulo, int unidades, double precioUnitario) {
        this.articulo = articulo;
        this.unidades = unidades;
        this.precioUnitario = precioUnitario;
    }

    @Override
    public String toString(){
        return "" ;
    }
}
