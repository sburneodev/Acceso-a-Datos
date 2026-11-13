package ejemplos;

import java.math.BigDecimal;
import java.sql.*;

/** Bloque 6: crear un procedimiento DESDE JAVA, llamarlo y borrarlo.
 *  El CREATE PROCEDURE va entero en un Statement, sin DELIMITER: DELIMITER es del cliente mysql. */
public class CrearProcedimiento {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             Statement st = con.createStatement()) {

            // 1. Crearlo: si ya existia, se borra antes (asi se puede ejecutar dos veces)
            st.executeUpdate("DROP PROCEDURE IF EXISTS contar_por_salario");
            st.executeUpdate(
                "CREATE PROCEDURE contar_por_salario(IN minimo DECIMAL(10,2), OUT total INT) "
              + "BEGIN "
              + "  SELECT COUNT(*) INTO total FROM empleados WHERE salario >= minimo; "
              + "END");
            System.out.println("Procedimiento contar_por_salario creado.");

            // 2. Llamarlo: el IN se da con set..., el OUT se registra ANTES de ejecutar
            try (CallableStatement cs = con.prepareCall("{call contar_por_salario(?, ?)}")) {
                cs.setBigDecimal(1, new BigDecimal("1750.00"));
                cs.registerOutParameter(2, Types.INTEGER);
                cs.execute();
                System.out.println("Empleados que cobran 1750 o mas: " + cs.getInt(2));
            }

            // 3. Recogerlo al terminar (criterio de evaluacion 2i)
            st.executeUpdate("DROP PROCEDURE contar_por_salario");
            System.out.println("Procedimiento borrado.");

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
