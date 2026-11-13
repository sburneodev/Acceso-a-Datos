package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CrearTabla {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             Statement stmt = con.createStatement()) {

            String sql = "CREATE TABLE departamentos (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "nombre VARCHAR(100) NOT NULL)";
            int resultado = stmt.executeUpdate(sql);
            System.out.println("Tabla creada correctamente. executeUpdate ha devuelto " + resultado);

        } catch (Exception e) {
            System.out.println("No se ha podido crear: " + e.getMessage());
        }
    }
}
