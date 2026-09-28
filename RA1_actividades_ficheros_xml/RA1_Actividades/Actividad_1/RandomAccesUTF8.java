import java.io.*;
import java.nio.charset.StandardCharsets;

public class RandomAccesUTF8 {
    public static void main(String[] args) {
        long posicion = 11;

        try (RandomAccessFile raf = new RandomAccessFile("datos.txt", "r")) {
            System.out.println("Tamaño del fichero: " + raf.length() + " bytes");
            raf.seek(posicion);
            System.out.println("Lectura desde el byte " + posicion + ":");
            System.out.println("> " + leerLineaUTF8(raf));

        } catch (IOException e) {
            System.out.println("Error en acceso aleatorio: " + e);
        }
    }

    /** Lee bytes hasta el salto de linea y los decodifica como UTF-8. */
    private static String leerLineaUTF8(RandomAccessFile raf) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = raf.read()) != -1 && b != '\n') {
            if (b != '\r') {          // ignora el \r de los ficheros CRLF
                buffer.write(b);
            }
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}