import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas automáticas del Conversor (casos del plan de pruebas, Apartado 1).
 * Cada prueba: 1) crea su fichero de entrada, 2) convierte, 3) comprueba con aserciones, 4) borra lo creado.
 */
class ConversorTest {

    static final String CABECERA = "numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario";

    Path carpeta;   // carpeta temporal propia de cada prueba

    @BeforeEach
    void preparar() throws IOException {
        carpeta = Files.createTempDirectory("conversor-test");
    }

    @AfterEach
    void limpiar() throws IOException {               // punto 4: borra todo lo que la prueba haya creado
        try (Stream<Path> s = Files.walk(carpeta)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
        assertFalse(Files.exists(carpeta), "La carpeta temporal no se ha borrado");
    }

    // Crea un fichero de entrada en la carpeta temporal
    Path crear(String nombre, String contenido) throws IOException {
        Path p = carpeta.resolve(nombre);
        Files.writeString(p, contenido, StandardCharsets.UTF_8);
        return p;
    }

    // ---------- CASOS NORMALES ----------

    @Test
    @DisplayName("Caso 1 (normal): CSV → JSON agrupa líneas por pedido y conserva acentos")
    void csvAJsonAgrupaLineasYConservaAcentos() throws IOException {
        Path entrada = crear("pedidos.csv", CABECERA + "\n"
                + "1001,2026-03-15,7,Ferretería Núñez,Martillo,2,12.5\n"
                + "1001,2026-03-15,7,Ferretería Núñez,Clavos,100,0.05\n"
                + "1002,2026-03-16,8,Bricolaje Peña,Taladro,1,89.9\n");
        Path salida = carpeta.resolve("pedidos_convertido.json");

        Conversor.escribirJson(Conversor.leerCSV(entrada), salida);
        List<Pedido> releidos = Conversor.leerJson(salida);

        assertTrue(Files.exists(salida));
        assertEquals(2, releidos.size());
        Pedido p1 = releidos.get(0);
        assertEquals(1001, p1.getNumero());
        assertEquals(LocalDate.of(2026, 3, 15), p1.getFecha());
        assertEquals("Ferretería Núñez", p1.getCliente().getNombre());
        assertEquals(2, p1.getLineasPedido().size());
        assertEquals("Clavos", p1.getLineasPedido().get(1).getArticulo());
        assertEquals(0.05, p1.getLineasPedido().get(1).getPrecioUnitario(), 1e-9);
    }

    @Test
    @DisplayName("Caso 3 (normal): ida y vuelta CSV → JSON → XML → CSV deja el fichero idéntico")
    void idaYVueltaDejaElCsvIdentico() throws IOException {
        String original = CABECERA + "\n"
                + "1001,2026-03-15,7,Ferretería Núñez,Martillo,2,12.5\n"
                + "1002,2026-03-16,8,Bricolaje Peña,Taladro,1,89.9\n";
        Path csv = crear("pedidos.csv", original);
        Path json = carpeta.resolve("a.json");
        Path xml = carpeta.resolve("a.xml");
        Path csv2 = carpeta.resolve("vuelta.csv");

        Conversor.escribirJson(Conversor.leerCSV(csv), json);
        Conversor.escribirXML(Conversor.leerJson(json), xml);
        Conversor.escribirCSV(Conversor.leerXML(xml), csv2);

        assertEquals(original, Files.readString(csv2, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Caso 4 (normal): comas y comillas dentro de un campo sobreviven a CSV → JSON → CSV")
    void comasYComillasEnCampos() throws IOException {
        String original = CABECERA + "\n"
                + "2001,2026-04-01,9,\"López, Hermanos\",\"Tornillo de 3\"\" galvanizado\",50,0.2\n";
        Path csv = crear("comillas.csv", original);
        Path json = carpeta.resolve("comillas.json");
        Path csv2 = carpeta.resolve("vuelta.csv");

        List<Pedido> leidos = Conversor.leerCSV(csv);
        assertEquals("López, Hermanos", leidos.get(0).getCliente().getNombre());
        assertEquals("Tornillo de 3\" galvanizado", leidos.get(0).getLineasPedido().get(0).getArticulo());

        Conversor.escribirJson(leidos, json);
        assertTrue(Files.readString(json).contains("Tornillo de 3\\\" galvanizado"), "La comilla no se escapó en el JSON");

        Conversor.escribirCSV(Conversor.leerJson(json), csv2);
        assertEquals(original, Files.readString(csv2, StandardCharsets.UTF_8));
    }

    // ---------- CASOS LÍMITE ----------

    @Test
    @DisplayName("Caso 5 (límite): fichero CSV vacío lanza error controlado")
    void csvVacioDaErrorControlado() throws IOException {
        Path vacio = crear("vacio.csv", "");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Conversor.leerCSV(vacio));
        assertTrue(e.getMessage().contains("vacío"));
    }

    @Test
    @DisplayName("Caso 7 (límite): pedido sin líneas sale como lineas: [] y <lineas/>")
    void pedidoSinLineas() throws IOException {
        Path csv = crear("sinlineas.csv", CABECERA + "\n4001,2026-05-02,2,Sin Líneas,,,\n");
        Path json = carpeta.resolve("s.json");
        Path xml = carpeta.resolve("s.xml");

        List<Pedido> leidos = Conversor.leerCSV(csv);
        assertEquals(1, leidos.size());
        assertTrue(leidos.get(0).getLineasPedido().isEmpty());

        Conversor.escribirJson(leidos, json);
        assertTrue(Files.readString(json).contains("\"lineas\": []"));

        Conversor.escribirXML(Conversor.leerJson(json), xml);
        assertTrue(Files.readString(xml).contains("<lineas/>"));
        assertTrue(Conversor.leerXML(xml).get(0).getLineasPedido().isEmpty());
    }

    @Test
    @DisplayName("Caso 8 (límite): el int más grande (2147483647) no se pierde")
    void intMaximoSeConserva() throws IOException {
        Path csv = crear("max.csv", CABECERA + "\n"
                + "2147483647,2026-05-03,2147483647,Max,Tuerca,2147483647,1.0\n");
        Path json = carpeta.resolve("max.json");

        Conversor.escribirJson(Conversor.leerCSV(csv), json);
        Pedido p = Conversor.leerJson(json).get(0);

        assertEquals(Integer.MAX_VALUE, p.getNumero());
        assertEquals(Integer.MAX_VALUE, p.getCliente().getId());
        assertEquals(Integer.MAX_VALUE, p.getLineasPedido().get(0).getUnidades());
    }

    // ---------- CASOS DE ERROR ----------

    @Test
    @DisplayName("Caso 12 (error): XML mal formado indica línea y columna")
    void xmlMalFormado() throws IOException {
        Path xml = crear("mal.xml", "<?xml version=\"1.0\"?>\n<pedidos>\n"
                + "  <pedido numero=\"1\" fecha=\"2026-01-01\">\n"
                + "    <cliente id=\"1\">A</cliente>\n</pedidos>\n");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Conversor.leerXML(xml));
        assertTrue(e.getMessage().contains("línea 5"), "Mensaje recibido: " + e.getMessage());
    }

    @Test
    @DisplayName("Caso 13 (error): CSV con campos de menos indica la línea")
    void csvConCamposDeMenos() throws IOException {
        Path csv = crear("campos.csv", CABECERA + "\n5001,2026-01-01,1,Cliente,Martillo,2\n");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Conversor.leerCSV(csv));
        assertEquals("Línea 2: se esperaban 7 campos y hay 6", e.getMessage());
    }
}
