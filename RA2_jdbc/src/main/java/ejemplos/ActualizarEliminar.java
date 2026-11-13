package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Tema 3: UPDATE y DELETE con PreparedStatement, y lo que devuelve executeUpdate. */
public class ActualizarEliminar {

    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            // UPDATE: el empleado 1 pasa a cobrar 2200
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE empleados SET salario = ? WHERE id = ?")) {
                ps.setBigDecimal(1, new BigDecimal("2200.00"));
                ps.setInt(2, 1);
                int filas = ps.executeUpdate();
                System.out.println("Filas actualizadas: " + filas);
            }

            // DELETE: se elimina el empleado 4
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM empleados WHERE id = ?")) {
                ps.setInt(1, 4);
                int filas = ps.executeUpdate();
                System.out.println("Filas eliminadas: " + filas);
            }

            // El mismo UPDATE contra un id que no existe: no es un error, son 0 filas
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE empleados SET salario = ? WHERE id = ?")) {
                ps.setBigDecimal(1, new BigDecimal("2200.00"));
                ps.setInt(2, 99);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    System.out.println("Filas actualizadas: 0 -> el empleado 99 no existe; no se ha guardado nada");
                }
            }
        }
    }
}
