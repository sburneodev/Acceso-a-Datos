package com.dam.xml;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "empleado")
@XmlAccessorType(XmlAccessType.FIELD)
public class Empleado {
    @XmlAttribute
    private String id;

    @XmlElement
    private String nombre;

    @XmlElement
    private String departamento;

    public Empleado() {}

    public Empleado(String id, String nombre, String departamento) {
        this.id = id;
        this.nombre = nombre;
        this.departamento = departamento;
    }

    // Getters OBLIGATORIOS: los campos son private y LeerEmpleadoXML esta en otra clase
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDepartamento() { return departamento; }
}
