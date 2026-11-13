package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public class Savepoints {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";

        Connection con = null;
        Savepoint sp1 = null;

        try {
            con = DriverManager.getConnection(url, usuario, password);
            con.setAutoCommit(false);

            try (Statement stmt = con.createStatement()) {

                // Paso 1
                stmt.executeUpdate("INSERT INTO logs (mensaje) VALUES ('Paso 1 completado')");
                sp1 = con.setSavepoint("Punto1");

                // Paso 2: falla a proposito. La columna mensaje es UNIQUE y este
                // texto ya existe, asi que el servidor rechaza el INSERT.
                stmt.executeUpdate("INSERT INTO logs (mensaje) VALUES ('Paso 1 completado')");
            }

            con.commit();
            System.out.println("Los dos pasos se han confirmado");

        } catch (SQLException e) {
            System.err.println("Error en el paso 2: " + e.getMessage());
            if (con != null) {
                try {
                    if (sp1 != null) {
                        con.rollback(sp1);   // vuelve al savepoint: el paso 1 sigue vivo
                        con.commit();        // y se confirma solo el paso 1
                        System.out.println("Paso 2 descartado; el paso 1 se ha mantenido");
                    } else {
                        con.rollback();      // ni siquiera llego al savepoint: se deshace todo
                        System.out.println("Rollback total");
                    }
                } catch (SQLException ex) {
                    System.err.println("Fallo al deshacer: " + ex.getMessage());
                }
            }

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ex) {
                    System.err.println("Error al cerrar la conexion: " + ex.getMessage());
                }
            }
        }
    }
}
