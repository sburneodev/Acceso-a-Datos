package com;

import java.time.LocalDate;
import java.util.List;

public class Prestamo {
    private int id;
    private int idSocio;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String titulo;


    public Prestamo(int id, int idSocio, int idLibro, String titulo, LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        this.id = id;
        this.idSocio = idSocio;
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean isDevuelto() {
        return fechaDevolucion != null;
    }

    public int getId() {
        return id;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public int getIdSocio() {
        return idSocio;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public void setIdSocio(int idSocio) {
        this.idSocio = idSocio;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
}
