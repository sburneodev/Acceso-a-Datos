package ejemplos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;

public class EjemploPool {
    public static void main(String[] args) {
        // Configuración del pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/empresa");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(5); // máximo de conexiones simultáneas
        config.setMinimumIdle(2);     // conexiones mínimas en espera
        config.setIdleTimeout(30000); // tiempo de inactividad antes de liberar
        config.setMaxLifetime(1800000); // vida máxima de una conexión

        // Creación del DataSource (pool)
        HikariDataSource dataSource = new HikariDataSource(config);

        // Uso de la conexión
        try (Connection con = dataSource.getConnection()) {
            System.out.println("Conexión obtenida del pool");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cerrar el pool al terminar la aplicación
        dataSource.close();
    }
}
