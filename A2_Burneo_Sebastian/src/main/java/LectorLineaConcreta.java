import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LectorLineaConcreta {
    
    final static long  linea=150000;
    
    static String path=obtenerRuta();

    static String obtenerRuta(){
        try{
            Path rutaClase=Path.of(LectorLineaConcreta.class.getProtectionDomain().
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

    public static void main(String[] args) {
        long inicio1=System.nanoTime();        
        try(BufferedReader bf=new BufferedReader(new FileReader(path,StandardCharsets.UTF_8))){
            String lineaLeida="";
            int contador=0;
            
            while((lineaLeida=bf.readLine())!=null){
                if (contador<=linea) {
                System.out.println(lineaLeida);
                    
                }
                contador++;
            }


        }catch(IOException e){
            System.out.println("Algo salio mal: "+e);
        }
        long fin1=System.nanoTime();

        Path pathObject=Path.of(path);
        long inicio2=System.nanoTime();
        try (FileChannel fchanel=FileChannel.open(pathObject, StandardOpenOption.READ)){

            long posicionDestino=150000;
            fchanel.position(posicionDestino);
            ByteBuffer byteBufer= ByteBuffer.allocate(100000024);
            int bytesLeidos=fchanel.read(byteBufer);

            if(bytesLeidos!=-1){
                byteBufer.flip();
                String resultado = new String(byteBufer.array(), 0, bytesLeidos);
                System.out.println("Contenido desde la posición " + posicionDestino + ":");
                System.out.println(resultado);
            }


        } catch (IOException e) {
             e.printStackTrace();
        }
        long fin2=System.nanoTime();

        double tiempo1=(fin1-inicio1)/1e6;
        double tiempo2=(fin2-inicio2)/1e6;



        System.out.println("Tiempo secuencial: "+tiempo1+ " ms");
        System.out.println("Tiempo acceso aleatorio: "+tiempo2+ " ms");


    }



}
