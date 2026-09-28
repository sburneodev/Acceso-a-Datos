package com.dam.xml;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "empleados")
@XmlAccessorType(XmlAccessType.FIELD)
public class Empleados {
    @XmlElement(name = "empleado")          // sin (name=...) la etiqueta se llamaria "lista"
    private List<Empleado> lista = new ArrayList<>();

    public List<Empleado> getLista() { return lista; }
    public void setLista(List<Empleado> lista) { this.lista = lista; }
}
