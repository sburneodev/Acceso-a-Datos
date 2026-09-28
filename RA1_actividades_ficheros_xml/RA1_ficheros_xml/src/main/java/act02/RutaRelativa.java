package act02;

import java.io.File;

public class RutaRelativa {
    public static void main(String[] args) {
        // Obtener ruta base del proyecto
        String rutaBase = System.getProperty("user.dir");
        String separador = File.separator;

        // Construir ruta completa relativa
        String rutaRelativa = rutaBase + separador + "datos" + separador 
            + "ejemplo.txt";

        // Crear objeto File con esa ruta
        File archivo = new File(rutaRelativa);

        // Mostrar información
        System.out.println("Ruta base del proyecto: " + rutaBase);
        System.out.println("Separador de carpetas del sistema: " + separador);
        System.out.println("Ruta relativa completa: " + rutaRelativa);
        System.out.println("¿Existe el archivo? " + archivo.exists());
        System.out.println("Ruta absoluta real: " + archivo.getAbsolutePath());
    }
}
