import  java.io.*;
import java.sql.SQLOutput;
import java.util.Arrays;

public  class Inventario{
    static int directoriosTotal =0;
    static int archivosTotal =0;
    static File masReciente;
    static long byte_reciente;
    static long bytes_grande=0;
    static String ficheroMasGrande="";
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
            ficheroGrande(args[0], bytes_grande, ficheroMasGrande);
            ficheroReciente(args[0], byte_reciente);


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

    static void ficheroGrande(String ruta, long bytes_grande,String ficheroMasGrande){

         long bytes_actual=-1;
        File carpeta=new File(ruta);
        if (carpeta.isFile()){
            System.out.println("Es un archivo unico.");
            return;
        }
        File[] elementos=carpeta.listFiles();
        for(int i=0;i<elementos.length;i++){
            if (elementos[i].isFile()){
                bytes_actual=elementos[i].length();
                if(bytes_actual>bytes_grande){
                    bytes_grande=bytes_actual;
                    ficheroMasGrande=elementos[i].getName();
                }
            }else {
                ficheroGrande(elementos[i].getAbsolutePath(),bytes_grande,ficheroMasGrande);
            }
        }
        System.out.println(ficheroMasGrande);
    }

    public static void ficheroReciente(String ruta,long byte_reciente){
        long bytes_actual=-1;
        File carpeta=new File(ruta);
        if (carpeta.isFile()){
            System.out.println("Es un archivo unico.");
            return ;
        }
        File[] elementos=carpeta.listFiles();
        for (File f:elementos){
            if(f.isFile()&&f.lastModified()>bytes_actual){
                bytes_actual=f.lastModified();
                masReciente=f;
        }else if(f.isDirectory()){
                ficheroReciente(ruta,byte_reciente);

            }
        }
        System.out.println("El archivo más reciente es: "+masReciente.getName());
    }


}