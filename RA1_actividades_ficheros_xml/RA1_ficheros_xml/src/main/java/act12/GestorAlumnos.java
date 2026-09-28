package act12;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Solucion de referencia de la Actividad 12 (CE 1f).
 *
 * Lee ficheros CSV de alumnos de forma robusta: cada fichero que falla se
 * registra con un motivo comprensible y el programa sigue con el siguiente.
 */
public class GestorAlumnos {

    // ------------------------------------------------------------ excepcion propia
    /** El fichero se leyo bien, pero su contenido no cumple el formato esperado. */
    public static class FormatoAlumnoException extends Exception {
        private final int linea;
        public FormatoAlumnoException(String mensaje, int linea, Throwable causa) {
            super("linea " + linea + ": " + mensaje, causa);
            this.linea = linea;
        }
        public int getLinea() { return linea; }
    }

    public record Alumno(int id, String nombre, String apellidos, int edad, double nota) {
        public Alumno {
            if (nota < 0 || nota > 10) {
                throw new IllegalArgumentException("nota fuera de rango: " + nota);
            }
        }
    }

    // ------------------------------------------------------------ lectura
    /**
     * Lee el CSV. NO captura nada: propaga y deja decidir a quien llama.
     *
     * @throws IOException              si el fichero no existe, no se puede leer
     *                                  o no esta en UTF-8
     * @throws FormatoAlumnoException   si alguna linea no tiene el formato esperado
     */
    public static List<Alumno> leer(Path ruta) throws IOException, FormatoAlumnoException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            throw new FormatoAlumnoException("el fichero esta vacio", 0, null);
        }

        List<Alumno> alumnos = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {        // i = 1: saltar la cabecera
            String linea = lineas.get(i).strip();
            if (linea.isEmpty()) continue;

            String[] c = linea.split(";", -1);
            int numero = i + 1;                          // numero de linea para el usuario

            if (c.length != 5) {
                throw new FormatoAlumnoException(
                        "se esperaban 5 campos y hay " + c.length, numero, null);
            }
            try {
                alumnos.add(new Alumno(
                        Integer.parseInt(c[0].strip()), c[1], c[2],
                        Integer.parseInt(c[3].strip()),
                        Double.parseDouble(c[4].strip())));
            } catch (IllegalArgumentException e) {
                // NumberFormatException hereda de IllegalArgumentException, asi que este
                // unico catch cubre tanto "veinte" en la edad como una nota fuera de rango.
                // (Por eso NO valdria un multi-catch con las dos: no compila.)
                throw new FormatoAlumnoException(e.getMessage() == null
                        ? "dato numerico incorrecto"
                        : "dato incorrecto (" + e.getMessage() + ")", numero, e);
            }
        }
        return alumnos;
    }

    // ------------------------------------------------------------ informe
    /** Aqui SI se captura: es la capa que habla con el usuario. */
    public static String procesar(Path ruta) {
        try {
            List<Alumno> alumnos = leer(ruta);
            double media = alumnos.stream().mapToDouble(Alumno::nota).average().orElse(0);
            return String.format("OK       %-20s %d alumnos, nota media %.2f",
                                 ruta.getFileName(), alumnos.size(), media);

        } catch (NoSuchFileException e) {
            return String.format("ERROR    %-20s no existe (%s)",
                                 ruta.getFileName(), ruta.toAbsolutePath());
        } catch (AccessDeniedException e) {
            return String.format("ERROR    %-20s sin permiso de lectura",
                                 ruta.getFileName());
        } catch (MalformedInputException e) {
            return String.format("ERROR    %-20s no esta guardado en UTF-8; vuelve a "
                                 + "guardarlo en UTF-8", ruta.getFileName());
        } catch (IOException e) {
            return String.format("ERROR    %-20s error de E/S: %s",
                                 ruta.getFileName(), e.getClass().getSimpleName());
        } catch (FormatoAlumnoException e) {
            return String.format("RECHAZADO %-19s %s (causa: %s)",
                                 ruta.getFileName(), e.getMessage(),
                                 e.getCause() == null ? "ninguna"
                                                      : e.getCause().getClass().getSimpleName());
        }
    }

    // ------------------------------------------------------------ programa
    public static void main(String[] args) throws IOException {
        Path dir = Path.of("datos");
        Files.createDirectories(dir);
        prepararFicherosDePrueba(dir);

        List<Path> ficheros = List.of(
                dir.resolve("alumnos_ok.csv"),
                dir.resolve("alumnos_campos.csv"),
                dir.resolve("alumnos_numero.csv"),
                dir.resolve("alumnos_nota.csv"),
                dir.resolve("alumnos_latin1.csv"),
                dir.resolve("alumnos_inexistente.csv"));

        List<String> informe = new ArrayList<>();
        for (Path f : ficheros) {
            String linea = procesar(f);
            System.out.println(linea);
            informe.add(linea);
        }

        // Escritura con try-with-resources y Charset explicito
        Path salida = Path.of("salida", "informe.txt");
        Files.createDirectories(salida.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(salida, StandardCharsets.UTF_8)) {
            for (String l : informe) {
                bw.write(l);
                bw.newLine();
            }
        }
        System.out.println("\nInforme escrito en " + salida);
    }

    /** Crea los seis casos de prueba (uno de ellos, a proposito, no se crea). */
    private static void prepararFicherosDePrueba(Path dir) throws IOException {
        Files.writeString(dir.resolve("alumnos_ok.csv"), """
                id;nombre;apellidos;edad;nota
                1;Juan;García López;20;8.5
                2;María;Rodríguez;19;9.2
                3;Íñigo;Muñoz;21;7.8
                """, StandardCharsets.UTF_8);

        Files.writeString(dir.resolve("alumnos_campos.csv"), """
                id;nombre;apellidos;edad;nota
                1;Juan;García López;20;8.5
                2;María;Rodríguez;19
                """, StandardCharsets.UTF_8);

        Files.writeString(dir.resolve("alumnos_numero.csv"), """
                id;nombre;apellidos;edad;nota
                1;Juan;García López;veinte;8.5
                """, StandardCharsets.UTF_8);

        Files.writeString(dir.resolve("alumnos_nota.csv"), """
                id;nombre;apellidos;edad;nota
                1;Juan;García López;20;8.5
                2;María;Rodríguez;19;12.0
                """, StandardCharsets.UTF_8);

        // Mismo contenido, guardado en Windows-1252: provoca MalformedInputException
        Files.write(dir.resolve("alumnos_latin1.csv"),
                ("id;nombre;apellidos;edad;nota\n1;Juan;García López;20;8.5\n")
                        .getBytes(StandardCharsets.ISO_8859_1));

        Files.deleteIfExists(dir.resolve("alumnos_inexistente.csv"));
    }
}
