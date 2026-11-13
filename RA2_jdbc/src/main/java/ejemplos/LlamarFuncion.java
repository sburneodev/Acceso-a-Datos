package ejemplos;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

/** Tema 5: llamar a la funcion contar_empleados (crearla antes en el cliente de MySQL). */
public class LlamarFuncion {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             CallableStatement cs = con.prepareCall("{? = call contar_empleados()}")) {
            cs.registerOutParameter(1, Types.INTEGER);   // el retorno es el parametro 1
            cs.execute();
            int total = cs.getInt(1);
            System.out.println("Numero de empleados: " + total);
        }
    }
}
