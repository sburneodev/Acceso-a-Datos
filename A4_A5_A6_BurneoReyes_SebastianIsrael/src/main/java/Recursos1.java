import java.io.*;
import java.nio.file.*;

public class Recursos1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java Recursos1 <fichero>");
            return;
        }
        Path original = Path.of(args[0]);
        Path copia = Path.of("copia_prueba.csv");

        try {
            Files.copy(original, copia, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("No se pudo preparar la copia: " + e.getMessage());
            return;
        }

        try (BufferedReader br =new BufferedReader(new FileReader(copia.toFile()))){

            String linea;
            int n = 0;
            while ((linea = br.readLine()) != null) {
                n++;
                System.out.println("Línea " + n + ": " + linea);
                if (n == 3) {
                    throw new IllegalStateException("Fallo provocado en la línea 3");
                }
            }
            System.out.println("Fichero cerrado");
        } catch (IllegalStateException e) {
            System.out.println("Error a mitad de la lectura: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        }

        // Compruebo si la copia sigue abierta: intento borrarla
        try {
            Files.delete(copia);
            System.out.println("Borrado correctamente");
        } catch (IOException e) {
            System.out.println("No se puede borrar: " + e.getMessage());
        }
    }
}