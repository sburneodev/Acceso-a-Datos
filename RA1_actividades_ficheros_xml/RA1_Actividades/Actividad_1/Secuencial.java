import  java.io.*;
import java.nio.charset.StandardCharsets;
public class Secuencial {
    public static void main(String[] args) {
        File archivo= new File("datos.txt");

        try(BufferedReader br=new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))){
            String linea;
            System.out.println("Lectura del archivo de manera secuencial");
            while ((linea=br.readLine())!=null) {
                System.out.println(linea);
                
            }

        }catch(FileNotFoundException e){
            System.out.println("Error en el enrutado de datos.txt"+ System.getProperty("user.dir")+"No tiene datos.txt");
        }catch(IOException e){
            System.out.println( "Error: "+e);
        }

    }
}
