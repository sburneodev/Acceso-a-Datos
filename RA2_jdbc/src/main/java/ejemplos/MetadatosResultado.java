package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 4: ResultSetMetaData, lo que se sabe de un resultado sin conocer la consulta. */
public class MetadatosResultado {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM empleados")) {

            ResultSetMetaData meta = rs.getMetaData();

            int columnas = meta.getColumnCount();
            System.out.println("Número de columnas: " + columnas);

            for (int i = 1; i <= columnas; i++) {
                System.out.println("Columna " + i + ": " +
                                   meta.getColumnName(i) +
                                   " (" + meta.getColumnTypeName(i) + ")");
            }
        }
    }
}
