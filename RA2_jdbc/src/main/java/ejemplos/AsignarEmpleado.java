package ejemplos;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Tema 7: asignar el empleado 1 al proyecto 2 con el procedimiento asignar_empleado. */
public class AsignarEmpleado {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             CallableStatement cs = con.prepareCall("{call asignar_empleado(?, ?, ?)}")) {
            cs.setInt(1, 1); // empleado
            cs.setInt(2, 2); // proyecto
            cs.setInt(3, 20); // horas
            cs.execute();
            System.out.println("Empleado 1 asignado al proyecto 2 con 20 horas");
        } catch (SQLException e) {
            System.out.println("No se ha podido asignar: " + e.getMessage());
        }
    }
}
