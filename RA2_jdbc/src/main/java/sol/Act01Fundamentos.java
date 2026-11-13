package sol;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Solución de referencia de la Actividad 1: fundamentos de JDBC.
 * Statement, PreparedStatement y CallableStatement sobre la base `empresa`.
 */
public class Act01Fundamentos {

    private static final String URL =
        "jdbc:mysql://localhost:3306/empresa?useSSL=false&characterEncoding=UTF-8&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            System.out.println("--- 1. Statement: listado completo ---");
            listarConStatement(conn);

            System.out.println("\n--- 2. PreparedStatement: buscar por id ---");
            buscarPorId(conn, 2);
            buscarPorId(conn, 99);

            System.out.println("\n--- 3. PreparedStatement frente a inyeccion SQL ---");
            buscarPorNombre(conn, "Ana Ruiz");
            buscarPorNombre(conn, "' OR '1'='1");

            System.out.println("\n--- 4. CallableStatement: obtener_empleado ---");
            obtenerEmpleado(conn, 3);
        }
    }

    /** Statement: vale porque la consulta no lleva ningun dato variable. */
    private static void listarConStatement(Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, salario FROM empleados ORDER BY id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  %d - %-14s %8.2f%n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getDouble("salario"));
            }
        }
    }

    /** PreparedStatement: obligatorio en cuanto hay un parametro. */
    private static void buscarPorId(Connection conn, int id) throws SQLException {
        String sql = "SELECT nombre, salario FROM empleados WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("  id %d -> %s (%.2f)%n",
                            id, rs.getString("nombre"), rs.getDouble("salario"));
                } else {
                    System.out.println("  id " + id + " -> no existe");
                }
            }
        }
    }

    private static void buscarPorNombre(Connection conn, String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleados WHERE nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.printf("  nombre=[%s] -> %d filas%n", nombre, rs.getInt(1));
            }
        }
    }

    /** CallableStatement: invoca el procedimiento almacenado. */
    private static void obtenerEmpleado(Connection conn, int id) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call obtener_empleado(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %d - %s (%.2f)%n",
                            rs.getInt("id"), rs.getString("nombre"), rs.getDouble("salario"));
                }
            }
        }
    }
}
