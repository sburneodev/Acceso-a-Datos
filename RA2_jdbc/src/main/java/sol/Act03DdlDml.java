package sol;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Solución de referencia de la Actividad 3: DDL y DML con JDBC. */
public class Act03DdlDml {

    private static final String URL =
        "jdbc:mysql://localhost:3306/empresa?useSSL=false&characterEncoding=UTF-8&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "root", "root")) {

            crearTabla(conn);
            insertarProyectos(conn);
            System.out.println("\n--- Tras insertar ---");
            listar(conn);

            actualizarPresupuesto(conn, 2, new BigDecimal("95000.00"));
            eliminar(conn, 3);

            System.out.println("\n--- Tras actualizar y eliminar ---");
            listar(conn);
        }
    }

    /** DDL: no lleva parametros, asi que Statement es suficiente. */
    private static void crearTabla(Connection conn) throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS proyectos (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    nombre      VARCHAR(100)  NOT NULL,
                    presupuesto DECIMAL(10,2) NOT NULL CHECK (presupuesto >= 0)
                ) ENGINE=InnoDB""";
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS proyectos");
            st.execute(ddl);
            System.out.println("Tabla proyectos creada.");
        }
    }

    /** DML con parametros: PreparedStatement y ejecucion por lotes. */
    private static void insertarProyectos(Connection conn) throws SQLException {
        String sql = "INSERT INTO proyectos (nombre, presupuesto) VALUES (?, ?)";
        Object[][] datos = {
            {"Migración a la nube", new BigDecimal("120000.00")},
            {"App de logística",    new BigDecimal("80000.00")},
            {"Portal de clientes",  new BigDecimal("45000.00")}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] fila : datos) {
                ps.setString(1, (String) fila[0]);
                ps.setBigDecimal(2, (BigDecimal) fila[1]);
                ps.addBatch();
            }
            int[] resultados = ps.executeBatch();
            System.out.println("Proyectos insertados: " + resultados.length);
        }
    }

    private static void actualizarPresupuesto(Connection conn, int id, BigDecimal nuevo)
            throws SQLException {
        String sql = "UPDATE proyectos SET presupuesto = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevo);
            ps.setInt(2, id);
            System.out.println("Filas actualizadas: " + ps.executeUpdate());
        }
    }

    private static void eliminar(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM proyectos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            System.out.println("Filas eliminadas: " + ps.executeUpdate());
        }
    }

    private static void listar(Connection conn) throws SQLException {
        String sql = "SELECT id, nombre, presupuesto FROM proyectos ORDER BY id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  %d - %-22s %10s%n",
                        rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("presupuesto"));
            }
        }
    }
}
