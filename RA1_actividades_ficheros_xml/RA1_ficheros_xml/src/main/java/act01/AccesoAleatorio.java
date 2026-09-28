package act01;

import java.io.*;

public class AccesoAleatorio {
    public static void main(String[] args) {
        // Cambia el valor para probar: 0, 10, 11, 15, 30...
        long posicion = 15;

        try (RandomAccessFile raf = new RandomAccessFile("datos/datos.txt", "r")) {
            System.out.println("Tamaño del fichero: " + raf.length() + " bytes");

            raf.seek(posicion); // Mover el puntero al byte indicado

            System.out.println("Lectura desde el byte " + posicion + ":");
            String linea = raf.readLine();
            System.out.println("> " + linea);

        } catch (IOException e) {
            System.out.println("Error en acceso aleatorio: " + e);
        }
    }
}
