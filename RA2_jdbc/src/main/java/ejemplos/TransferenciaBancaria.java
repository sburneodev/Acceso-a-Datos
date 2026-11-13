package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransferenciaBancaria {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/empresa";
        String usuario = "root";
        String password = "root";

        BigDecimal importe = new BigDecimal("500.00");

        // 1. La conexion se declara FUERA del try: el catch y el finally la necesitan.
        Connection con = null;

        try {
            con = DriverManager.getConnection(url, usuario, password);

            // 2. Se abre la transaccion: a partir de aqui nada se guarda solo.
            con.setAutoCommit(false);

            try (PreparedStatement ps1 = con.prepareStatement(
                        "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?");
                 PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?")) {

                ps1.setBigDecimal(1, importe);
                ps1.setInt(2, 1);
                ps1.executeUpdate();

                ps2.setBigDecimal(1, importe);
                ps2.setInt(2, 2);
                ps2.executeUpdate();
            }

            // 3. Todo ha ido bien: se confirma.
            con.commit();
            System.out.println("Transferencia confirmada (commit)");

        } catch (SQLException e) {
            System.err.println("Error en la transferencia: " + e.getMessage());

            // 4. Deshacer. El rollback tambien puede fallar, asi que va en su propio try.
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("Rollback ejecutado: no se ha movido dinero");
                } catch (SQLException ex) {
                    System.err.println("Fallo el rollback: " + ex.getMessage());
                }
            }

        } finally {
            // 5. Restaurar el modo por defecto y cerrar.
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                    System.out.println("Autocommit restaurado y conexion cerrada");
                } catch (SQLException ex) {
                    System.err.println("Error al cerrar la conexion: " + ex.getMessage());
                }
            }
        }
    }
}
