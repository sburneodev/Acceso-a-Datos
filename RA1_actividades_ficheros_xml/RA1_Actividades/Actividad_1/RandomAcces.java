import javax.management.MBeanAttributeInfo;
import  java.io.*;


public class RandomAcces {

    static void main() {
        String ruta="datosSinTilde.txt";
        long posicion=10;
        try(RandomAccessFile raf=new RandomAccessFile(ruta, "r")){
            System.out.println("Tamaño del fichero: "+raf.length()+" bytes");
            raf.seek(posicion);
            System.out.println("Lectura desde la posicion (byte)"+ posicion);
            String linea   = raf.readLine();
            System.out.println(linea);

        }catch(FileNotFoundException e){
            System.out.println("Error en el enrutado de datos.txt"+ System.getProperty("user.dir")+"No tiene datos.txt");
    }catch (IOException e){
        System.out.println("Error: "+e);
    }
    }

}
