package ejemplos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 6: que hace el pool con una conexion devuelta con el autocommit desactivado. */
public class PoolAutocommit {
    public static void main(String[] args) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/empresa");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(1);        // una sola conexion: la segunda vez es la misma
        try (HikariDataSource pool = new HikariDataSource(config)) {
            try (Connection con = pool.getConnection()) {
                con.setAutoCommit(false);
                System.out.println("Prestamo 1: sesion " + sesion(con) + ", autocommit " + con.getAutoCommit()
                        + " (se devuelve sin restaurarlo)");
            }
            try (Connection con = pool.getConnection()) {
                System.out.println("Prestamo 2: sesion " + sesion(con) + ", autocommit " + con.getAutoCommit());
            }
        }
    }

    private static long sesion(Connection con) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT CONNECTION_ID()")) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
