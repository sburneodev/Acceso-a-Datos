package incidencias;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.Objects;

/**
 * Una incidencia del servicio técnico (una línea válida del CSV).
 *
 * <p>Es un POJO: campos privados, constructor vacío, getters y setters.
 * Las anotaciones de JAXB solo dicen cómo se ve en XML; la clase sigue siendo normal.</p>
 *
 * <p>XML que genera (dentro de {@code <incidencias>}):</p>
 * <pre>{@code
 * <incidencia id="1">
 *     <fecha>2026-11-02</fecha>
 *     <aula>A101</aula>
 *     <equipo>PC-01</equipo>
 *     <descripcion>No enciende la pantalla</descripcion>
 *     <estado>ABIERTA</estado>
 * </incidencia>
 * }</pre>
 */
@XmlRootElement(name = "incidencia")          // puede ser raíz si se guarda una sola
@XmlAccessorType(XmlAccessType.FIELD)         // JAXB lee los CAMPOS (no necesita getters)
@XmlType(propOrder = {"fecha", "aula", "equipo", "descripcion", "estado"}) // orden de las etiquetas hijas
public class IncidenciaPojo {

    @XmlAttribute                             // ADAPTAR: si el id debe ser etiqueta, cambia a @XmlElement y añádelo al propOrder
    private int id;

    @XmlElement private String fecha;         // ADAPTAR: los nombres de etiqueta = nombres de campo; usa @XmlElement(name="...") para renombrar
    @XmlElement private String aula;
    @XmlElement private String equipo;
    @XmlElement private String descripcion;
    @XmlElement private String estado;        // "ABIERTA" o "CERRADA"

    /** ⚠ OBLIGATORIO para JAXB: crea el objeto con él al hacer unmarshal.
     *  Si falta, falla al crear el JAXBContext (también al GUARDAR). */
    public IncidenciaPojo() {
    }

    /**
     * Crea una incidencia con todos sus datos.
     *
     * @param id          identificador entero
     * @param fecha       fecha tal como viene en el CSV
     * @param aula        aula
     * @param equipo      equipo afectado
     * @param descripcion descripción del problema
     * @param estado      {@code ABIERTA} o {@code CERRADA}
     */
    public IncidenciaPojo(int id, String fecha, String aula, String equipo, String descripcion, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.aula = aula;
        this.equipo = equipo;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    /** @return {@code true} si el estado es CERRADA (ignorando mayúsculas y espacios) */
    public boolean isCerrada() {
        return estado != null && estado.trim().equalsIgnoreCase("CERRADA");
    }

    /** @return {@code true} si el estado es ABIERTA (ignorando mayúsculas y espacios) */
    public boolean isAbierta() {
        return estado != null && estado.trim().equalsIgnoreCase("ABIERTA");
    }

    // ---------------- getters y setters ----------------

    /** @return el id */
    public int getId() { return id; }
    /** @param id el id */
    public void setId(int id) { this.id = id; }
    /** @return la fecha */
    public String getFecha() { return fecha; }
    /** @param fecha la fecha */
    public void setFecha(String fecha) { this.fecha = fecha; }
    /** @return el aula */
    public String getAula() { return aula; }
    /** @param aula el aula */
    public void setAula(String aula) { this.aula = aula; }
    /** @return el equipo */
    public String getEquipo() { return equipo; }
    /** @param equipo el equipo */
    public void setEquipo(String equipo) { this.equipo = equipo; }
    /** @return la descripción */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion la descripción */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    /** @return el estado */
    public String getEstado() { return estado; }
    /** @param estado el estado */
    public void setEstado(String estado) { this.estado = estado; }

    // equals/hashCode: permiten comparar listas enteras en los tests con assertEquals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncidenciaPojo otra)) return false;
        return id == otra.id
                && Objects.equals(fecha, otra.fecha)
                && Objects.equals(aula, otra.aula)
                && Objects.equals(equipo, otra.equipo)
                && Objects.equals(descripcion, otra.descripcion)
                && Objects.equals(estado, otra.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fecha, aula, equipo, descripcion, estado);
    }

    @Override
    public String toString() {
        return id + ";" + fecha + ";" + aula + ";" + equipo + ";" + descripcion + ";" + estado;
    }
}
