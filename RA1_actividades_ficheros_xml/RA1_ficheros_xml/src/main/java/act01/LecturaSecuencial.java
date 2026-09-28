package act01;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LecturaSecuencial {
    public static void main(String[] args) {
        File archivo = new File("datos/datos.txt");

        try (BufferedReader br = new BufferedReader(
                 new FileReader(archivo, StandardCharsets.UTF_8))) {

            String linea;
            System.out.println("Lectura completa del archivo (modo secuencial):");
            while ((linea = br.readLine()) != null) {
                System.out.println("> " + linea);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No se encuentra datos.txt en " + System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e);
        }
    }
}
