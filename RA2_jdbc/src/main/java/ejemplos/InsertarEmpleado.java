package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertarEmpleado {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";
        String sql = "INSERT INTO empleados (nombre, salario) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "Carlos");
            ps.setBigDecimal(2, new BigDecimal("2100.00"));
            int filas = ps.executeUpdate();

            System.out.println("Filas insertadas: " + filas);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
