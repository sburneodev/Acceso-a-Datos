package revision;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Actividad 7 del RA2: esta clase compila y, a primera vista, funciona. Pero tiene problemas.
 * Antes de ejecutarla: ejemplos.CrearProyectos (crea la tabla proyectos que usa traspasar).
 *
 * NO la copies como modelo: es el codigo que hay que revisar.
 */
public class GestionEmpleados {

    static Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/empresa", "root", "root");
    }

    static ResultSet listar() throws SQLException {
        Statement st = conectar().createStatement();
        return st.executeQuery("SELECT id, nombre, salario FROM empleados");
    }

    static int contarPorNombre(String nombre) throws SQLException {
        Statement st = conectar().createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empleados WHERE nombre = '" + nombre + "'");
        rs.next();
        return rs.getInt(1);
    }

    static void cambiarSalario(int id, double salario) {
        try {
            Statement st = conectar().createStatement();
            st.executeUpdate("UPDATE empleados SET salario = " + salario + " WHERE id = " + id);
            System.out.println("Salario del empleado " + id + " guardado correctamente");
        } catch (SQLException e) {
        }
    }

    static void traspasar(int empleado, int proyecto, double importe) throws SQLException {
        Connection con = conectar();
        Statement st = con.createStatement();
        st.executeUpdate("UPDATE empleados SET salario = salario + " + importe + " WHERE id = " + empleado);
        st.executeUpdate("UPDATE proyectos SET presupuesto = presupuesto - " + importe + " WHERE id = " + proyecto);
        System.out.println("Traspaso de " + importe + " hecho");
    }

    public static void main(String[] args) throws SQLException {
        ResultSet rs = listar();
        while (rs.next()) {
            System.out.println(rs.getInt("id") + " - " + rs.getString("nombre") + " - " + rs.getDouble("salario"));
        }
        System.out.println("Empleados llamados Ana Ruiz: " + contarPorNombre("Ana Ruiz"));
        System.out.println("Empleados llamados ' OR '1'='1: " + contarPorNombre("' OR '1'='1"));
        cambiarSalario(99, 2000.10);
        traspasar(1, 99, 300);
    }
}
