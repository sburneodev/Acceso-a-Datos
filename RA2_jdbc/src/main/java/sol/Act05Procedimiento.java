package sol;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Solución de referencia de la Actividad 5: CallableStatement con parámetro OUT. */
public class Act05Procedimiento {

    private static final String URL =
        "jdbc:mysql://localhost:3306/empresa?useSSL=false&characterEncoding=UTF-8&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "root", "root")) {

            // Dejar el salario en su valor de partida para poder repetir la actividad
            try (PreparedStatement ps = conn.prepareStatement(
                     "UPDATE empleados SET salario = 1500.00 WHERE id = 1")) {
                ps.executeUpdate();
            }

            System.out.println("Salario ANTES : " + salarioDe(conn, 1));

            try (CallableStatement cs =
                     conn.prepareCall("{call incrementar_salario(?, ?, ?)}")) {

                cs.setInt(1, 1);                                  // IN  id
                cs.setBigDecimal(2, new BigDecimal("250.00"));    // IN  incremento
                cs.registerOutParameter(3, Types.DECIMAL);        // OUT nuevo salario

                cs.execute();

                BigDecimal nuevo = cs.getBigDecimal(3);
                System.out.println("El procedimiento devuelve: " + nuevo);
            }

            System.out.println("Salario DESPUES: " + salarioDe(conn, 1));

            // Caso de error: el procedimiento avisa si el empleado no existe
            try (CallableStatement cs =
                     conn.prepareCall("{call incrementar_salario(?, ?, ?)}")) {
                cs.setInt(1, 999);
                cs.setBigDecimal(2, new BigDecimal("100.00"));
                cs.registerOutParameter(3, Types.DECIMAL);
                cs.execute();
                System.out.println("Empleado 999: " + cs.getBigDecimal(3));
            } catch (SQLException e) {
                System.out.println("Empleado 999 -> error controlado: " + e.getMessage());
            }
        }
    }

    private static BigDecimal salarioDe(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps =
                 conn.prepareStatement("SELECT salario FROM empleados WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }
}
