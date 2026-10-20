import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.List;
import java.util.stream.Stream;

public class LectorTodosLosDatos{
String path=obtenerRuta();

    static String obtenerRuta(){
        try{
            Path rutaClase=Path.of(LectorTodosLosDatos.class.getProtectionDomain().
            getCodeSource().getLocation().
            toURI());

            Path rutaActual=rutaClase;
            while(rutaActual !=null && !rutaActual.getFileName().toString().equals("A2_Burneo_Sebastian") ){
                rutaActual=rutaActual.getParent();
            }
            
                return rutaActual.resolve("datos/archivo.txt").toAbsolutePath().toString();
            


        }catch(Exception e){
          return "datos/archivo.txt";  
        }
    }
    void main() throws FileNotFoundException {
        long inicio1=System.nanoTime();
        lecturaByteAByte(path);
        long fin1=System.nanoTime();
        long inicio2=System.nanoTime();
        lectorLineaLineaBF(path);
        long fin2=System.nanoTime();
        long inicio3=System.nanoTime();
        lectorLineaDirectaNio(path);
        long fin3=System.nanoTime();
        long inicio4=System.nanoTime();
        lineaSinCargarTodo(path);
        long fin4=System.nanoTime();
        double primero=(fin1-inicio1)/1e6;
        double segundo=(fin2-inicio2)/1e6;
        double tercero=(fin3-inicio3)/1e6;
        double cuarto=(fin4-inicio4)/1e6;
        System.out.println(String.format("Primero: %.3f ms", primero));
        System.out.println(String.format("Segundo: %.3f ms", segundo));
        System.out.println(String.format("Tercero: %.3f ms", tercero));
        System.out.println(String.format("Cuarto: %.3f ms", cuarto));

        }
    void lecturaByteAByte(String path){
        try(FileInputStream fis=new FileInputStream(path)){
            int byteLeido;
            while((byteLeido=fis.read())!=-1){
                System.out.print((byte) byteLeido + " ");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error al leer el archivo");
        } catch (IOException e) {
            System.out.println("Error al leer el archivo");

        }
    }

    void lectorLineaLineaBF(String path){
        try(BufferedReader br=new BufferedReader(new FileReader(path,StandardCharsets.UTF_8))){
            String linea;
            while((linea=br.readLine())!=null){
                System.out.println(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer archivo: " +e);
        }

    }

    void lectorLineaDirectaNio(String path){
        try{
            List<String> lineas = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
            lineas.forEach(System.out::println);
        }catch (IOException e){
            System.out.println("Error al leer archivo: " +e);
        }
    }

    void lineaSinCargarTodo(String path){
        try(Stream<String> lista=Files.lines(Path.of(path),StandardCharsets.UTF_8)){
            lista.forEach(System.out::println);
        }catch (IOException e){
            System.out.println("Error al leer archivo: " +e);
        }
    }
}