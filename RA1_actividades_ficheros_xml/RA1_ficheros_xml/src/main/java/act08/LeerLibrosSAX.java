package act08;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;

public class LeerLibrosSAX {
    public static void main(String[] args) {
        try {
            File archivo = new File("datos/libros.xml");

            SAXParserFactory factory = SAXParserFactory.newInstance();
            // Endurecimiento contra XXE (ver Tema 7.1)
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);

            SAXParser parser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                // Acumular en characters(), decidir en endElement()
                private final StringBuilder texto = new StringBuilder();

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    texto.setLength(0);
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    texto.append(ch, start, length);
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if (qName.equals("titulo")) {
                        System.out.println("Título: " + texto.toString().trim());
                    } else if (qName.equals("autor")) {
                        System.out.println("Autor: " + texto.toString().trim());
                    }
                    texto.setLength(0);
                }
            };

            parser.parse(archivo, handler);
        } catch (Exception e) {
            System.out.println("Error SAX: " + e);
        }
    }
}
