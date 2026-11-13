import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

public class PruebaConexion {
    private static final Properties prop=new Properties();
    

    public static void getConnection(){
        String url;
        String user;
        String password;
        try(FileInputStream fis=new FileInputStream("./resources/db.properties")){
            prop.load(fis);
             url=prop.getProperty("url");
             user=prop.getProperty("user");
             password=prop.getProperty("password");
            try(Connection con=DriverManager.getConnection(url,user,password)){
                DatabaseMetaData meta=con.getMetaData();
                String driver=meta.getDriverName();
                String driverName=meta.getDriverVersion();

                System.out.println("Conexion a la base de datos [OK]");
                System.out.println("Driver: "+driver);
                System.out.println("Driver name: "+driverName);

            }catch(SQLException e){
                System.out.println("Error al conectar"+e);

            }
        }catch(IOException e){
            System.out.println("Error al cargar el archivo:"+e.getMessage());
        }
    }

    static void main(String[] args) {
        getConnection();
    }
}
