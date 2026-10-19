import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class GeneradorDatos {
    final long cantidad=200000;
    final String archivo="archivo.txt";

    void main() {
        try{
        Files.createDirectories(Path.of("datos"));

        FileWriter fw=new FileWriter("datos/"+archivo, StandardCharsets.UTF_8);
        BufferedWriter bw=new BufferedWriter(fw);

        for(int i=0;i<=cantidad;i++){
            Random rand = new Random();
            double randomNum = 0 + ( 5000  ) * rand.nextDouble();
            String frase=String.format("%06d;Álvaro Muñoz Peña;1987-03-14;%.2f",i,randomNum);
            bw.write(String.format(frase));
            bw.newLine();

        }
        bw.close();

    }catch (IOException e){
            System.out.println("Error al escribir: "+e);
        }

}
}