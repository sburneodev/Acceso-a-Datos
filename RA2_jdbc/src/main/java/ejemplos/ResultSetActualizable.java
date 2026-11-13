package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 4: modificar una fila desde el ResultSet, sin UPDATE. Y las tres consultas con las que
 * el driver no lo permite: de dos tablas, sin la clave primaria y con un valor calculado.
 */
public class ResultSetActualizable {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            try (Statement stmt = con.createStatement(
                         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                 ResultSet rs = stmt.executeQuery("SELECT id, nombre, salario FROM empleados ORDER BY id")) {
                if (rs.next()) {
                    System.out.println("Antes:   " + rs.getString("nombre") + " cobra " + rs.getBigDecimal("salario"));
                    rs.updateBigDecimal("salario", new BigDecimal("2500.00"));
                    rs.updateRow();                              // aplica el cambio en la BD
                    System.out.println("Despues: " + rs.getString("nombre") + " cobra " + rs.getBigDecimal("salario"));
                    rs.updateBigDecimal("salario", new BigDecimal("1500.00"));
                    rs.updateRow();                              // y se deja como estaba
                }
            }

            // una segunda tabla, solo para el ejemplo del JOIN
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS deptos (id INT PRIMARY KEY, nombre VARCHAR(20))");
                st.execute("INSERT IGNORE INTO deptos VALUES (1, 'Ventas')");
            }
            String[] noActualizables = {
                "SELECT e.id, e.nombre, e.salario, d.nombre AS depto FROM empleados e JOIN deptos d ON d.id = 1",
                "SELECT nombre, salario FROM empleados",
                "SELECT id, SUM(salario) AS salario FROM empleados GROUP BY id"
            };
            for (String sql : noActualizables) {
                System.out.println(sql);
                try (Statement stmt = con.createStatement(
                             ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                     ResultSet rs = stmt.executeQuery(sql)) {
                    rs.next();
                    rs.updateBigDecimal("salario", new BigDecimal("9999.00"));
                    rs.updateRow();
                } catch (SQLException e) {
                    // el mensaje del driver es largo: su primera frase dice el motivo
                    System.out.println("  -> " + e.getMessage().split("\\.")[0]);
                }
            }
            try (Statement st = con.createStatement()) {
                st.execute("DROP TABLE deptos");
            }
        }
    }
}
