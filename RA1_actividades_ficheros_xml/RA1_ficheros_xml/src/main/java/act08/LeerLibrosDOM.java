package act08;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

import java.io.File;

public class LeerLibrosDOM {
    public static void main(String[] args) {
        try {
            File archivo = new File("datos/libros.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Endurecimiento contra XXE (ver Tema 7.1)
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);

            NodeList lista = doc.getElementsByTagName("libro");
            for (int i = 0; i < lista.getLength(); i++) {
                Element libro = (Element) lista.item(i);
                String titulo = libro.getElementsByTagName("titulo").item(0).getTextContent();
                String autor = libro.getElementsByTagName("autor").item(0).getTextContent();
                System.out.println("Libro: " + titulo + ", Autor: " + autor);
            }

        } catch (Exception e) {
            System.out.println("Error DOM: " + e);
        }
    }
}
