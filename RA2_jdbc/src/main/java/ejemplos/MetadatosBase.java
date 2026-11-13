package ejemplos;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tema 4: DatabaseMetaData, lo que se sabe de la base entera. */
public class MetadatosBase {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            DatabaseMetaData dbMeta = con.getMetaData();
            System.out.println("Producto BD: " + dbMeta.getDatabaseProductName());
            System.out.println("Versión: " + dbMeta.getDatabaseProductVersion());
            System.out.println("Driver: " + dbMeta.getDriverName());

            System.out.println("\nTablas de la base empresa:");
            try (ResultSet tablas = dbMeta.getTables("empresa", null, "%", new String[] {"TABLE"})) {
                while (tablas.next()) {
                    System.out.println("  " + tablas.getString("TABLE_NAME"));
                }
            }

            System.out.println("\nColumnas de empleados:");
            try (ResultSet cols = dbMeta.getColumns("empresa", null, "empleados", "%")) {
                while (cols.next()) {
                    System.out.printf("  %-8s %-8s nulos: %s%n", cols.getString("COLUMN_NAME"),
                            cols.getString("TYPE_NAME"), cols.getString("IS_NULLABLE"));
                }
            }

            System.out.println("\nClave primaria de empleados:");
            try (ResultSet pk = dbMeta.getPrimaryKeys("empresa", null, "empleados")) {
                while (pk.next()) {
                    System.out.println("  " + pk.getString("COLUMN_NAME"));
                }
            }
        }
    }
}
