package incidencias;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Ejercicio 2: CSV → {@code List<Incidencia>} (con el LectorIncidencias del ejercicio 1) → XML con JAXB.
 * Después relee el XML y comprueba que no se ha perdido nada.
 */
public class Ejercicio2 {                                         // ADAPTAR: nombre exacto de la clase con main

    static final Path CSV = Path.of("datos", "incidencias.csv");
    static final Path XML = Path.of("salida", "incidencias.xml");    // ADAPTAR: nombre del XML

    public static void main(String[] args) {
        try {
            if (!Files.exists(CSV)) {
                System.out.println("ERROR: no encuentra el archivo " + CSV);   // ADAPTAR: texto exacto
                return;
            }

            // CSV → objetos (reutiliza el lector del ejercicio 1)
            List<IncidenciaPojo> incidencias = new LectorIncidencias().leer(CSV);

            // objetos → XML
            ConversorXml conversor = new ConversorXml();
            conversor.guardar(incidencias, XML);
            System.out.println("XML guardado en " + XML + " con " + incidencias.size() + " incidencias"); // ADAPTAR

            // XML → objetos, para comprobar la ida y vuelta
            List<IncidenciaPojo> releidas = conversor.leer(XML);
            if (releidas.equals(incidencias)) {
                System.out.println("Comprobación correcta: " + releidas.size() + " incidencias, sin pérdidas"); // ADAPTAR
            } else {
                System.out.println("ERROR: el XML no coincide con el CSV");
            }

        } catch (NoSuchFileException e) {
            System.out.println("ERROR: no encuentra el archivo " + e.getFile());
        } catch (IOException e) {                        // leer el CSV o crear la carpeta
            System.out.println("ERROR: " + e.getMessage());
        } catch (JAXBException e) {                      // fallo de JAXB al escribir o leer el XML
            // getMessage() a veces es null en JAXB: la causa real va en getLinkedException()/getCause()
            Throwable causa = e.getLinkedException() != null ? e.getLinkedException() : e.getCause();
            System.out.println("ERROR al convertir a XML: " + (causa != null ? causa.getMessage() : e.getMessage()));
        }
    }
}
