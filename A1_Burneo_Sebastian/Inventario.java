import  java.io.*;
import java.sql.SQLOutput;
import java.util.Arrays;

public  class Inventario{
    static int directoriosTotal =0;
    static int archivosTotal =0;
    public static void main(String[] args){

        try {

            if (args.length == 0) {
                System.out.println("Debe recibirse un archivo");
                return;
            }
            File archivo = new File(args[0]);

            if (!archivo.exists()) {
                System.out.println("No existe el archivo");
                return;
            } else {
                System.out.println("Existe");
            }
            if (archivo.isDirectory()) {
                System.out.println("La ruta es un fichero");

            } else if (archivo.isFile()) {
                System.out.println("la ruta es un archivo");
            }
            contar(args[0]);
            System.out.println("Total archivos: " + archivosTotal);
            System.out.println("Total subcarpetas: " + directoriosTotal);
            System.out.println(conteoBytes(archivo));


        }catch (Exception e){
            System.out.println("Algo falló: "+e);
        }


    }

    static void  contar(String fileString){
        File carpeta=new File(fileString);

        /*if(!carpeta.isDirectory()){
            System.out.println("No es una carpeta que podamos recorres. Pon una carpeta.");
            return;
        }*/

        File[] elementos=carpeta.listFiles();

        if(elementos==null){
            System.out.println("carpeta vacía");
            return;
        }

        for (File f:elementos){
            if(f.isFile()){
                System.out.println("Fichero: "+f.getName());
                archivosTotal++;
            } else if (f.isDirectory()) {
                System.out.println("Carpeta: "+f.getName());
                directoriosTotal++;
                contar(f.getAbsolutePath());

            }
        }

    }

    static String conteoBytes(File f){
        long bytes_totales=f.length();
        long kb=0;
        long mb=0;
        if(bytes_totales>1024 && bytes_totales<1048576){
            kb=bytes_totales/1024;
            return "El archivo tiene "+kb+" Kb";

        }else if(bytes_totales>1048576){
            mb=bytes_totales/1048576;
            return "El archivo tiene "+mb+" Mb";

        }
        return "Archivo vacio";
    }

    static void ficheroGrande(File carpeta){
        if (carpeta.isFile()){
            System.out.println("Es un archivo unico.");
        }

    }


}