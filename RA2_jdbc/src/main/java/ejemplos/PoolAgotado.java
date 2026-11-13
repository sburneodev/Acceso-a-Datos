package ejemplos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Tema 2: lo que pasa cuando el pool se queda sin conexiones.
 *
 * Parte 1: un pool de 5 conexiones y 10 usuarios a la vez, cada uno ocupando su conexion
 *          un segundo. Se imprime cuanto ha esperado cada uno a que le dieran conexion.
 * Parte 2: la fuga. Se piden 5 conexiones y NO se cierran; la sexta peticion espera
 *          el connectionTimeout (2 s) y falla.
 */
public class PoolAgotado {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";

    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(2000);   // como mucho 2 s esperando una conexion
        config.setPoolName("PoolCinco");

        try (HikariDataSource pool = new HikariDataSource(config)) {

            System.out.println("--- Parte 1: 10 usuarios, 5 conexiones ---");
            long inicio = System.nanoTime();
            List<Thread> usuarios = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                int n = i;
                Thread t = new Thread(() -> usar(pool, n, inicio));
                usuarios.add(t);
                t.start();
            }
            for (Thread t : usuarios) t.join();

            System.out.println("\n--- Parte 2: 5 conexiones que nadie cierra ---");
            List<Connection> olvidadas = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                olvidadas.add(pool.getConnection());   // sin try-with-resources: no vuelven
                System.out.println("  pedida la conexion " + i + " (y no se cierra)");
            }
            long t0 = System.nanoTime();
            try (Connection sexta = pool.getConnection()) {
                System.out.println("  la sexta ha llegado");
            } catch (SQLException e) {
                System.out.printf("  la sexta, tras esperar %.1f s: %s%n",
                        (System.nanoTime() - t0) / 1e9, e.getMessage());
            }
            for (Connection c : olvidadas) c.close();
        }
    }

    private static void usar(HikariDataSource pool, int n, long inicio) {
        long pide = System.nanoTime();
        try (Connection con = pool.getConnection();
             Statement st = con.createStatement()) {
            long espera = (System.nanoTime() - pide) / 1_000_000;
            st.execute("SELECT SLEEP(1)");                       // ocupa la conexion 1 s
            System.out.printf("  usuario %2d: espero %4d ms, termino en el segundo %.1f%n",
                    n, espera, (System.nanoTime() - inicio) / 1e9);
        } catch (SQLException e) {
            System.out.println("  usuario " + n + ": " + e.getMessage());
        }
    }
}
