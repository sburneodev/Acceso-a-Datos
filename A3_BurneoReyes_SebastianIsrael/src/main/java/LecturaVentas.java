import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LecturaVentas{

    static  int cantidad=10000;
    static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    void main() {
        boolean todoOok=true;
        int leidos=0;

        long tiempoPlano = 0;
        long tiempoData = 0;
        long tiempoObj = 0;
        long t1;

        try(BufferedReader brPlano=new BufferedReader(new FileReader("ventas.txt",StandardCharsets.UTF_8));
            DataInputStream dis=new DataInputStream(new BufferedInputStream(new FileInputStream("ventas.dat")));
            ObjectInputStream ois=new ObjectInputStream((new BufferedInputStream(new FileInputStream("ventasObj.dat"))))
        ){
        for(int i =0; i<cantidad;i++){
            t1 = System.nanoTime();
            String linea= brPlano.readLine();
            String[] partes=linea.split("\\|\\|");
            Ventas vPlano= new Ventas(
                    Integer.parseInt(partes[0]
                    ), LocalDateTime.parse(partes[1],FORMATO),
                    partes[2], partes[3],
                    Integer.parseInt(partes[4]), Double.parseDouble(partes[5].replace(",",".").replace("€","")));
            tiempoPlano += System.nanoTime() - t1;

            t1 = System.nanoTime();
            Ventas vData= new Ventas(
                    dis.readInt(),
                    LocalDateTime.parse(dis.readUTF(),FORMATO),
                    dis.readUTF(),
                    dis.readUTF(),
                    dis.readInt(),
                    dis.readDouble()
            );
            tiempoData += System.nanoTime() - t1;

            t1 = System.nanoTime();
            Ventas vObj= (Ventas) ois.readObject();
            tiempoObj += System.nanoTime() - t1;

            if (!vPlano.equals(vData) || !vData.equals(vObj)) {
                System.out.println("¡ERROR! Los datos no coinciden en la línea " + (i + 1));
                todoOok = false;
                break;
            }
            leidos++;
        }
            System.out.println("Todo ok: Archivos leidos coincidentes "+cantidad+"/"+leidos);
            System.out.printf("Texto Plano: %.2f ms%n", tiempoPlano / 1e6);
            System.out.printf("Binario Dat: %.2f ms%n", tiempoData / 1e6);
            System.out.printf("Objetos:     %.2f ms%n", tiempoObj / 1e6);

        } catch(IOException e){
            System.out.println("Error: "+e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error al leer clase: "+e.getMessage());
        }
    }


}