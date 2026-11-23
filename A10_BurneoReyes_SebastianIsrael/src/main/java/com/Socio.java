package com;

import java.time.LocalDate;

public class Socio {
public int id;
public String nombre;
public String telefono;
public LocalDate fechaAlta;

    public Socio(int id, String nombre, String telefono, LocalDate fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.fechaAlta = fechaAlta;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}
