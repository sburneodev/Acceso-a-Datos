package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Tema 3: INSERT, UPDATE y DELETE seguidos sobre la misma fila. Deja la tabla como estaba. */
public class OperacionesIntegradas {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/empresa";
        try (Connection con = DriverManager.getConnection(url, "root", "root")) {
            // INSERT
            try (PreparedStatement psInsert = con.prepareStatement(
                    "INSERT INTO empleados (nombre, salario) VALUES (?, ?)")) {
                psInsert.setString(1, "Laura");
                psInsert.setBigDecimal(2, new BigDecimal("1900.00"));
                System.out.println("INSERT: " + psInsert.executeUpdate() + " fila");
            }
            // UPDATE
            try (PreparedStatement psUpdate = con.prepareStatement(
                    "UPDATE empleados SET salario = ? WHERE nombre = ?")) {
                psUpdate.setBigDecimal(1, new BigDecimal("2000.00"));
                psUpdate.setString(2, "Laura");
                System.out.println("UPDATE: " + psUpdate.executeUpdate() + " fila");
            }
            // DELETE
            try (PreparedStatement psDelete = con.prepareStatement(
                    "DELETE FROM empleados WHERE nombre = ?")) {
                psDelete.setString(1, "Laura");
                System.out.println("DELETE: " + psDelete.executeUpdate() + " fila");
            }
            System.out.println("Operaciones DML ejecutadas correctamente.");
        }
    }
}
