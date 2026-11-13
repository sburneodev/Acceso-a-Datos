package ejemplos;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tema 4: un cursor que se mueve en las dos direcciones, y que tipos admite el driver. */
public class CursorDesplazable {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement stmt = con.createStatement(
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery("SELECT id, nombre FROM empleados ORDER BY id")) {

            rs.last();
            System.out.println("last()        -> fila " + rs.getRow() + ": " + rs.getString("nombre")
                    + "   (asi se sabe cuantas filas hay)");
            rs.first();
            System.out.println("first()       -> fila " + rs.getRow() + ": " + rs.getString("nombre"));
            rs.absolute(3);
            System.out.println("absolute(3)   -> fila " + rs.getRow() + ": " + rs.getString("nombre"));
            rs.previous();
            System.out.println("previous()    -> fila " + rs.getRow() + ": " + rs.getString("nombre"));

            DatabaseMetaData meta = con.getMetaData();
            System.out.println();
            System.out.println("Lo que admite este driver:");
            System.out.println("  TYPE_FORWARD_ONLY       " + meta.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
            System.out.println("  TYPE_SCROLL_INSENSITIVE " + meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
            System.out.println("  TYPE_SCROLL_SENSITIVE   " + meta.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
        }
    }
}
