package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 3: recuperar el id que ha generado AUTO_INCREMENT al insertar. */
public class ClaveGenerada {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";
            int idNuevo;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Sara");
                ps.setBigDecimal(2, new BigDecimal("1650.00"));
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    claves.next();
                    idNuevo = claves.getInt(1);
                }
            }
            System.out.println("Sara se ha guardado con el id " + idNuevo);

            // y se borra con ese mismo id, para dejar la tabla como estaba
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM empleados WHERE id = ?")) {
                ps.setInt(1, idNuevo);
                System.out.println("Borrada con el id " + idNuevo + ": " + ps.executeUpdate() + " fila");
            }
        }
    }
}
