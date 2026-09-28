package act04;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GestorFicheroTexto {
    public static void main(String[] args) {
        try {
            // La carpeta datos NO se crea sola
            Files.createDirectories(Path.of("datos"));

            // Escritura (Charset explicito + try-with-resources)
            try (BufferedWriter bw = new BufferedWriter(
                     new FileWriter("datos/registro.txt", StandardCharsets.UTF_8))) {
                bw.write("Registro 1: revisión anual");
                bw.newLine();
                // TODO: escribe aqui los registros 2 y 3
            }
            System.out.println("Archivo escrito con éxito.");

            // Lectura (el MISMO Charset)
            try (BufferedReader br = new BufferedReader(
                     new FileReader("datos/registro.txt", StandardCharsets.UTF_8))) {
                String linea;
                System.out.println("Contenido del archivo:");
                while ((linea = br.readLine()) != null) {
                    System.out.println("> " + linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e);
        }
    }
}
