package ejemplos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tema 4: del ResultSet a objetos. Los metodos recorren el resultado, construyen objetos
 * Empleado y devuelven ESO, nunca el ResultSet, que muere al cerrar la conexion.
 */
public class EmpleadoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";

    public List<Empleado> listar() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(URL, "root", "root");
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, nombre, salario FROM empleados ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(aEmpleado(rs));
            }
        }
        return lista;
    }

    public Optional<Empleado> buscarPorId(int id) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, "root", "root");
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, nombre, salario FROM empleados WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(aEmpleado(rs)) : Optional.empty();
            }
        }
    }

    /** Una fila del ResultSet, la que tenga el cursor, convertida en objeto. */
    private static Empleado aEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("salario"));
    }

    public static void main(String[] args) throws SQLException {
        EmpleadoDAO dao = new EmpleadoDAO();

        List<Empleado> todos = dao.listar();
        System.out.println(todos.size() + " empleados, ordenados por nombre:");
        for (Empleado e : todos) {
            System.out.println("  " + e);
        }

        System.out.println("buscarPorId(2)  -> " + dao.buscarPorId(2));
        System.out.println("buscarPorId(99) -> " + dao.buscarPorId(99));

        BigDecimal total = BigDecimal.ZERO;
        for (Empleado e : todos) total = total.add(e.salario());
        System.out.println("Suma de salarios, calculada en Java con los objetos: " + total);
    }
}
