package conexion;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;

public class TestConexion {
    public static void main(String[] args) {
        // 1. Cargar configuración desde db.properties
        Properties props = new Properties();
        try (InputStream input = TestConexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("[ERROR] No se encontró el archivo db.properties");
                return;
            }
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 2. Obtener datos de conexión
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        // 3. Probar conexión
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            System.out.println("[OK] Conexión establecida con éxito a la base de datos.");
            
            // Mostrar metadatos
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("   Driver: " + meta.getDriverName());
            System.out.println("   Versión del driver: " + meta.getDriverVersion());
            System.out.println("   Base de datos: " + meta.getDatabaseProductName());
            System.out.println("   Versión BD: " + meta.getDatabaseProductVersion());
            System.out.println("   Usuario conectado: " + meta.getUserName());
            System.out.println("   URL de conexión: " + meta.getURL());

            // 4. Consulta de ejemplo. Statement y ResultSet van en el
            //    try-con-recursos para que se cierren siempre (criterio 2i).
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, salario FROM empleados ORDER BY id")) {

                System.out.println();
                System.out.println("=== EMPLEADOS ===");
                while (rs.next()) {
                    // getBigDecimal, no getDouble: el dinero nunca se guarda en coma flotante
                    BigDecimal salario = rs.getBigDecimal("salario");
                    System.out.printf("ID: %d | Nombre: %-14s | Salario: %10.2f%n",
                            rs.getInt("id"), rs.getString("nombre"), salario);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] No se ha podido conectar a la base de datos: " + e.getMessage());
        }
    }
}
