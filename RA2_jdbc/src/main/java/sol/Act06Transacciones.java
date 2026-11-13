package sol;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Solución de referencia de la Actividad 6: transacciones, rollback y savepoints.
 *
 * Tres escenarios:
 *   A) transferencia correcta            -> commit
 *   B) transferencia excesiva            -> rollback completo (lo impide CHECK saldo >= 0)
 *   C) fallo al registrar el paso 2      -> rollback parcial a un savepoint
 */
public class Act06Transacciones {

    private static final String URL =
        "jdbc:mysql://localhost:3306/empresa?useSSL=false&characterEncoding=UTF-8&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws SQLException {
        prepararDatos();

        System.out.println("--- Estado inicial ---");
        mostrarEstado();

        System.out.println("\n--- A) Transferencia de 500 EUR de Ana a Luis ---");
        transferir(1, 2, new BigDecimal("500.00"));
        mostrarEstado();

        System.out.println("\n--- B) Transferencia de 99.999 EUR (debe fallar) ---");
        transferir(1, 2, new BigDecimal("99999.00"));
        mostrarEstado();

        System.out.println("\n--- C) Registro por pasos con savepoint ---");
        registrarPasosConSavepoint();
        mostrarEstado();
    }

    /** Deja las tablas en su estado de partida para poder repetir la actividad. */
    private static void prepararDatos() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "root", "root");
             Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS logs");
            st.execute("DROP TABLE IF EXISTS cuentas");
            st.execute("""
                CREATE TABLE cuentas (
                    id      INT AUTO_INCREMENT PRIMARY KEY,
                    titular VARCHAR(100)  NOT NULL,
                    saldo   DECIMAL(10,2) NOT NULL CHECK (saldo >= 0)
                )""");
            st.execute("""
                CREATE TABLE logs (
                    id      INT AUTO_INCREMENT PRIMARY KEY,
                    mensaje VARCHAR(100) NOT NULL UNIQUE
                )""");
            st.execute("""
                INSERT INTO cuentas (titular, saldo) VALUES
                    ('Ana', 2000.00), ('Luis', 1500.00)""");
        }
    }

    /** Transferencia atomica: o se hacen los dos UPDATE, o no se hace ninguno. */
    private static void transferir(int origen, int destino, BigDecimal importe) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, "root", "root");
            conn.setAutoCommit(false);

            actualizarSaldo(conn, origen,  importe.negate());
            actualizarSaldo(conn, destino, importe);

            conn.commit();
            System.out.println("   Transferencia confirmada (commit)");

        } catch (SQLException e) {
            System.out.println("   Error: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("   Rollback completo: no se ha movido nada");
                }
            } catch (SQLException ex) {
                System.err.println("   Fallo el rollback: " + ex.getMessage());
            }
        } finally {
            cerrar(conn);
        }
    }

    /** Dos pasos: el primero se conserva aunque el segundo falle. */
    private static void registrarPasosConSavepoint() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, "root", "root");
            conn.setAutoCommit(false);

            insertarLog(conn, "Paso 1 completado");
            System.out.println("   Paso 1 registrado");

            Savepoint trasPaso1 = conn.setSavepoint("TRAS_PASO_1");

            try {
                // Falla a proposito: la columna mensaje es UNIQUE
                insertarLog(conn, "Paso 1 completado");
                System.out.println("   Paso 2 registrado");
            } catch (SQLException e) {
                System.out.println("   Error en el paso 2: " + e.getMessage());
                conn.rollback(trasPaso1);
                System.out.println("   Paso 2 descartado; el paso 1 se ha mantenido");
            }

            conn.commit();

        } catch (SQLException e) {
            System.err.println("   Error general: " + e.getMessage());
        } finally {
            cerrar(conn);
        }
    }

    private static void actualizarSaldo(Connection conn, int id, BigDecimal delta)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                 "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?")) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private static void insertarLog(Connection conn, String mensaje) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO logs (mensaje) VALUES (?)")) {
            ps.setString(1, mensaje);
            ps.executeUpdate();
        }
    }

    private static void mostrarEstado() {
        try (Connection conn = DriverManager.getConnection(URL, "root", "root");
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT id, titular, saldo FROM cuentas ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("   cuenta %d  %-6s %10s%n",
                            rs.getInt(1), rs.getString(2), rs.getBigDecimal(3));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id, mensaje FROM logs ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("   log %d: %s%n", rs.getInt(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /** Restaurar autocommit ANTES de cerrar: la conexion puede venir de un pool. */
    private static void cerrar(Connection conn) {
        if (conn == null) return;
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            System.err.println("   Error al cerrar: " + e.getMessage());
        }
    }
}
