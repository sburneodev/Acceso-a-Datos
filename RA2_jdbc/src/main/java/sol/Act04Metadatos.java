package sol;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/** Solución de referencia de la Actividad 4: ResultSet y metadatos. */
public class Act04Metadatos {

    private static final String URL =
        "jdbc:mysql://localhost:3306/empresa?useSSL=false&characterEncoding=UTF-8&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "root", "root");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM empleados ORDER BY id")) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnas = rsmd.getColumnCount();

            System.out.println("--- ResultSetMetaData ---");
            System.out.println("Numero de columnas: " + columnas);
            for (int i = 1; i <= columnas; i++) {
                System.out.printf("  %d. %-10s %-10s (%s)%n",
                        i, rsmd.getColumnName(i), rsmd.getColumnTypeName(i),
                        rsmd.getColumnClassName(i));
            }

            System.out.println("\n--- Filas (recorrido generico, sin nombres fijos) ---");
            while (rs.next()) {
                StringBuilder fila = new StringBuilder("  ");
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) fila.append(" | ");
                    fila.append(rsmd.getColumnName(i)).append('=').append(rs.getString(i));
                }
                System.out.println(fila);
            }

            System.out.println("\n--- DatabaseMetaData ---");
            DatabaseMetaData dbmd = conn.getMetaData();
            System.out.println("  Producto : " + dbmd.getDatabaseProductName());
            System.out.println("  Version  : " + dbmd.getDatabaseProductVersion());
            System.out.println("  Driver   : " + dbmd.getDriverName()
                    + " " + dbmd.getDriverVersion());
            System.out.println("  Usuario  : " + dbmd.getUserName());
        }
    }
}
