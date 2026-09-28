package act01;

import java.io.File;

public class TamanoFichero {
    public static void main(String[] args) {
        File f = new File("datos/datos.txt");
        System.out.println("Tamaño: " + f.length() + " bytes");
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
    }
}
