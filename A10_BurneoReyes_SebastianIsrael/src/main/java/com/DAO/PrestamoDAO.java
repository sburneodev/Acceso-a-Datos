package com.DAO;

import com.Conexiones;
import com.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    public static List<Prestamo> getPrestamosSocios(int idSocio) throws SQLException {
        String sql = "SELECT p.id AS id_prestamo, p.id_socio AS id_socio, p.id_libro AS id_libro, "
                + "l.titulo AS titulo, p.fecha_prestamo AS fecha_prestamo, p.fecha_devolucion AS fecha_devolucion "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "WHERE p.id_socio = ?";
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection con = Conexiones.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setInt(1, idSocio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate fechaDev = null;
                    if (rs.getDate("fecha_devolucion") != null) {
                        fechaDev = rs.getDate("fecha_devolucion").toLocalDate();
                    }

                    Prestamo p = new Prestamo(
                            rs.getInt("id_prestamo"),
                            rs.getInt("id_socio"),
                            rs.getInt("id_libro"),
                            rs.getString("titulo"),
                            rs.getDate("fecha_prestamo").toLocalDate(),
                            fechaDev);
                    prestamos.add(p);
                }
            }

            return prestamos;

        }
    }

    public static void prestar(int idSocio, int idLibro,boolean fallo) throws SQLException {
        String sqlPrestamo = "INSERT INTO prestamos(id_socio, id_libro, fecha_prestamo) VALUES (?, ?, ?)";
        String sqlUpdate = "UPDATE libros SET ejemplares=ejemplares-1 WHERE id=? AND ejemplares>0";
        try (Connection con = Conexiones.getConexion()) {
            con.setAutoCommit(false);
            try  (PreparedStatement psUpdateLibro = con.prepareStatement(sqlUpdate);
                  PreparedStatement psPrestamo = con.prepareStatement(sqlPrestamo);){

                psUpdateLibro.setInt(1, idLibro);
                int rows = psUpdateLibro.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("El libro " + idLibro + " no existe o no tiene ejemplares disponibles");
                }
                // Fallo a propósito: el UPDATE ya se ha hecho y el INSERT todavía no.
                // El catch hace rollback y la base queda como estaba.
                if (fallo) {
                    throw new SQLException("Fallo provocado a propósito entre los dos cambios");
                }
                psPrestamo.setInt(1, idSocio);
                psPrestamo.setInt(2, idLibro);
                psPrestamo.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                psPrestamo.execute();
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

    public static int prestamosActivos(int idSocio) throws SQLException{
        int cantidadActivos=-1;
        if (Conexiones.esH2()) {
            // H2: la misma consulta que hace el procedimiento, lanzada desde Java
            String sql = "SELECT COUNT(*) FROM prestamos WHERE id_socio = ? AND fecha_devolucion IS NULL";
            try (Connection con = Conexiones.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSocio);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                   return rs.getInt(1);
                }
            }
        }
        try(Connection con=Conexiones.getConexion();
        CallableStatement cs=con.prepareCall("{call prestamos_activos(?,?)}")){
            cs.setInt(1,idSocio);
            cs.registerOutParameter(2,Types.INTEGER);
            cs.execute();

            cantidadActivos=cs.getInt(2);

        }
        return cantidadActivos;
    }

}
