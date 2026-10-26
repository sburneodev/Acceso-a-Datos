import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

public class EscrituraVentas {
    static int numero;
    static LocalDateTime fecha=LocalDateTime.now();
    static String cliente="Cliente";
    static String producto="Producto";
    static int unidades;
    static double precio;

    static int cantidadLineas=10000;
    static long tiempoPlano=0;
    static long tiempoObj=0;
    static long tiempoData=0;

    void main(String[] args) {
        try {
            File file = new File("ventasObj.dat");
            long tiempo1 = System.nanoTime();
            long tiempo2 = System.nanoTime();
            Random rand = new Random();
            ObjectOutputStream oos;
            if(file.exists()&& file.length()>0){
                oos = new ObjectOutputStream(new FileOutputStream("ventasObj.dat", true));
            }else{
                oos = new ObjectOutputStream(new FileOutputStream("ventasObj.dat"));
            }
            for (int i = 0; i < cantidadLineas; i++) {
                numero = i + 1;
                fecha = LocalDateTime.now();
                DateTimeFormatter formato =
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                String fechaFormateada = fecha.format(formato);
                int unidaddes = rand.nextInt(100) + 1;
                precio = 0.75 * unidaddes;

                String plano = String.format("%d||%s||%s||%s||%d||%.2f€", numero, fechaFormateada,
                        cliente, producto, unidades, precio);
                Ventas v = new Ventas(numero, fecha, cliente, producto, unidades, precio);
                escribirPlano(plano, tiempo1, tiempo2);
                escribirData(numero, fechaFormateada, cliente, producto, unidaddes, precio, tiempo1, tiempo2);
                escribirObj(v, tiempo1, tiempo2, oos);


            }
            oos.close();
            int contador = 0;
            try (ObjectInputStream objIn = new ObjectInputStream(
                    new FileInputStream("ventasObj.dat"))) {
                contador = 0;
                while (true) {
                    Ventas venta = (Ventas) objIn.readObject();
                    contador++;
                }

            } catch (EOFException e) {
                System.out.println("Ventas leídas: " + contador);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }catch(Exception e){
            System.out.println("Error al escribir ventas");
        }
    }

    void escribirPlano(String plano,long tiempo1, long tiempo2){
        try(BufferedWriter bwPlano = new BufferedWriter
                (new FileWriter("ventas.txt", StandardCharsets.UTF_8,true))){
            tiempo1=System.nanoTime();
            bwPlano.append(plano);
            bwPlano.newLine();
            tiempo2=System.nanoTime();
            tiempoPlano+=tiempo2-tiempo1;


        }catch(IOException e){
            System.out.println("Error al escribir ventas plano: "+e.getMessage());
        }
    }

    void escribirData(int numero, String fecha, String cliente, String producto, int unidades, double precio
    ,long tiempo1, long tiempo2){

        try(DataOutputStream daos=new DataOutputStream(new FileOutputStream("ventas.dat", true))){
           tiempo1=System.nanoTime();

            daos.writeInt(numero);
            daos.writeUTF(fecha);
            daos.writeUTF(cliente);
            daos.writeUTF(producto);
            daos.writeInt(unidades);
            daos.writeDouble(precio);

            tiempo2=System.nanoTime();
            tiempoData+=tiempo2-tiempo1;

    }catch(IOException e){
            System.out.println("Error al escribir ventas dat: "+e.getMessage());
        }
    }

    void escribirObj(Ventas v, long tiempo1, long tiempo2, ObjectOutputStream oos){
        try{

        tiempo1=System.nanoTime();
        oos.writeObject(v);
        tiempo2=System.nanoTime();
        tiempoData+=tiempo2-tiempo1;
    }catch(IOException e){
            System.out.println("Error al escribir ventasobj dat: "+e.getMessage());
        }
    }


}