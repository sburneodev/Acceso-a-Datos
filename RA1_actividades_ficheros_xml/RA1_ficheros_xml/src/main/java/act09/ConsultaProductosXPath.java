package act09;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import java.io.File;

public class ConsultaProductosXPath {
    public static void main(String[] args) {
        try {
            File archivo = new File("datos/productos.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Endurecimiento contra XXE (ver Tema 7.1)
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);

            XPathFactory xpf = XPathFactory.newInstance();
            xpf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            XPath xpath = xpf.newXPath();

            // Mostrar todos los nombres de productos
            NodeList nombres = (NodeList) xpath.evaluate("//nombre", doc, XPathConstants.NODESET);
            System.out.println("Nombres de productos:");
            for (int i = 0; i < nombres.getLength(); i++) {
                System.out.println("> " + nombres.item(i).getTextContent());
            }

            // Precio del producto llamado "Monitor"
            String precio = xpath.evaluate("//producto[nombre='Monitor']/precio/text()", doc);
            System.out.println("Precio del Monitor: " + precio);

            // ID del producto más barato
            String idBarato = xpath.evaluate("//producto[precio=//precio[not(. > //precio)]]/@id", doc);
            System.out.println("ID del producto más barato: " + idBarato);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
