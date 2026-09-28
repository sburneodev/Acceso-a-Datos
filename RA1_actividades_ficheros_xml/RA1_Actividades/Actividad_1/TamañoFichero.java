import java.io.* ;
public class TamañoFichero {
    
    public static void main(String[] args) {
        File f=new File("datos.txt");
        System.out.println("Tamaño "+ f.length()+" bytes");
        System.out.println("Directorio de trabajo: "+System.getProperty("user.dir"));
    }
}
