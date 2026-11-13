package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 3: insertar 5.000 filas una a una o por lotes, cronometrado.
 * Se repite con rewriteBatchedStatements=true en la URL: sin esa opcion, el driver de MySQL
 * manda el lote fila a fila y apenas se gana nada.
 */
public class InsertarPorLotes {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";
    private static final int FILAS = 5000;

    public static void main(String[] args) throws SQLException {
        medir("Una a una (executeUpdate en un bucle)", URL, false);
        medir("Por lotes (addBatch + executeBatch)", URL, true);
        medir("Por lotes con rewriteBatchedStatements=true", URL + "?rewriteBatchedStatements=true", true);
    }

    private static void medir(String forma, String url, boolean lote) throws SQLException {
        try (Connection con = DriverManager.getConnection(url, "root", "root");
             Statement st = con.createStatement()) {
            st.execute("DROP TABLE IF EXISTS lotes_prueba");
            st.execute("CREATE TABLE lotes_prueba (id INT PRIMARY KEY, texto VARCHAR(30))");

            long t0 = System.nanoTime();
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO lotes_prueba (id, texto) VALUES (?, ?)")) {
                for (int i = 1; i <= FILAS; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, "fila " + i);
                    if (lote) ps.addBatch();
                    else ps.executeUpdate();
                }
                if (lote) ps.executeBatch();
            }
            long ms = (System.nanoTime() - t0) / 1_000_000;
            System.out.printf("  %-46s %6d ms  (%,d filas por segundo)%n", forma, ms, FILAS * 1000L / Math.max(ms, 1));
            st.execute("DROP TABLE lotes_prueba");
        }
    }
}
