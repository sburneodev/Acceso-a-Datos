package incidencias;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Convierte una lista de incidencias a XML y de XML a lista, con JAXB.
 *
 * <p>Siempre se pasa por los objetos: CSV → {@code List<Incidencia>} → XML.</p>
 *
 * <p>Las tres piezas de JAXB:</p>
 * <ul>
 *   <li>{@link JAXBContext}: se crea con la clase RAÍZ ({@link Incidencias}); analiza sus anotaciones.</li>
 *   <li>{@link Marshaller}: objetos → XML.</li>
 *   <li>{@link Unmarshaller}: XML → objetos.</li>
 * </ul>
 */
public class ConversorXml {

    /**
     * Guarda las incidencias en un XML con sangría y en UTF-8.
     * Crea las carpetas que falten.
     *
     * @param incidencias lista a guardar
     * @param xml         ruta del fichero XML de salida
     * @throws JAXBException si JAXB no puede convertir o escribir (clase mal anotada, sin constructor vacío...)
     * @throws IOException   si no se puede crear la carpeta destino
     */
    public void guardar(List<IncidenciaPojo> incidencias, Path xml) throws JAXBException, IOException {  // ADAPTAR: nombre/firma
        Path carpeta = xml.toAbsolutePath().getParent();        // toAbsolutePath: evita null si la ruta no tiene carpeta
        Files.createDirectories(carpeta);

        JAXBContext contexto = JAXBContext.newInstance(Incidencias.class);
        Marshaller marshaller = contexto.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);   // = "jaxb.formatted.output": XML indentado
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");        // = "jaxb.encoding"
        marshaller.marshal(new Incidencias(incidencias), xml.toFile());
    }

    /**
     * Lee un XML generado por {@link #guardar} y devuelve sus incidencias.
     *
     * @param xml ruta del fichero XML
     * @return lista de incidencias en el orden del XML
     * @throws JAXBException si el fichero no existe, no es XML válido o no encaja con las clases
     */
    public List<IncidenciaPojo> leer(Path xml) throws JAXBException {     // ADAPTAR: nombre/firma
        JAXBContext contexto = JAXBContext.newInstance(Incidencias.class);
        Unmarshaller unmarshaller = contexto.createUnmarshaller();
        Incidencias raiz = (Incidencias) unmarshaller.unmarshal(xml.toFile());   // cast a la clase raíz
        return raiz.getLista();
    }
}
