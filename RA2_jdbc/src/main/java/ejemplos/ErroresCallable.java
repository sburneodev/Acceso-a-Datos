package ejemplos;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

/** Tema 5: los tres errores tipicos con CallableStatement. */
public class ErroresCallable {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            System.out.println("1. Una funcion llamada como si fuera un procedimiento:");
            try (CallableStatement cs = con.prepareCall("{call contar_empleados()}")) {
                cs.execute();
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }

            System.out.println("2. Leer el parametro OUT sin haberlo registrado:");
            try (CallableStatement cs = con.prepareCall("{call obtener_salario(?, ?)}")) {
                cs.setInt(1, 2);
                cs.execute();
                System.out.println("   " + cs.getBigDecimal(2));
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }

            System.out.println("3. Un parametro de menos:");
            try (CallableStatement cs = con.prepareCall("{call obtener_salario(?)}")) {
                cs.registerOutParameter(1, Types.DECIMAL);
                cs.execute();
            } catch (SQLException e) {
                System.out.println("   " + e.getMessage());
            }
        }
    }
}
