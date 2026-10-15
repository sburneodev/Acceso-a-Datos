import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Inventario {
    static int directoriosTotal = 0;
    static int archivosTotal = 0;
    static File masReciente;
    static long bytes_grande = 0;
    static String ficheroMasGrande = "";
    static long bytes_actual = -1;
    static int txt = 0;
    static int pdf = 0;

    // Mapa para contar cuántos ficheros se copian por cada extensión
    static Map<String, Integer> conteoOrganizados = new HashMap<>();

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Debe recibirse al menos una ruta.");
                return;
            }

            // Detección del parámetro --organizar
            boolean organizar = false;
            String ruta = "";

            for (String arg : args) {
                if (arg.equalsIgnoreCase("--organizar")) {
                    organizar = true;
                } else if (ruta.isEmpty()) {
                    ruta = arg;
                }
            }

            if (ruta.isEmpty()) {
                System.out.println("Error: Debes indicar una ruta de carpeta.");
                return;
            }

            File archivo = new File(ruta);

            if (!archivo.exists()) {
                System.out.println("No existe el archivo o carpeta.");
                return;
            }

            if (!archivo.canRead()) {
                System.out.println("No tienes derechos en este fichero/archivo.");
                return;
            }

            // MODO 1: --organizar
            if (organizar) {
                if (!archivo.isDirectory()) {
                    System.out.println("Error: La opción --organizar solo puede ejecutarse sobre un directorio.");
                    return;
                }

                System.out.println("Organizando ficheros en: " + archivo.getAbsolutePath());
                organizarFicheros(archivo, archivo);

                System.out.println("\n--- RESUMEN DE COPIA ---");
                if (conteoOrganizados.isEmpty()) {
                    System.out.println("No se copiaron ficheros.");
                } else {
                    for (Map.Entry<String, Integer> entry : conteoOrganizados.entrySet()) {
                        System.out.println("Carpeta '" + entry.getKey() + "/': " + entry.getValue() + " ficheros copiados.");
                    }
                }
                return;
            }

            // MODO 2: Inventario normal
            if (archivo.isDirectory()) {
                System.out.println("La ruta es una carpeta.");
            } else if (archivo.isFile()) {
                System.out.println("La ruta es un archivo.");
            }

            contar(ruta);
            System.out.println("Total archivos: " + archivosTotal);
            System.out.println("Total subcarpetas: " + directoriosTotal);

            if (archivosTotal != 0 || directoriosTotal != 0) {
                System.out.println(conteoBytes(archivo));
                System.out.println("El fichero más grande es " + ficheroGrande(ruta, bytes_grande, ficheroMasGrande));
                System.out.println("El fichero más reciente es " + ficheroReciente(ruta));
                conteoTipo(ruta);
                System.out.println("Hay " + txt + " archivos txt y " + pdf + " archivos pdf.");
            }

        } catch (Exception e) {
            System.out.println("Algo falló: " + e);
        }
    }

    // --- LÓGICA DE ORGANIZACIÓN ---
    static void organizarFicheros(File carpetaActual, File raizDestino) {
        File[] elementos = carpetaActual.listFiles();
        if (elementos == null) return;

        for (File f : elementos) {
            // Ignorar el propio código y las carpetas de organización para evitar bucles
            if (f.getName().equals("Inventario.java") || f.getName().endsWith(".class")) {
                continue;
            }

            if (f.isFile()) {
                copiarSegunExtension(f, raizDestino);
            } else if (f.isDirectory()) {
                // Evitamos volver a procesar las carpetas creadas por el organizador si ya existen en la raíz
                if (carpetaActual.equals(raizDestino)) {
                    String nombreDir = f.getName();
                    if (nombreDir.equals("sin_extension") || (!nombreDir.contains(".") && !nombreDir.contains(" "))) {
                        // Comprobación de seguridad para no re-escanear carpetas destino creadas
                    }
                }
                organizarFicheros(f, raizDestino);
            }
        }
    }

    static void copiarSegunExtension(File fichero, File raizDestino) {
        String nombre = fichero.getName();
        String carpetaExt = "sin_extension";

        int punto = nombre.lastIndexOf('.');
        if (punto > 0 && punto < nombre.length() - 1) {
            carpetaExt = nombre.substring(punto + 1).toLowerCase();
        }

        try {
            // 1. Crear subcarpeta destino si no existe: raiz/extension/
            Path dirDestino = raizDestino.toPath().resolve(carpetaExt);
            if (!Files.exists(dirDestino)) {
                Files.createDirectories(dirDestino);
            }

            // 2. Ruta destino del fichero
            Path destinoFichero = dirDestino.resolve(fichero.getName());

            // 3. Copiar (sin StandardCopyOption.REPLACE_EXISTING para no machacar)
            Files.copy(fichero.toPath(), destinoFichero);

            // 4. Sumar al contador
            conteoOrganizados.put(carpetaExt, conteoOrganizados.getOrDefault(carpetaExt, 0) + 1);

        } catch (FileAlreadyExistsException e) {
            System.out.println("Aviso: El archivo ya existía en destino (" + fichero.getName() + "), no se ha sobreescrito.");
        } catch (IOException e) {
            System.out.println("Error al copiar " + fichero.getName() + ": " + e.getMessage());
        }
    }

    // --- MÉTODOS ANTERIORES DE INVENTARIO ---
    static void contar(String fileString) {
        File carpeta = new File(fileString);
        File[] elementos = carpeta.listFiles();
        if (elementos == null) return;

        for (File f : elementos) {
            if (f.isFile()) {
                archivosTotal++;
            } else if (f.isDirectory()) {
                directoriosTotal++;
                contar(f.getAbsolutePath());
            }
        }
    }

    static String conteoBytes(File f) {
        long bytes_totales = f.length();
        if (bytes_totales > 1048576) {
            return "El archivo tiene " + (bytes_totales / 1048576) + " Mb";
        } else if (bytes_totales > 1024) {
            return "El archivo tiene " + (bytes_totales / 1024) + " Kb";
        }
        return "Archivo vacio o menor a 1Kb";
    }

    static String ficheroGrande(String ruta, long bytes_grande, String ficheroMasGrande) {
        long bytes_actual = -1;
        File carpeta = new File(ruta);
        if (carpeta.isFile()) return carpeta.getName();

        File[] elementos = carpeta.listFiles();
        if (elementos == null) return ficheroMasGrande;

        for (File elemento : elementos) {
            if (elemento.isFile()) {
                bytes_actual = elemento.length();
                if (bytes_actual > bytes_grande) {
                    bytes_grande = bytes_actual;
                    ficheroMasGrande = elemento.getName();
                }
            } else {
                ficheroGrande(elemento.getAbsolutePath(), bytes_grande, ficheroMasGrande);
            }
        }
        return ficheroMasGrande;
    }

    public static String ficheroReciente(String ruta) {
        File carpeta = new File(ruta);
        if (carpeta.isFile()) return "";

        File[] elementos = carpeta.listFiles();
        if (elementos == null) return "";

        for (File f : elementos) {
            if (f.getName().equals("Inventario.java") || f.getName().endsWith(".class")) {
                continue;
            } else if (f.isFile() && f.lastModified() > bytes_actual) {
                bytes_actual = f.lastModified();
                masReciente = f;
            } else if (f.isDirectory()) {
                ficheroReciente(f.getAbsolutePath());
            }
        }
        return masReciente != null ? masReciente.getName() : "Ninguno";
    }

    static void conteoTipo(String ruta) {
        File carpeta = new File(ruta);
        if (carpeta.isFile()) return;

        File[] elementos = carpeta.listFiles();
        if (elementos == null) return;

        for (File f : elementos) {
            if (f.isFile()) {
                if (f.getName().toLowerCase().endsWith(".txt")) txt++;
                else if (f.getName().toLowerCase().endsWith(".pdf")) pdf++;
            } else if (f.isDirectory()) {
                conteoTipo(f.getAbsolutePath());
            }
        }
    }
}
/*Apartado 2.1
import java.io.File;
import java.nio.file.Path;

// Consultar el directorio de trabajo actual:
String cwd = System.getProperty("user.dir");
System.out.println("CWD (dónde estoy parado): " + cwd);

// Con la API moderna (Path.of / toAbsolutePath):
Path rutaRel = Path.of("prueba");
Path rutaAbs = rutaRel.toAbsolutePath().normalize();

System.out.println("Relativa: " + rutaRel);
System.out.println("Absoluta resuelta: " + rutaAbs);
System.out.println("¿Es absoluta?: " + rutaRel.isAbsolute()); // false


Apartado 2.2

import java.io.File;
import java.nio.file.Path;

// Forma antigua (java.io): concatenar con File.separator
String rutaAntigua = "prueba" + File.separator + "nivel 2";

// Forma moderna (java.nio.file): Path gestiona los separadores del SO
Path rutaModerna = Path.of("prueba", "nivel 2", "nivel 3");

// O encadenando rutas con .resolve():
Path base = Path.of("prueba");
Path completa = base.resolve("nivel 2").resolve("nivel 3");

System.out.println("Ruta portable con NIO.2: " + completa);



Apartado 2.3
import java.nio.file.Files;
import java.nio.file.Path;

public class TestRuta {
    public static void main(String[] args) {
        // Si el usuario olvidó las comillas y la terminal separó el nombre por espacios:
        String entrada = (args.length > 0) ? String.join(" ", args) : "Año 2027";

        Path ruta = Path.of(entrada);

        // Files.exists() de NIO.2
        if (!Files.exists(ruta)) {
            try {
                Files.createDirectory(ruta); // La creamos para probar
            } catch (Exception e) {
                System.out.println("Error al crear carpeta: " + e.getMessage());
            }
        }

        System.out.println("Carpeta leída: " + ruta.getFileName());
        System.out.println("¿Existe correctamente?: " + Files.exists(ruta));
    }
}
*/