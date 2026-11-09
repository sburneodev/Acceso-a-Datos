package incidencias;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ejercicio 3: prueba de ida y vuelta del conversor XML y del lector CSV.
 */
@DisplayName("Conversión CSV → XML")
class ConversorXmlTest {

    @TempDir
    Path carpeta;     // JUnit la crea antes de cada test y la borra después: no ensucia el proyecto

    @Test
    @DisplayName("la ida y vuelta a XML conserva el número de incidencias y sus datos")
    void idaYVueltaConservaDatos() throws Exception {
        // Preparar: al menos dos incidencias, con tildes y eñes
        List<IncidenciaPojo> original = List.of(
                new IncidenciaPojo(1, "2026-11-02", "A101", "PC-01", "No enciende la pantalla", "ABIERTA"),
                new IncidenciaPojo(2, "2026-11-03", "B204", "Proyector", "Imagen con rayas en el cañón", "CERRADA"));
        Path xml = carpeta.resolve("incidencias.xml");
        ConversorXml conversor = new ConversorXml();

        // Actuar: guardar y volver a leer
        conversor.guardar(original, xml);
        List<IncidenciaPojo> leidas = conversor.leer(xml);

        // Comprobar: mismo número y mismos datos de una de ellas
        assertTrue(Files.exists(xml));
        assertEquals(original.size(), leidas.size());
        IncidenciaPojo segunda = leidas.get(1);
        assertEquals(2, segunda.getId());
        assertEquals("B204", segunda.getAula());
        assertEquals("Imagen con rayas en el cañón", segunda.getDescripcion());
        assertEquals("CERRADA", segunda.getEstado());
        assertEquals(original, leidas);   // y todas enteras (gracias a equals de Incidencia)
    }

    @Test
    @DisplayName("el lector salta cabecera y blancos, y apunta las líneas rotas")
    void lectorDetectaLineasRotas() throws Exception {
        Path csv = carpeta.resolve("prueba.csv");
        Files.write(csv, List.of(
                "id;fecha;aula;equipo;descripcion;estado",          // 1 cabecera
                "1;2026-11-02;A101;PC-01;Pantalla;ABIERTA",         // 2 válida
                "",                                                 // 3 en blanco
                "x2;2026-11-02;A101;PC-02;id no entero;CERRADA",    // 4 rota
                "3;2026-11-03;B204;PC-03;Faltan campos",            // 5 rota
                "4;2026-11-04;B204;Ratón;Rueda rota;CERRADA"),      // 6 válida
                StandardCharsets.UTF_8);
        LectorIncidencias lector = new LectorIncidencias();

        List<IncidenciaPojo> validas = lector.leer(csv);

        assertEquals(2, validas.size());
        assertEquals(List.of(4, 5), lector.getLineasRotas());
        assertEquals("Ratón", validas.get(1).getEquipo());
    }

    @Test
    @DisplayName("CSV → XML → objetos no pierde ninguna incidencia")
    void csvAXmlSinPerdidas() throws Exception {
        Path csv = carpeta.resolve("prueba.csv");
        Files.write(csv, List.of(
                "id;fecha;aula;equipo;descripcion;estado",
                "1;2026-11-02;A101;PC-01;Teclado sin ñ;ABIERTA",
                "2;2026-11-03;C010;Router;Sin conexión;CERRADA"),
                StandardCharsets.UTF_8);
        Path xml = carpeta.resolve("salida").resolve("incidencias.xml");   // la carpeta salida no existe aún

        List<IncidenciaPojo> delCsv = new LectorIncidencias().leer(csv);
        ConversorXml conversor = new ConversorXml();
        conversor.guardar(delCsv, xml);
        List<IncidenciaPojo> delXml = conversor.leer(xml);

        assertEquals(delCsv, delXml);
    }
}
