package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 4: el recorrido de siempre, while (rs.next()). */
public class CursorBasico {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nombre, salario FROM empleados")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                BigDecimal salario = rs.getBigDecimal("salario");
                System.out.println(id + " - " + nombre + " - " + salario);
            }
        }
    }
}
