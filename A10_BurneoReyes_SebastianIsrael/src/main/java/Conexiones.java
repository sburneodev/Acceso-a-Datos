import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

public class Conexiones{

    //Maximo conexion de pools. 5 por ser un entorno de pruebas
    public static final int maxPool=5;

    //Minimo dos conexiones a la espera por defecto
    public static final int minIdle=2;

    //30 s por defecto para cerrar conexión si no hay uso.
    public static final int idleTimeout=30000;

    //30 min de vida maxima de la conexión.
    public static final int maxLifeCic=1800000;



    static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream is = PruebaConexion.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (is == null) {
                System.out.println("No se encontró db.properties");
                return;
            }
            prop.load(is);

        } catch (IOException e) {
            System.out.println("Error al leer db.properties: " + e.getMessage());
            return;
        }

        String url = prop.getProperty("db.url");
        String user = prop.getProperty("db.user");
        String password = prop.getProperty("db.password");
        HikariConfig config=new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPool);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifeCic);

        HikariDataSource dataSource=new HikariDataSource(config);
        try(Connection con=dataSource.getConnection()){
            System.out.println("Conexión obtenida del pool");
        }catch (Exception e){
            e.printStackTrace();
        }

        dataSource.close();
        
    }
}
