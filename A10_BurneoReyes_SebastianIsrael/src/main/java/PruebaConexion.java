import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PruebaConexion {

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream is = PruebaConexion.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (is == null) {
                System.out.println("No se encontró db.properties");
                return;
            }
            prop.load(is);

        } catch (IOException e) {
            System.out.println("Error al leer db.properties: " + e.getMessage());
            return;
        }

        String url = prop.getProperty("db.url");
        String user = prop.getProperty("db.user");
        String password = prop.getProperty("db.password");

        try (Connection con = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("Conexión OK");
            System.out.println("Base de datos: " + meta.getDatabaseProductName());
            System.out.println("Versión: " + meta.getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
        }
    }
}