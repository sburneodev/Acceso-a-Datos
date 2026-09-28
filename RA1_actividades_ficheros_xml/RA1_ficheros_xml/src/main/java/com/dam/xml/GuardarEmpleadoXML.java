package com.dam.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuardarEmpleadoXML {
    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("datos"));   // la carpeta no se crea sola

            Empleado e = new Empleado("E001", "Sofía Ramírez", "Desarrollo");

            JAXBContext context = JAXBContext.newInstance(Empleado.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(e, new File("datos/empleado.xml"));

            System.out.println("Empleado serializado en datos/empleado.xml");
        } catch (Exception ex) {
            System.out.println("Error al guardar: " + ex);
        }
    }
}
