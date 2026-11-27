package com;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class Conexiones{

    /*Maximo conexion de pools. 5 por ser un entorno de pruebas, ya que cubren la
    * concurrencia esperada sin acaparar conexiones del servidor de BD
    */
    public static final int maxPool=5;

    //Minimo dos conexiones a la espera
    public static final int minIdle=2;

    //30 s  para cerrar conexión si no hay uso.
    public static final int idleTimeout=30000;

    //30 min de vida maxima de la conexión.
    public static final int maxLifeCic=1800000;

    private static final HikariDataSource POOL = crearPool();

    private static Boolean esH2;

    private Conexiones() { }//Así nadie podra crear com.Conexiones.

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Conexiones::cerrar));
    }

    private static HikariDataSource crearPool() {
        Properties prop = new Properties();

        try (InputStream is = Conexiones.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (is == null) {
                throw new IllegalStateException("No se encontró db.properties");
            }
            prop.load(is);

        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer db.properties: " ,e);

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

        return  new HikariDataSource(config);
    }

    public static Connection getConexion() throws SQLException {
        return POOL.getConnection();
    }

    public static void cerrar() {
        if (!POOL.isClosed()) {
            POOL.close();
        }
    }

    public static boolean esH2() throws SQLException {
        if (esH2 == null) {
            try (Connection con = getConexion()) {
                esH2 = "H2".equals(con.getMetaData().getDatabaseProductName());
            }
        }
        return esH2;
    }
}

