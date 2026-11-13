package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransaccionIntegrada {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        Connection con = null;

        try {
            con = DriverManager.getConnection(url, "root", "root");
            con.setAutoCommit(false);

            BigDecimal importe = new BigDecimal("300.00");

            try (PreparedStatement ps1 = con.prepareStatement(
                        "UPDATE empleados SET salario = salario + ? WHERE id = ?");
                 PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE proyectos SET presupuesto = presupuesto - ? WHERE id = ?")) {

                ps1.setBigDecimal(1, importe);
                ps1.setInt(2, 1);
                ps1.executeUpdate();

                ps2.setBigDecimal(1, importe);
                ps2.setInt(2, 2);
                ps2.executeUpdate();
            }

            con.commit();
            System.out.println("Transaccion confirmada (commit)");

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("Rollback ejecutado");
                } catch (SQLException ex) {
                    System.err.println("Fallo el rollback: " + ex.getMessage());
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
