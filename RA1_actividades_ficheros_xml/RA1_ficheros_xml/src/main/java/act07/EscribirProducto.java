package act07;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class EscribirProducto {
    public static void main(String[] args) {
        Producto p = new Producto("Teclado", 29.99);

        try {
            Files.createDirectories(Path.of("datos"));   // la carpeta no se crea sola

            try (ObjectOutputStream oos = new ObjectOutputStream(
                     new FileOutputStream("datos/producto.dat"))) {
                oos.writeObject(p);
                System.out.println("Producto serializado.");
            }
        } catch (IOException e) {
            System.out.println("Error al escribir: " + e);
        }
    }
}
