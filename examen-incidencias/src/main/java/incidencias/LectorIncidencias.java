package incidencias;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee el CSV de incidencias (UTF-8, separador {@code ;}, cabecera en la línea 1).
 *
 * <p>Una línea es válida si al partirla por {@code ;} tiene exactamente 6 campos
 * y el id es un entero. Si no, es una línea rota: no se convierte en incidencia,
 * no detiene la lectura y se apunta su número de línea (contando la cabecera).
 * Las líneas en blanco no son ni válidas ni rotas, pero cuentan en la numeración.</p>
 */
public class LectorIncidencias {

    private static final int NUM_CAMPOS = 6;
    private static final String SEPARADOR = ";";

    /** Números de línea rotas de la última lectura, en el orden del fichero. */
    private final List<Integer> lineasRotas = new ArrayList<>();

    // Constructor sin parámetros: el enunciado lo pide (o no escribir ninguno).
    public LectorIncidencias() {
    }

    /**
     * Lee el CSV y devuelve las incidencias válidas en el orden del fichero.
     *
     * @param csv ruta del fichero CSV
     * @return lista (ArrayList) con las incidencias válidas; vacía si no hay ninguna
     * @throws java.nio.file.NoSuchFileException si el fichero no existe
     * @throws IOException si falla la lectura (también si los bytes no son UTF-8 válido)
     */
    public List<IncidenciaPojo> leer(Path csv) throws IOException {   // ADAPTAR: nombre/firma exactos del enunciado
        lineasRotas.clear();                       // por si se llama dos veces con el mismo objeto
        List<IncidenciaPojo> validas = new ArrayList<>();

        List<String> lineas = Files.readAllLines(csv, StandardCharsets.UTF_8);

        // i = 1 para saltar la cabecera; el número de línea "humano" es i + 1
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            int numLinea = i + 1;

            if (linea.isBlank()) {
                continue;                          // en blanco: ni válida ni rota (pero ya contó en numLinea)
            }

            String[] campos = linea.split(SEPARADOR, -1);   // -1: conserva campos vacíos del final
            if (campos.length != NUM_CAMPOS) {
                lineasRotas.add(numLinea);
                continue;
            }

            int id;
            try {
                id = Integer.parseInt(campos[0].trim());
            } catch (NumberFormatException e) {
                lineasRotas.add(numLinea);         // id no entero → rota
                continue;
            }

            validas.add(new IncidenciaPojo(id,
                    campos[1].trim(), campos[2].trim(), campos[3].trim(),
                    campos[4].trim(), campos[5].trim()));
        }
        return validas;
    }

    /*
     * Alternativa con BufferedReader (por si piden "con br"). Mismo resultado:
     *
     * try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
     *     String linea = br.readLine();          // cabecera (línea 1)
     *     int numLinea = 1;
     *     while ((linea = br.readLine()) != null) {
     *         numLinea++;
     *         ... mismo cuerpo que el for ...
     *     }
     * }
     */

    /**
     * Números de las líneas rotas de la última lectura, en el orden del fichero.
     *
     * @return copia de la lista de números de línea rotas
     */
    public List<Integer> getLineasRotas() {        // ADAPTAR: el segundo método público que pida el enunciado
        return new ArrayList<>(lineasRotas);
    }
}
