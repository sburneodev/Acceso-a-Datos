package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 4: los tres errores tipicos al leer un ResultSet. */
public class ErroresCursor {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";

    public static void main(String[] args) throws SQLException {
        System.out.println("1. Leer sin haber llamado a next():");
        try (Connection con = DriverManager.getConnection(URL, "root", "root");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM empleados")) {
            System.out.println("   " + rs.getString("nombre"));
        } catch (SQLException e) {
            System.out.println("   " + e.getMessage());
        }

        System.out.println("2. La columna 0:");
        try (Connection con = DriverManager.getConnection(URL, "root", "root");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM empleados")) {
            rs.next();
            System.out.println("   " + rs.getString(0));
        } catch (SQLException e) {
            System.out.println("   " + e.getMessage());
        }

        System.out.println("3. Un ResultSet devuelto por un metodo que ya ha cerrado la conexion:");
        ResultSet rs = consultarMal();
        try {
            rs.next();
        } catch (SQLException e) {
            System.out.println("   " + e.getMessage());
        }
    }

    /** MAL: devuelve el ResultSet. El try-con-recursos lo cierra al salir del metodo. */
    private static ResultSet consultarMal() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, "root", "root");
             Statement st = con.createStatement()) {
            return st.executeQuery("SELECT nombre FROM empleados");
        }
    }
}
