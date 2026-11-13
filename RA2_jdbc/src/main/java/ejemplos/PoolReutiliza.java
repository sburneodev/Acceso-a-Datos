package ejemplos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 2: la prueba de que el pool reutiliza la conexion.
 *
 * El hashCode del objeto Connection no sirve para saberlo: el pool entrega cada vez un
 * envoltorio nuevo. Lo que si sirve es preguntarle al SERVIDOR que numero de conexion
 * esta atendiendo: SELECT CONNECTION_ID() devuelve el identificador de la sesion en MySQL.
 */
public class PoolReutiliza {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) throws SQLException {
        System.out.println("--- Sin pool: DriverManager tres veces ---");
        for (int i = 1; i <= 3; i++) {
            try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
                mostrar(i, con);
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASS);
        config.setMaximumPoolSize(5);

        System.out.println("\n--- Con pool: getConnection() tres veces ---");
        try (HikariDataSource pool = new HikariDataSource(config)) {
            for (int i = 1; i <= 3; i++) {
                try (Connection con = pool.getConnection()) {
                    mostrar(i, con);
                }
            }
        }
    }

    private static void mostrar(int vez, Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT CONNECTION_ID()")) {
            rs.next();
            System.out.printf("  vez %d: objeto Java %-11d  conexion en MySQL n.o %d%n",
                    vez, con.hashCode(), rs.getLong(1));
        }
    }
}
