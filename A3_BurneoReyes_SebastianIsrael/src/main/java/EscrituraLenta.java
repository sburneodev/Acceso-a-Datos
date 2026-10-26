import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EscrituraLenta {

    static final int CANTIDAD_LINEAS = 10000;
    static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("ventas_lentas.txt"))) {

            for (int i = 1; i <= CANTIDAD_LINEAS; i++) {
                String fechaFormateada = LocalDateTime.now().format(FORMATO);
                String linea = String.format("%d||%s||Cliente||Producto||1||0.75", i, fechaFormateada);

                bw.write(linea);
                bw.newLine();

                System.out.println("Escrita línea " + i + "/10000");

                Thread.sleep(200);
                bw.flush();
            }

            System.out.println("Proceso completado (no lo has matado a tiempo).");

        } catch (IOException e) {
            System.out.println("Error de escritura: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("El programa fue interrumpido.");
        }
    }
}