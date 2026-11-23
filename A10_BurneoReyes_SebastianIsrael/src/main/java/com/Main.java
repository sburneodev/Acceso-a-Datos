package com;

import com.DAO.LibroDAO;
import com.DAO.PrestamoDAO;
import com.DAO.SocioDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static List<Libro> catalogo = List.of(
            new Libro(null,"Don Quijote de la Mancha", "Miguel de Cervantes", 1605, 3),
            new Libro(null,"Cien años de soledad", "Gabriel García Márquez", 1967, 2),
            new Libro(null,"La sombra del viento", "Carlos Ruiz Zafón", 2001, 4),
            new Libro(null,"1984", "George Orwell", 1949, 2),
            new Libro(null,"El señor de los anillos", "J. R. R. Tolkien", 1954, 1),
            new Libro(null,"Orgullo y prejuicio", "Jane Austen", 1813, 2),
            new Libro(null,"La casa de los espíritus", "Isabel Allende", 1982, 3),
            new Libro(null,"Crimen y castigo", "Fiódor Dostoyevski", 1866, 1),
            new Libro(null,"El nombre de la rosa", "Umberto Eco", 1980, 2),
            new Libro(null,"Fahrenheit 451", "Ray Bradbury", 1953, 5)
    );

    public static void main(String[] args) {
        Estructura.crearTablas();
        try {
            LibroDAO.cargarCatalogo(catalogo);
        }catch (SQLException e){
            System.out.println("Fallo al cargar catalogo: "+e.getMessage());
        }
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1 -> altaSocio();
                case 2 -> altaLibro();
                case 3 -> bajaLibro();
                case 4 -> librosDisponibles();
                case 5 -> prestamosSocio();
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción no válida");
            }
        } while (opcion != 0);

        Conexiones.cerrar();
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== BIBLIOTECA =====");
        System.out.println("1. Alta de socio");
        System.out.println("2. Alta de libro");
        System.out.println("3. Baja de libro");
        System.out.println("4. Libros disponibles");
        System.out.println("5. Préstamos de un socio");
        System.out.println("0. Salir");
    }


    private static void altaSocio() {
        String nombre = leerTexto("Nombre: ");
        String telefono = leerTexto("Teléfono: ");
        Socio socio = new Socio(null,nombre, telefono, LocalDate.now());

        try {
            int id = SocioDAO.altaSocio(socio);
            System.out.println("Socio dado de alta con id " + id);
        } catch (SQLException e) {
            System.out.println("No se pudo dar de alta el socio: " + e.getMessage());
        }
    }

    private static void altaLibro() {
        String titulo = leerTexto("Título: ");
        String autor = leerTexto("Autor: ");
        int anio = leerEntero("Año: ");
        int ejemplares = leerEntero("Ejemplares disponibles: ");
        Libro libro = new Libro(null, titulo, autor, anio, ejemplares);

        try {
            int id = LibroDAO.altaLibro(libro);
            System.out.println("Libro dado de alta con id " + id);
        } catch (SQLException e) {
            System.out.println("No se pudo dar de alta el libro: " + e.getMessage());
        }
    }

    private static void bajaLibro() {
        int id = leerEntero("Id del libro a borrar: ");

        try {
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
    }

    private static void librosDisponibles() {
        try {
            List<Libro> libros = LibroDAO.librosDisponibles();
            if (libros.isEmpty()) {
                System.out.println("No hay libros disponibles");
                return;
            }
            System.out.println("--- Libros disponibles ---");
            for (Libro l : libros) {
                System.out.println(l.getId() + " | " + l.getTitulo() + " | " + l.getAutor()
                        + " | " + l.getAnio() + " | ejemplares: " + l.getEjemplares());
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar los libros: " + e.getMessage());
        }
    }

    private static void prestamosSocio() {
        int idSocio = leerEntero("Id del socio: ");

        try {
            List<Prestamo> prestamos = PrestamoDAO.prestamosSocios(idSocio);
            if (prestamos.isEmpty()) {
                System.out.println("El socio " + idSocio + " no tiene préstamos");
                return;
            }
            System.out.println("--- Préstamos del socio " + idSocio + " ---");
            for (Prestamo p : prestamos) {
                System.out.println(p.getTitulo() + " | prestado el " + p.getFechaPrestamo()
                        + " | " + (p.isDevuelto() ? "Devuelto el " + p.getFechaDevolucion() : "Pendiente"));
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar los préstamos: " + e.getMessage());
        }
    }


    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = sc.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero");
            }
        }
    }

    private static String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = sc.nextLine().trim();
            if (!linea.isEmpty()) {
                return linea;
            }
            System.out.println("No puede estar vacío");
        }
    }
}