package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Tema 6: como viene una conexion recien abierta. */
public class EstadoConexion {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {
            // Autocommit activado por defecto
            System.out.println("Autocommit: " + con.getAutoCommit());
            int nivel = con.getTransactionIsolation();
            String nombre = switch (nivel) {
                case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
                case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
                case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
                case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
                default -> "otro";
            };
            System.out.println("Nivel de aislamiento: " + nivel + " (" + nombre + ")");
        }
    }
}
