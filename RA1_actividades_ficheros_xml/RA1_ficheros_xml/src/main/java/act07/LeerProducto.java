package act07;

import java.io.*;

public class LeerProducto {
    public static void main(String[] args) {
        try (ObjectInputStream ois = new ObjectInputStream(
                 new FileInputStream("datos/producto.dat"))) {

            Producto p = (Producto) ois.readObject();
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Precio: " + p.getPrecio());

        } catch (FileNotFoundException e) {
            System.out.println("No existe datos/producto.dat. Ejecuta antes EscribirProducto.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al leer: " + e);
        }
    }
}
