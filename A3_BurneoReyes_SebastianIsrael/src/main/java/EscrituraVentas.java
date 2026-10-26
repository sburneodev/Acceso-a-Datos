import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class EscrituraVentas {
    static final int CANTIDAD_LINEAS = 10000;
    static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    static File fplano=new File("ventas.txt");
    static File fdata=new File("ventas.dat");
    static File fobj=new File("ventasObj.dat");

    static long tiempoPlano = 0;
    static long tiempoData = 0;
    static long tiempoObj = 0;

    public static void main(String[] args) {
        Random rand = new Random();
        String cliente = "Cliente";
        String producto = "Producto";


        try (BufferedWriter bwPlano = new BufferedWriter(
                new FileWriter("ventas.txt", StandardCharsets.UTF_8));
             DataOutputStream daos = new DataOutputStream(new BufferedOutputStream(
                     new FileOutputStream("ventas.dat")));
             ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                     new FileOutputStream("ventasObj.dat")))) {

            for (int i = 0; i < CANTIDAD_LINEAS; i++) {
                int numero = i + 1;
                LocalDateTime fecha = LocalDateTime.now();
                String fechaFormateada = fecha.format(FORMATO);
                int unidades = rand.nextInt(100) + 1;
                double precio = 0.75 * unidades;

                String plano = String.format("%d||%s||%s||%s||%d||%.2f€",
                        numero, fechaFormateada, cliente, producto, unidades, precio);
                Ventas v = new Ventas(numero, fecha, cliente, producto, unidades, precio);

                escribirPlano(plano, bwPlano);
                escribirData(numero, fechaFormateada, cliente, producto, unidades, precio, daos);
                escribirObj(v, oos, i);
            }


            long t = System.nanoTime(); bwPlano.flush(); tiempoPlano += System.nanoTime() - t;
            t = System.nanoTime();      daos.flush();    tiempoData  += System.nanoTime() - t;
            t = System.nanoTime();      oos.flush();     tiempoObj   += System.nanoTime() - t;

            System.out.printf("Plano: %.2f ms%n", tiempoPlano / 1e6);
            System.out.println("Tamaños: "+(fplano.length()/1024)+"Kb");
            System.out.printf("Data:  %.2f ms%n", tiempoData / 1e6);
            System.out.println("Tamaños: "+(fdata.length()/1024)+"Kb");
            System.out.printf("Obj:   %.2f ms%n", tiempoObj / 1e6);
            System.out.println("Tamaños: "+(fobj.length()/1024)+"Kb");

        } catch (IOException e) {
            System.out.println("Error al escribir ventas: " + e.getMessage());
        }
    }

    static void escribirPlano(String plano, BufferedWriter bw) {
        try {
            long t1 = System.nanoTime();
            bw.append(plano);
            bw.newLine();
            tiempoPlano += System.nanoTime() - t1;
        } catch (IOException e) {
            System.out.println("Error al escribir ventas plano: " + e.getMessage());
        }
    }

    static void escribirData(int numero, String fecha, String cliente, String producto,
                             int unidades, double precio, DataOutputStream daos) {
        try {
            long t1 = System.nanoTime();
            daos.writeInt(numero);
            daos.writeUTF(fecha);
            daos.writeUTF(cliente);
            daos.writeUTF(producto);
            daos.writeInt(unidades);
            daos.writeDouble(precio);
            tiempoData += System.nanoTime() - t1;
        } catch (IOException e) {
            System.out.println("Error al escribir ventas dat: " + e.getMessage());
        }
    }

    static void escribirObj(Ventas v, ObjectOutputStream oos, int i) {
        try {
            long t1 = System.nanoTime();
            oos.writeObject(v);
            if (i % 100 == 0) oos.reset();   // evita acumular referencias en memoria
            tiempoObj += System.nanoTime() - t1;
        } catch (IOException e) {
            System.out.println("Error al escribir ventasobj dat: " + e.getMessage());
        }
    }
}