package incidencias;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase contenedora para JAXB: la raíz {@code <incidencias>} con una lista dentro.
 *
 * <p>JAXB no sabe guardar un {@code List} suelto como raíz: necesita una clase
 * con {@code @XmlRootElement} que tenga la lista como campo.</p>
 */
@XmlRootElement(name = "incidencias")         // ADAPTAR: nombre de la etiqueta raíz
@XmlAccessorType(XmlAccessType.FIELD)
public class Incidencias {

    @XmlElement(name = "incidencia")          // nombre de CADA elemento de la lista (sin name saldría <lista>)
    private List<IncidenciaPojo> lista = new ArrayList<>();

    /** ⚠ OBLIGATORIO para JAXB (también en la clase contenedora). */
    public Incidencias() {
    }

    /** @param lista incidencias que contendrá */
    public Incidencias(List<IncidenciaPojo> lista) {
        this.lista = new ArrayList<>(lista);
    }

    /** @return la lista de incidencias (nunca {@code null}) */
    public List<IncidenciaPojo> getLista() {
        return lista;
    }

    /** @param lista la lista de incidencias */
    public void setLista(List<IncidenciaPojo> lista) {
        this.lista = lista;
    }
}
