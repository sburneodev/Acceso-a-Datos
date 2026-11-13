package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 6: dos conexiones a la vez, A lee y B escribe. Se repite con tres niveles de aislamiento
 * para A. Necesita las tablas de CrearCuentas, y deja el saldo de Ana como estaba.
 */
public class NivelesAislamiento {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";

    public static void main(String[] args) throws SQLException {
        lecturaSucia();
        lecturaNoRepetible(Connection.TRANSACTION_READ_COMMITTED, "READ_COMMITTED");
        lecturaNoRepetible(Connection.TRANSACTION_REPEATABLE_READ, "REPEATABLE_READ");
    }

    /** B cambia sin confirmar; A, en READ_UNCOMMITTED, lo ve. Despues B se arrepiente. */
    private static void lecturaSucia() throws SQLException {
        System.out.println("--- Lectura sucia: A en READ_UNCOMMITTED ---");
        try (Connection a = DriverManager.getConnection(URL, "root", "root");
             Connection b = DriverManager.getConnection(URL, "root", "root")) {
            a.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            b.setAutoCommit(false);
            ejecutar(b, "UPDATE cuentas SET saldo = 9999 WHERE id = 1");
            System.out.println("  B cambia el saldo de Ana a 9999 SIN commit");
            System.out.println("  A lee: " + saldoAna(a));
            b.rollback();
            System.out.println("  B hace rollback. A lee: " + saldoAna(a));
        }
    }

    /** A lee dos veces dentro de su transaccion; entre medias B cambia y confirma. */
    private static void lecturaNoRepetible(int nivel, String nombre) throws SQLException {
        System.out.println("--- Leer dos veces la misma fila: A en " + nombre + " ---");
        try (Connection a = DriverManager.getConnection(URL, "root", "root");
             Connection b = DriverManager.getConnection(URL, "root", "root")) {
            a.setTransactionIsolation(nivel);
            a.setAutoCommit(false);
            System.out.println("  A lee: " + saldoAna(a));
            ejecutar(b, "UPDATE cuentas SET saldo = saldo + 100 WHERE id = 1");   // B en autocommit
            System.out.println("  B suma 100 al saldo de Ana y confirma");
            System.out.println("  A vuelve a leer: " + saldoAna(a));
            a.commit();
            ejecutar(b, "UPDATE cuentas SET saldo = saldo - 100 WHERE id = 1");   // se deja como estaba
        }
    }

    private static BigDecimal saldoAna(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT saldo FROM cuentas WHERE id = 1")) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private static void ejecutar(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }
}
