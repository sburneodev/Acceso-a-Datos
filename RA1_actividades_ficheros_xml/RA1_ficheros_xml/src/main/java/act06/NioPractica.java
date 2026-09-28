package act06;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SeekableByteChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.List;

public class NioPractica {
    public static void main(String[] args) {
        try {
            // Parte 1: Crear archivo y escribir líneas
            Path archivo = Path.of("datos", "nio_prueba.txt");   // Path.of desde Java 11
            Files.createDirectories(archivo.getParent());
            List<String> lineas = List.of("Primera línea", "Segunda línea", "Tercera línea");
            Files.write(archivo, lineas, StandardCharsets.UTF_8);

            // Parte 2: Leer y mostrar el contenido
            List<String> contenido = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            System.out.println("Contenido leído:");
            for (String linea : contenido) {
                System.out.println("> " + linea);
            }

            // Parte 3: Escribir con canal y búfer
            Path archivoCanal = Path.of("datos", "nio_canal.txt");
            String mensaje = "NIO con canal";

            try (SeekableByteChannel canal = Files.newByteChannel(
                    archivoCanal,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {   // si no, deja basura al reejecutar

                ByteBuffer buffer = ByteBuffer.wrap(mensaje.getBytes(StandardCharsets.UTF_8));
                canal.write(buffer);
                System.out.println("Escrito en archivo con canal.");
            }

        } catch (NoSuchFileException e) {
            System.out.println("No se encuentra el fichero: " + e.getFile());
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e);
        }
    }
}
