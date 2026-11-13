package ejemplos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tema 7: crea las tablas proyectos y asignaciones (empleados ya la trae el laboratorio), dos
 * proyectos de partida y el procedimiento asignar_empleado. El procedimiento se crea desde Java:
 * sin DELIMITER, que es cosa del cliente mysql, no de JDBC.
 */
public class CrearProyectos {
    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/empresa", "root", "root");
             Statement st = con.createStatement()) {
            st.execute("DROP PROCEDURE IF EXISTS asignar_empleado");
            st.execute("DROP TABLE IF EXISTS asignaciones");
            st.execute("DROP TABLE IF EXISTS proyectos");
            st.execute("""
                CREATE TABLE proyectos (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(100),
                    presupuesto DECIMAL(10,2)
                )""");
            st.execute("""
                CREATE TABLE asignaciones (
                    id_empleado INT,
                    id_proyecto INT,
                    horas INT,
                    PRIMARY KEY (id_empleado, id_proyecto),
                    FOREIGN KEY (id_empleado) REFERENCES empleados(id),
                    FOREIGN KEY (id_proyecto) REFERENCES proyectos(id)
                )""");
            st.execute("INSERT INTO proyectos (nombre, presupuesto) VALUES "
                    + "('Migración a la nube', 120000.00), ('App de logística', 8000.00)");
            st.execute("""
                CREATE PROCEDURE asignar_empleado(
                    IN empId INT,
                    IN proyId INT,
                    IN horasAsignadas INT
                )
                BEGIN
                    INSERT INTO asignaciones (id_empleado, id_proyecto, horas)
                    VALUES (empId, proyId, horasAsignadas);
                END""");
            System.out.println("Tablas proyectos y asignaciones, dos proyectos y el procedimiento asignar_empleado creados");
        }
    }
}
