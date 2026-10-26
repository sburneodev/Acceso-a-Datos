import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public  class EscrituraEnUltimaLinea {

    void main() {
        long conteoAntes;
        long conteoDespues;
        int cantidadNueva=3;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("ventas.txt", true))) {
            conteoAntes = Files.lines(Paths.get("ventas.txt")).count();
            for (int i=0;i<cantidadNueva;i++) {

                String linea = String.format("%d||26/10/2026 18:00:31||Cliente||Producto||1||0.75€",conteoAntes+i+1);
                bw.write(linea);
                bw.newLine();
            }
            bw.flush();
            conteoDespues=Files.lines(Paths.get("ventas.txt")).count();

            System.out.println("Antes: "+conteoAntes+"\nDESPUES: "+conteoDespues);
        } catch (IOException e) {
            System.out.println("Algo falló: "+e.getMessage());
        }
    }
}