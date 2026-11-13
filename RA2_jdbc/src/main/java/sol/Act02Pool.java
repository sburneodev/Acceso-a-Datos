package sol;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Solución de referencia de la Actividad 2: pool de conexiones con HikariCP. */
public class Act02Pool {

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/empresa"
                + "?useSSL=false&characterEncoding=UTF-8"
                + "&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(5);      // maximo de conexiones
        config.setMinimumIdle(2);          // conexiones minimas en espera
        config.setIdleTimeout(30000);      // 30 s de inactividad
        config.setPoolName("PoolEmpresa");
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void cerrarPool() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            // try-with-resources: al salir, close() DEVUELVE la conexion al pool,
            // no la cierra de verdad. Ese es el truco del pool.
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre FROM empleados ORDER BY id")) {

                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString("nombre"));
                }
                System.out.println("Conexion " + i + " (" + conn.hashCode() + "): " + sb);

            } catch (SQLException e) {
                System.err.println("Error en la conexion " + i + ": " + e.getMessage());
            }
        }
        cerrarPool();
        System.out.println("Pool cerrado.");
    }
}
