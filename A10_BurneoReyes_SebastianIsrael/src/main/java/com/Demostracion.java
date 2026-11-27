package com;

import com.DAO.LibroDAO;
import com.DAO.PrestamoDAO;
import com.DAO.SocioDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Recorre la aplicación de principio a fin sin pedir nada por teclado.
 * Funciona igual con MySQL y con H2: solo cambia db.properties.
 */
public class Demostracion {

    public static void main(String[] args) {
        try {
            titulo("1. Estructura: se borra si existía y se crea de nuevo");
            Estructura.borrarTablas();
            Estructura.crearTablas();
            System.out.println("Base de datos: " + (Conexiones.esH2() ? "H2" : "MySQL"));

            titulo("2. Alta de dos socios y carga del catálogo en un lote");
            int ana = SocioDAO.altaSocio(new Socio(null,"Ana López", "600111222", LocalDate.now()));
            System.out.println("Socio dado de alta: Ana López, id " + ana);
            int luis = SocioDAO.altaSocio(new Socio(null,"Luis Martín", "600333444", LocalDate.now()));
            System.out.println("Socio dado de alta: Luis Martín, id " + luis);

            int cargados = LibroDAO.cargarCatalogo(Main.catalogo);
            System.out.println("Libros cargados en un lote: " + cargados);

            titulo("3. Libros disponibles");
            List<Libro> disponibles = LibroDAO.librosDisponibles();
            for (Libro l : disponibles) {
                System.out.println(l.getId() + " | " + l.getTitulo() + " | " + l.getAutor()
                        + " | " + l.getAnio() + " | ejemplares: " + l.getEjemplares());
            }
            // Libros que se usan en la demostración
            int idUnico = buscarId(disponibles, "El señor de los anillos");   // tiene 1 ejemplar
            int idFallo = buscarId(disponibles, "Fahrenheit 451");            // tiene 5 ejemplares

            titulo("4. Préstamo correcto");
            PrestamoDAO.prestar(ana, idUnico, false);
            System.out.println("Préstamo hecho: socio " + ana + ", libro " + idUnico);
            System.out.println("Ejemplares que quedan del libro " + idUnico + ": " + LibroDAO.ejemplaresDe(idUnico));

            titulo("5. Prestar un libro sin ejemplares");
            try {
                PrestamoDAO.prestar(luis, idUnico, false);
                System.out.println("ERROR: se ha prestado un libro sin ejemplares");
            } catch (SQLException e) {
                System.out.println("No se ha podido prestar: " + e.getMessage());
            }
            System.out.println("Ejemplares del libro " + idUnico + ": " + LibroDAO.ejemplaresDe(idUnico)
                    + " (no ha bajado de 0)");

            titulo("6. Fallo provocado a mitad de un préstamo");
            int ejemplaresAntes = LibroDAO.ejemplaresDe(idFallo);
            int prestamosAntes = PrestamoDAO.getPrestamosSocios(luis).size();
            System.out.println("Antes:   ejemplares del libro " + idFallo + " = " + ejemplaresAntes
                    + ", préstamos del socio " + luis + " = " + prestamosAntes);
            try {
                PrestamoDAO.prestar(luis, idFallo, true);   // falla después del UPDATE y antes del INSERT
            } catch (SQLException e) {
                System.out.println("El préstamo ha fallado: " + e.getMessage());
            }
            int ejemplaresDespues = LibroDAO.ejemplaresDe(idFallo);
            int prestamosDespues = PrestamoDAO.getPrestamosSocios(luis).size();
            System.out.println("Después: ejemplares del libro " + idFallo + " = " + ejemplaresDespues
                    + ", préstamos del socio " + luis + " = " + prestamosDespues);
            if (ejemplaresAntes == ejemplaresDespues && prestamosAntes == prestamosDespues) {
                System.out.println("La base ha quedado exactamente como estaba: el rollback ha deshecho el UPDATE");
            } else {
                System.out.println("ERROR: ha quedado rastro del préstamo fallido");
            }

            titulo("7. Procedimiento: ¿cuántos libros tiene sin devolver?");
            System.out.println("El socio " + ana + " tiene " + PrestamoDAO.prestamosActivos(ana)
                    + " libro(s) sin devolver");

            titulo("8. Devolución y préstamos del socio");
            int idPrestamo = PrestamoDAO.getPrestamosSocios(ana).get(0).getId();
            PrestamoDAO.devolver(idPrestamo);
            System.out.println("Préstamo " + idPrestamo + " devuelto");
            System.out.println("Ejemplares del libro " + idUnico + ": " + LibroDAO.ejemplaresDe(idUnico));
            for (Prestamo p : PrestamoDAO.getPrestamosSocios(ana)) {
                System.out.println(p.getId() + " | " + p.getTitulo() + " | prestado el " + p.getFechaPrestamo()
                        + " | " + (p.isDevuelto() ? "Devuelto el " + p.getFechaDevolucion() : "Pendiente"));
            }
            System.out.println("Ahora el socio " + ana + " tiene " + PrestamoDAO.prestamosActivos(ana)
                    + " libro(s) sin devolver");

        } catch (SQLException e) {
            System.out.println("Error inesperado en la demostración: " + e.getMessage());
        } finally {
            titulo("9. Se recoge la estructura");
            Estructura.borrarTablas();
            Conexiones.cerrar();
            System.out.println("Pool cerrado. Fin de la demostración");
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    private static int buscarId(List<Libro> libros, String titulo) {
        for (Libro l : libros) {
            if (l.getTitulo().equals(titulo)) {
                return l.getId();
            }
        }
        throw new IllegalStateException("No se encuentra el libro " + titulo);
    }
}