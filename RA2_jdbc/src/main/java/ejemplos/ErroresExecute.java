package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 3: los tres errores de elegir mal el metodo, o de parametrizar lo que no se puede. */
public class ErroresExecute {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement st = con.createStatement()) {

            System.out.println("1. executeQuery con un UPDATE:");
            try {
                st.executeQuery("UPDATE empleados SET salario = salario WHERE id = 1");
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }

            System.out.println("2. executeUpdate con un SELECT:");
            try {
                st.executeUpdate("SELECT * FROM empleados");
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }

            System.out.println("3. Un nombre de tabla como parametro:");
            try (PreparedStatement ps = con.prepareStatement("CREATE TABLE ? (id INT)")) {
                ps.setString(1, "prueba");
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }
        }
    }
}
