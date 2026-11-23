package com;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Estructura {

public static void crearTablas(){
    try (Connection con = Conexiones.getConexion();
            Statement st=con.createStatement()){
        st.addBatch("CREATE TABLE IF NOT EXISTS socios (\n" +
                "    id          INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    nombre      VARCHAR(100) NOT NULL,\n" +
                "    telefono    VARCHAR(15),\n" +
                "    fecha_alta  DATE NOT NULL\n" +
                ") ENGINE=InnoDB");
        st.addBatch("CREATE TABLE IF NOT EXISTS libros (\n" +
                "    id          INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    titulo      VARCHAR(200) NOT NULL,\n" +
                "    autor       VARCHAR(100) NOT NULL,\n" +
                "    anio        INT,\n" +
                "    ejemplares  INT NOT NULL DEFAULT 1 CHECK (ejemplares >= 0)\n" +
                ") ENGINE=InnoDB");
        st.addBatch("CREATE TABLE IF NOT EXISTS prestamos (\n" +
                "    id                INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    id_socio          INT NOT NULL,\n" +
                "    id_libro          INT NOT NULL,\n" +
                "    fecha_prestamo    DATE NOT NULL,\n" +
                "    fecha_devolucion  DATE NULL,\n" +
                "    CONSTRAINT fk_prestamos_socio FOREIGN KEY (id_socio) REFERENCES socios(id),\n" +
                "    CONSTRAINT fk_prestamos_libro FOREIGN KEY (id_libro) REFERENCES libros(id)\n" +
                ") ENGINE=InnoDB");
        st.executeBatch();
        System.out.println("Tablas creadas");
    }catch (SQLException e){
        System.out.println("Error al crear las tablas: "+e.getMessage());
    }
}

public static  void borrarTablas(){
    try(Connection con=Conexiones.getConexion();
    Statement st=con.createStatement()){
        st.addBatch("DROP TABLE IF EXISTS prestamos");
        st.addBatch("DROP TABLE IF EXISTS libros");
        st.addBatch("DROP TABLE IF EXISTS socios");
        st.executeBatch();
        System.out.println("Tablas borrado");
    }catch (SQLException e){
        System.out.println("Error al borrar las tablas: "+e.getMessage());
    }
}


}
