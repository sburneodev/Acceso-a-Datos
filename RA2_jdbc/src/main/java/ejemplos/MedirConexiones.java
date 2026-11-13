package ejemplos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 2: cuanto cuesta abrir una conexion, medido.
 *
 * Hace la misma consulta 50 veces de tres maneras y cronometra cada una:
 *   1. abriendo y cerrando una conexion nueva con DriverManager cada vez;
 *   2. con una sola conexion abierta, que se reutiliza (lo que cuesta solo la consulta);
 *   3. pidiendo la conexion a un pool de HikariCP cada vez.
 * Antes de medir se calienta todo (las primeras llamadas de Java siempre son mas lentas).
 */
public class MedirConexiones {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static final int VECES = 50;

    public static void main(String[] args) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASS);
        config.setMaximumPoolSize(5);

        try (HikariDataSource pool = new HikariDataSource(config);
             Connection fija = DriverManager.getConnection(URL, USER, PASS)) {

            // calentamiento: no se mide
            for (int i = 0; i < 5; i++) {
                conDriverManager();
                consultar(fija);
                conPool(pool);
            }

            long t0 = System.nanoTime();
            for (int i = 0; i < VECES; i++) conDriverManager();
            long directa = System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (int i = 0; i < VECES; i++) consultar(fija);
            long soloConsulta = System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (int i = 0; i < VECES; i++) conPool(pool);
            long conPool = System.nanoTime() - t0;

            System.out.println(VECES + " consultas de cada forma:");
            imprimir("Abrir y cerrar con DriverManager", directa);
            imprimir("Una conexion ya abierta (solo la consulta)", soloConsulta);
            imprimir("Pedir la conexion al pool", conPool);
            System.out.printf("Abrir una conexion cuesta unas %.0f veces lo que la consulta.%n",
                    (double) directa / soloConsulta);
        }
    }

    private static void conDriverManager() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            consultar(con);
        }
    }

    private static void conPool(HikariDataSource pool) throws SQLException {
        try (Connection con = pool.getConnection()) {   // close() la devuelve al pool
            consultar(con);
        }
    }

    private static void consultar(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empleados")) {
            rs.next();
        }
    }

    private static void imprimir(String forma, long nanos) {
        System.out.printf("  %-44s %7.1f ms en total  %6.2f ms cada una%n",
                forma, nanos / 1e6, nanos / 1e6 / VECES);
    }
}
