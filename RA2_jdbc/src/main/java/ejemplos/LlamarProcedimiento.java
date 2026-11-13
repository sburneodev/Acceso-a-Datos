package ejemplos;

import java.math.BigDecimal;
import java.sql.*;

/** Tema 5: llamar a obtener_salario (crearlo antes en el cliente de MySQL, como dice el libro). */
public class LlamarProcedimiento {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";

        // Los dos recursos van en el try-con-recursos: se cierran solos,
        // tambien si salta una excepcion (criterio de evaluacion 2i).
        try (Connection con = DriverManager.getConnection(url, usuario, password);
             CallableStatement cs = con.prepareCall("{call obtener_salario(?, ?)}")) {

            cs.setInt(1, 2);                            // parametro IN
            cs.registerOutParameter(2, Types.DECIMAL);  // parametro OUT

            cs.execute();

            BigDecimal salario = cs.getBigDecimal(2);   // recuperar parametro OUT
            System.out.println("El salario del empleado 2 es: " + salario);

        } catch (SQLException e) {
            System.err.println("Error al llamar al procedimiento: " + e.getMessage());
        }
    }
}
