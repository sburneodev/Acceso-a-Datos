package com.DAO;

import com.Conexiones;
import com.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrestamoDAO {

    public static List<Prestamo> getPrestamosSocios(int idSocio) throws SQLException {
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

    public static void prestar(int idSocio, int idLibro) throws SQLException {
        String sqlPrestamo = "INSERT INTO prestamos(id_socio, id_libro, fecha_prestamo) VALUES (?, ?, ?) ";
        String sqlUpdate = "UPDATE libros SET ejemplares=ejemplares-1 Where id=? AND ejemplares>0;";
        try (Connection con = Conexiones.getConexion()) {
            con.setAutoCommit(false);
            try  (PreparedStatement psUpdateLibro = con.prepareStatement(sqlUpdate);
                  PreparedStatement psPrestamo = con.prepareStatement(sqlPrestamo);){

                psUpdateLibro.setInt(1, idLibro);
                int rows = psUpdateLibro.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new SQLException("El libro " + idLibro + " no tiene ejemplares disponibles");
                }
                psPrestamo.setInt(1, idSocio);
                psPrestamo.setInt(2, idLibro);
                psPrestamo.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                psPrestamo.executeQuery();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);

            }
        }
    }

    public static void devolver(int idPrestamo) throws SQLException{
        String sqlUpdate="UPDATE prestamos SET fecha_devolucion = ? WHERE id = ? AND fecha_devolucion IS NULL;";
        String sqlUpdateLibro="UPDATE libros Set ejemplares=ejemplares+1 WHERE id=(SELECT id_libro FROM prestamos WHERE id=?);";
        try(Connection con=Conexiones.getConexion()){
            con.setAutoCommit(false);
            try(PreparedStatement psUpdate=con.prepareStatement(sqlUpdate);
            PreparedStatement psUpdateLibro= con.prepareStatement(sqlUpdateLibro)){
               psUpdate.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
               psUpdate.setInt(2,idPrestamo);
               int rows=psUpdate.executeUpdate();
               if(rows==0){
                   con.rollback();
                   throw new SQLException("El préstamo " + idPrestamo + " no existe o ya está devuelto");
               }
               psUpdateLibro.setInt(1,idPrestamo);
               psUpdateLibro.executeUpdate();
               con.commit();

            }catch (SQLException e){
                con.rollback();
                throw e;
            }finally {
                con.setAutoCommit(true);

            }

        }

    }

}
