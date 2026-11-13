package sol;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Solución de referencia de la Actividad 7: caso integrado.
 * Pool de conexiones + PreparedStatement + procedimiento almacenado + transacción.
 */
public class Act07Integrado {

    private static final HikariDataSource POOL = crearPool();

    private static HikariDataSource crearPool() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mysql://localhost:3306/empresa"
                + "?useSSL=false&characterEncoding=UTF-8"
                + "&connectionTimeZone=Europe/Madrid&allowPublicKeyRetrieval=true");
        cfg.setUsername("root");
        cfg.setPassword("root");
        cfg.setMaximumPoolSize(5);
        cfg.setPoolName("PoolIntegrado");
        return new HikariDataSource(cfg);
    }

    public static void main(String[] args) throws SQLException {
        crearEsquema();
        insertarDatos();
        crearProcedimiento();

        System.out.println("--- 1. Asignar empleados con el procedimiento ---");
        asignar(1, 1, 160);
        asignar(2, 1, 120);
        asignar(1, 1, 80);        // repetido: el procedimiento avisa

        System.out.println("\n--- 2. Transaccion: subir sueldo y descontarlo del proyecto ---");
        subirSueldoConCargoAlProyecto(1, 1, new BigDecimal("200.00"));
        subirSueldoConCargoAlProyecto(2, 2, new BigDecimal("999999.00"));   // debe fallar

        System.out.println("\n--- 3. Estado final ---");
        estadoFinal();
        POOL.close();
    }

    private static void crearEsquema() throws SQLException {
        try (Connection c = POOL.getConnection(); Statement st = c.createStatement()) {
            st.execute("DROP TABLE IF EXISTS asignaciones");
            st.execute("DROP TABLE IF EXISTS proyectos_int");
            st.execute("DROP TABLE IF EXISTS empleados_int");
            st.execute("""
                CREATE TABLE empleados_int (
                    id      INT AUTO_INCREMENT PRIMARY KEY,
                    nombre  VARCHAR(100)  NOT NULL,
                    salario DECIMAL(10,2) NOT NULL CHECK (salario > 0)
                )""");
            st.execute("""
                CREATE TABLE proyectos_int (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    nombre      VARCHAR(100)  NOT NULL,
                    presupuesto DECIMAL(12,2) NOT NULL CHECK (presupuesto >= 0)
                )""");
            st.execute("""
                CREATE TABLE asignaciones (
                    id           INT AUTO_INCREMENT PRIMARY KEY,
                    empleado_id  INT NOT NULL,
                    proyecto_id  INT NOT NULL,
                    horas        INT NOT NULL CHECK (horas > 0),
                    UNIQUE KEY uk_emp_pro (empleado_id, proyecto_id),
                    FOREIGN KEY (empleado_id) REFERENCES empleados_int(id),
                    FOREIGN KEY (proyecto_id) REFERENCES proyectos_int(id)
                )""");
        }
    }

    private static void insertarDatos() throws SQLException {
        try (Connection c = POOL.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO empleados_int (nombre, salario) VALUES (?, ?)")) {
                for (Object[] e : new Object[][]{
                        {"Ana Ruiz", new BigDecimal("1500.00")},
                        {"Luis Márquez", new BigDecimal("1800.00")}}) {
                    ps.setString(1, (String) e[0]);
                    ps.setBigDecimal(2, (BigDecimal) e[1]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO proyectos_int (nombre, presupuesto) VALUES (?, ?)")) {
                for (Object[] p : new Object[][]{
                        {"Migración a la nube", new BigDecimal("120000.00")},
                        {"App de logística", new BigDecimal("500.00")}}) {
                    ps.setString(1, (String) p[0]);
                    ps.setBigDecimal(2, (BigDecimal) p[1]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private static void crearProcedimiento() throws SQLException {
        try (Connection c = POOL.getConnection(); Statement st = c.createStatement()) {
            st.execute("DROP PROCEDURE IF EXISTS asignar_empleado");
            st.execute("""
                CREATE PROCEDURE asignar_empleado(
                    IN  p_empleado INT,
                    IN  p_proyecto INT,
                    IN  p_horas    INT,
                    OUT p_resultado VARCHAR(120))
                BEGIN
                    DECLARE v_ya INT DEFAULT 0;
                    SELECT COUNT(*) INTO v_ya FROM asignaciones
                     WHERE empleado_id = p_empleado AND proyecto_id = p_proyecto;
                    IF v_ya > 0 THEN
                        SET p_resultado = 'AVISO: ya estaba asignado';
                    ELSE
                        INSERT INTO asignaciones (empleado_id, proyecto_id, horas)
                        VALUES (p_empleado, p_proyecto, p_horas);
                        SET p_resultado = CONCAT('OK: empleado ', p_empleado,
                                                 ' asignado al proyecto ', p_proyecto);
                    END IF;
                END""");
        }
    }

    private static void asignar(int empleado, int proyecto, int horas) {
        try (Connection c = POOL.getConnection();
             CallableStatement cs = c.prepareCall("{call asignar_empleado(?, ?, ?, ?)}")) {
            cs.setInt(1, empleado);
            cs.setInt(2, proyecto);
            cs.setInt(3, horas);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            System.out.println("   " + cs.getString(4));
        } catch (SQLException e) {
            System.err.println("   Error: " + e.getMessage());
        }
    }

    /** Sube el sueldo y descuenta el mismo importe del presupuesto, todo o nada. */
    private static void subirSueldoConCargoAlProyecto(int empleado, int proyecto,
                                                      BigDecimal importe) {
        Connection c = null;
        try {
            c = POOL.getConnection();
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(
                     "UPDATE empleados_int SET salario = salario + ? WHERE id = ?")) {
                ps.setBigDecimal(1, importe);
                ps.setInt(2, empleado);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                     "UPDATE proyectos_int SET presupuesto = presupuesto - ? WHERE id = ?")) {
                ps.setBigDecimal(1, importe);
                ps.setInt(2, proyecto);
                ps.executeUpdate();
            }

            c.commit();
            System.out.println("   Commit: " + importe + " EUR cargados al proyecto " + proyecto);

        } catch (SQLException e) {
            System.out.println("   Error: " + e.getMessage());
            try {
                if (c != null) {
                    c.rollback();
                    System.out.println("   Rollback: ni el sueldo sube ni el presupuesto baja");
                }
            } catch (SQLException ex) {
                System.err.println("   " + ex.getMessage());
            }
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); }
                catch (SQLException e) { System.err.println(e.getMessage()); }
            }
        }
    }

    private static void estadoFinal() throws SQLException {
        try (Connection c = POOL.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                     "SELECT id, nombre, salario FROM empleados_int ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("   empleado %d  %-14s %10s%n",
                            rs.getInt(1), rs.getString(2), rs.getBigDecimal(3));
                }
            }
            try (ResultSet rs = st.executeQuery(
                     "SELECT id, nombre, presupuesto FROM proyectos_int ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("   proyecto %d  %-22s %12s%n",
                            rs.getInt(1), rs.getString(2), rs.getBigDecimal(3));
                }
            }
            try (ResultSet rs = st.executeQuery(
                     "SELECT empleado_id, proyecto_id, horas FROM asignaciones ORDER BY id")) {
                while (rs.next()) {
                    System.out.printf("   asignacion: empleado %d -> proyecto %d (%d h)%n",
                            rs.getInt(1), rs.getInt(2), rs.getInt(3));
                }
            }
        }
    }
}
