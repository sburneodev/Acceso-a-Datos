import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LectorCodificacion {
    String pathUtf8 =obtenerRuta();


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

    void main() {
        try(BufferedReader bf=new BufferedReader(new FileReader(pathUtf8))){
            System.out.println("Sin especificar: ");
            String linea="";
            int contador=0;
            while((linea=bf.readLine())!=null&&contador<10){
                System.out.println(linea);
                contador++;
            }

        }catch (IOException e){
            System.out.println("Error :"+e.getMessage());
        }
        try(BufferedReader bf=new BufferedReader(new FileReader(pathUtf8, StandardCharsets.UTF_8))){
            System.out.println("\n\n-Especificada: ");
            String linea="";
            int contador=0;
            while((linea=bf.readLine())!=null&&contador<10){
                System.out.println(linea);
                contador++;
            }

        }catch (IOException e){
            System.out.println("Error :"+e.getMessage());
        }

        Path pathIso= Path.of(pathUtf8).resolveSibling("archivoISO.txt");
        try(Reader r= Files.newBufferedReader(Path.of(pathUtf8),StandardCharsets.UTF_8);
            Writer w=Files.newBufferedWriter(pathIso,StandardCharsets.ISO_8859_1)){

            r.transferTo(w);
            System.out.println("Pasado a ISO");

        }catch(IOException e){
            System.out.println("Algo fallo en la trnaferencia");
        }

        try(BufferedReader bf=new BufferedReader(new FileReader(pathIso.toString(),StandardCharsets.UTF_8))){
            String linea="";
            int contador=0;
            while((linea=bf.readLine())!=null&&contador<10){
                System.out.println(linea);
                contador++;
            }

        }catch (IOException e){
            System.out.println("Algo revento al leer ISO en UTF-8: "+e.getMessage());

        }


}

}