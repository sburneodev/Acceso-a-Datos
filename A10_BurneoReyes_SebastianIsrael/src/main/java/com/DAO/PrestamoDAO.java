package com.DAO;

import com.Conexiones;
import com.Prestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrestamoDAO {

    public static List<Prestamo> prestamosSocios(int idSocio) throws SQLException {
        String sql = "select p.id, p.id_socio, p.id_libro, l.titulo, p.fecha_prestamo, p.fecha_devolucion " +
                "from prestamos p JOIN libros l ON l.id=p.id_libro where id_socio=?";
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection con = Conexiones.getConexion();

             PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setInt(1, idSocio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate fechaDev;
                    if (rs.getDate("p.fecha_devolucion") != null) {
                        fechaDev = rs.getDate("p.fecha_devolucion").toLocalDate();
                    } else {
                        fechaDev = null;
                    }

                    Prestamo p = new Prestamo(rs.getInt("p.id"), rs.getInt("p.id_socio"),
                            rs.getInt("p.id_libro"), rs.getString("l.titulo"),
                            rs.getDate("p.fecha_prestamo").toLocalDate(),
                            fechaDev);
                    prestamos.add(p);

                }
            }

            return prestamos;

        }
    }
}
