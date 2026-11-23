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
        String sql = "INSERT INTO libros(titulo, autor, anio, ejemplares) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexiones.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
    /*Hacer esto en el main para el catalogo
    *
    * List<Libro> catalogo = List.of(
    new Libro("Don Quijote de la Mancha", "Miguel de Cervantes", 1605, 3),
    new Libro("Cien años de soledad", "Gabriel García Márquez", 1967, 2),
    new Libro("La sombra del viento", "Carlos Ruiz Zafón", 2001, 4),
    new Libro("1984", "George Orwell", 1949, 2),
    new Libro("El señor de los anillos", "J. R. R. Tolkien", 1954, 1),
    new Libro("Orgullo y prejuicio", "Jane Austen", 1813, 2),
    new Libro("La casa de los espíritus", "Isabel Allende", 1982, 3),
    new Libro("Crimen y castigo", "Fiódor Dostoyevski", 1866, 1),
    new Libro("El nombre de la rosa", "Umberto Eco", 1980, 2),
    new Libro("Fahrenheit 451", "Ray Bradbury", 1953, 5)
);
    * */

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




