package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tema 7: insertar un proyecto y consultar los que superan un presupuesto. */
public class ConsultarProyectos {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root")) {

            String sql = "INSERT INTO proyectos (nombre, presupuesto) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "Proyecto X");
                ps.setBigDecimal(2, new BigDecimal("50000.00"));
                ps.executeUpdate();
            }

            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT * FROM proyectos WHERE presupuesto > ?")) {
                ps2.setBigDecimal(1, new BigDecimal("10000"));
                try (ResultSet rs = ps2.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getInt("id") + " - " +
                                           rs.getString("nombre") + " - " +
                                           rs.getBigDecimal("presupuesto"));
                    }
                }
            }
        }
    }
}
