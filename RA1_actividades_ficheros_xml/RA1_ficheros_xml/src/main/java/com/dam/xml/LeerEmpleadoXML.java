package com.dam.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class LeerEmpleadoXML {
    public static void main(String[] args) {
        try {
            JAXBContext context = JAXBContext.newInstance(Empleado.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            Empleado e = (Empleado) unmarshaller.unmarshal(new File("datos/empleado.xml"));

            // Getters, NO e.id: los campos son private
            System.out.println("ID: " + e.getId());
            System.out.println("Nombre: " + e.getNombre());
            System.out.println("Departamento: " + e.getDepartamento());
        } catch (Exception ex) {
            System.out.println("Error al leer: " + ex);
        }
    }
}
