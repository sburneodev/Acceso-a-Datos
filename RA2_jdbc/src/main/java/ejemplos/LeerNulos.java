package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 4: que devuelve getInt cuando la columna vale NULL, y como distinguirlo.
 * La consulta se inventa una columna "jefe" que vale NULL para todos menos para Luis.
 */
public class LeerNulos {
    public static void main(String[] args) throws SQLException {
        String sql = "SELECT nombre, IF(id = 2, 1, NULL) AS jefe FROM empleados ORDER BY id";
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int conGetInt = rs.getInt("jefe");
                boolean eraNulo = rs.wasNull();
                Integer conGetObject = rs.getObject("jefe", Integer.class);
                System.out.printf("  %-14s getInt=%d  wasNull=%-5s  getObject=%s%n",
                        rs.getString("nombre"), conGetInt, eraNulo, conGetObject);
            }
        }
    }
}
