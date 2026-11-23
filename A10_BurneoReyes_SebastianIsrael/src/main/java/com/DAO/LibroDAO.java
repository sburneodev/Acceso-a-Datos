package com.DAO;

import com.Conexiones;
import com.Libro;
import com.Socio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    public static int altaLibro(Libro libro) throws SQLException {
        String sql="INSERT INTO libros(titulo,autor,anio,ejemplares) VALUES(?,?,?,?);";

        try(Connection con = Conexiones.getConexion();
            PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)){
        ps.setString(1, libro.getTitulo());
        ps.setString(2, libro.getAutor());
        ps.setInt(3, libro.getAnio());
        ps.setInt(4, libro.getEjemplares());
        ps.executeUpdate();
        try(ResultSet rs = ps.getGeneratedKeys()){
            if(rs.next()){
                return  rs.getInt(1);
            }
            throw new SQLException("La base no ha devuelto el id generado");
            }
        }
    }

    public static int bajaLibro(int id) throws SQLException {
        String sql="DELETE FROM libros WHERE id=?";
        try(Connection con = Conexiones.getConexion();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);
            return ps.executeUpdate();
            }
        }

         /*Añadir al main esto cuando vaya a usarse
    *
    * try {
    int filas = LibroDAO.bajaLibro(id);
    if (filas == 0) {
        System.out.println("No se ha borrado nada: el libro " + id + " no existía");
    } else {
        System.out.println("Filas borradas: " + filas);
    }
} catch (SQLException e) {
    if (e.getErrorCode() == 1451) {
        System.out.println("No se puede borrar el libro " + id + ": tiene préstamos");
    } else {
        System.out.println("Error al borrar el libro: " + e.getMessage());
    }
}
    * */

    public static int cargarCatalogo(List<Libro> libros) throws SQLException {
        String sql="Select count(*) from libros;";
        try(Connection con=Conexiones.getConexion();
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs=ps.executeQuery()){
            rs.next();
            if(rs.getInt(1)!=0){
                return 0;
            }
        }
        String sql2 = "INSERT INTO libros(titulo, autor, anio, ejemplares) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexiones.getConexion();
             PreparedStatement ps = con.prepareStatement(sql2)) {

            for (Libro libro : libros) {
                ps.setString(1, libro.getTitulo());
                ps.setString(2, libro.getAutor());
                ps.setInt(3, libro.getAnio());
                ps.setInt(4, libro.getEjemplares());
                ps.addBatch();
            }

            int[] resultados = ps.executeBatch();
            return resultados.length;
        }
    }



    public static List<Libro> librosDisponibles() throws SQLException {
        List<Libro> libros = new ArrayList<>();
        String sql="SELECT * FROM libros WHERE ejemplares>0";
        try(Connection con = Conexiones.getConexion();
        PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs= ps.executeQuery()){
            while(rs.next()){
                int id=rs.getInt("id");
                String titulo=rs.getString("titulo");
                String autor=rs.getString("autor");
                int anio=rs.getInt("anio");
                int ejemplares=rs.getInt("ejemplares");
                Libro l=new Libro(id,titulo,autor,anio,ejemplares);
                libros.add(l);
            }
        }
        return libros;
    }


    }




