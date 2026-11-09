package incidencias;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ejercicio 1: lee el CSV con NIO, resume las incidencias y guarda el resumen en un TXT (UTF-8).
 *
 * <p>Reglas del examen: rutas relativas, UTF-8 explícito, todo por {@code System.out.println}
 * (también los ERROR) y sin {@code System.exit}: el programa acaba volviendo del main.</p>
 */
public class Ejercicio1 {                                         // ADAPTAR: nombre exacto de la clase con main

    static final Path CSV = Path.of("datos", "incidencias.csv");
    static final Path CARPETA_SALIDA = Path.of("salida");
    static final Path RESUMEN = CARPETA_SALIDA.resolve("resumen.txt");   // ADAPTAR: nombre del TXT

    public static void main(String[] args) {
        try {
            // 1) Comprobar que existe y su tamaño en bytes
            if (!Files.exists(CSV)) {
                System.out.println("ERROR: no encuentra el archivo " + CSV);   // ADAPTAR: texto exacto
                return;                                                       // sin System.exit
            }
            System.out.println("Tamaño de " + CSV + ": " + Files.size(CSV) + " bytes"); // ADAPTAR: texto exacto

            // 2) Crear la carpeta de salida (no falla si ya existe)
            Files.createDirectories(CARPETA_SALIDA);

            // 3) Leer con el lector
            LectorIncidencias lector = new LectorIncidencias();
            List<IncidenciaPojo> incidencias = lector.leer(CSV);
            List<Integer> rotas = lector.getLineasRotas();

            // 4) Contar: válidas, abiertas, cerradas y por aula (TreeMap = orden alfabético)
            int abiertas = 0;
            int cerradas = 0;
            Map<String, Integer> porAula = new TreeMap<>();
            for (IncidenciaPojo inc : incidencias) {
                if (inc.isAbierta()) abiertas++;
                else if (inc.isCerrada()) cerradas++;
                porAula.merge(inc.getAula(), 1, Integer::sum);   // suma 1 al contador de esa aula
            }

            // 5) Montar el resumen (ADAPTAR: textos carácter a carácter con el enunciado)
            List<String> resumen = new ArrayList<>();
            resumen.add("Incidencias válidas: " + incidencias.size());
            resumen.add("Abiertas: " + abiertas);
            resumen.add("Cerradas: " + cerradas);
            resumen.add("Por aula:");
            for (Map.Entry<String, Integer> e : porAula.entrySet()) {
                resumen.add("  " + e.getKey() + ": " + e.getValue());          // dos espacios delante
            }
            resumen.add("Líneas rotas: " + rotas.size());
            for (int num : rotas) {
                resumen.add("  " + num);                                     // en el orden del fichero
            }

            // 6) Guardar en UTF-8 (crea o machaca) y mostrar por consola
            Files.write(RESUMEN, resumen, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            resumen.forEach(System.out::println);

            // 7) Comprobar que el resumen existe y su tamaño (ADAPTAR: quitar si no lo piden)
            if (Files.exists(RESUMEN)) {
                System.out.println("Resumen guardado en " + RESUMEN + " (" + Files.size(RESUMEN) + " bytes)");
            }

        } catch (NoSuchFileException e) {                 // la concreta primero
            System.out.println("ERROR: no encuentra el archivo " + e.getFile());
        } catch (IOException e) {                         // resto de E/S (permisos, UTF-8 inválido...)
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
