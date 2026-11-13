package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 6: crea (o vuelve a crear) las tablas cuentas y logs de los ejemplos, con sus datos de partida. */
public class CrearCuentas {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement st = con.createStatement()) {
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
            st.execute("INSERT INTO cuentas (titular, saldo) VALUES ('Ana', 2000.00), ('Luis', 1500.00)");
            System.out.println("Tablas cuentas y logs creadas: Ana 2000.00, Luis 1500.00");
        }
    }
}
