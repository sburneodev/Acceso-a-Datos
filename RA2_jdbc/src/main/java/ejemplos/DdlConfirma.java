package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 3: en MySQL, una sentencia DDL confirma la transaccion en curso.
 * Se inserta un empleado con el autocommit desactivado, despues se ejecuta un CREATE TABLE,
 * y despues un rollback. El empleado sigue ahi: el CREATE TABLE ya lo habia confirmado.
 */
public class DdlConfirma {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement st = con.createStatement()) {

            con.setAutoCommit(false);
            st.executeUpdate("INSERT INTO empleados (nombre, salario) VALUES ('Prueba DDL', 1000)");
            System.out.println("INSERT hecho, sin commit. Empleados llamados 'Prueba DDL': " + contar(st));

            st.executeUpdate("CREATE TABLE IF NOT EXISTS tabla_ddl (id INT)");
            System.out.println("CREATE TABLE ejecutado.");

            con.rollback();
            System.out.println("rollback() hecho. Empleados llamados 'Prueba DDL': " + contar(st));

            // dejar la base como estaba
            st.executeUpdate("DELETE FROM empleados WHERE nombre = 'Prueba DDL'");
            st.executeUpdate("DROP TABLE tabla_ddl");
            con.commit();
        }
    }

    private static int contar(Statement st) throws SQLException {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empleados WHERE nombre = 'Prueba DDL'")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
