import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Convierte ficheros de pedidos entre los formatos CSV, XML y JSON.
 *
 * <p>El formato de entrada se deduce de la extensión del fichero y el de salida se indica
 * con la opción {@code --a}. El resultado se escribe en la misma carpeta que la entrada,
 * con el sufijo {@code _convertido}.</p>
 *
 * <p>Uso:</p>
 * <pre>
 * java Conversor.java pedidos.csv --a json
 * </pre>
 *
 * <p>Los errores se muestran al usuario con un mensaje breve y se registran con todo
 * el detalle en {@code conversor.log}.</p>
 *
 * @author Sebastián Israel Burneo Reyes
 * @version 1.0
 * @see Pedido
 * @see Cliente
 * @see LineaPedido
 */
public class Conversor{

    /**
     * Punto de entrada del programa.
     *
     * <p>Comprueba los argumentos, lee el fichero de entrada con el lector que corresponde
     * a su extensión y escribe la salida en el formato pedido. Si hay un error de datos o
     * de entrada/salida, termina con código 1.</p>
     *
     * @param args tres argumentos: el fichero de entrada, {@code --a} y el formato de salida
     *             ({@code csv}, {@code xml} o {@code json})
     */
    public static void main(String[] args) {
        try{
            if (args.length != 3 || !args[1].equals("--a")) {
                System.err.println("Uso: java Conversor <fichero> --a <csv|xml|json>");
                return;
            }
            String fichero = args[0];
            String formato=args[2];
            Path p=Path.of(fichero);
            if (!Files.exists(p)) {
                System.err.println("No encuentro el fichero '" + fichero + "'. Revisa el nombre y la ruta.");
                registrar("Fichero de entrada no existe: " + fichero, null);
                System.exit(1);
            }

            List<Pedido> pedidos;
            if(fichero.toLowerCase().endsWith(".csv")){
                pedidos=leerCSV(p);
            }else if(fichero.toLowerCase().endsWith(".xml")){
                pedidos=leerXML(p);
            }else if(fichero.toLowerCase().endsWith(".json")){
                pedidos=leerJson(p);
            }else{
                throw new IllegalArgumentException("El fichero: "+fichero+" no tiene uno de los formatos admitidos.");
            }
            String nombre = p.getFileName().toString();
            String base = nombre.substring(0, nombre.lastIndexOf('.'));
            // Si la entrada ya es una salida anterior, se quita el sufijo para no generar
            // nombres como pedidos_convertido_convertido.xml al encadenar conversiones.
            if (base.endsWith("_convertido")) {
                base = base.substring(0, base.length() - "_convertido".length());
            }
            Path salida = p.resolveSibling(base + "_convertido." + formato.toLowerCase());
            // Al quitar el sufijo, "x_convertido.csv --a csv" daría como salida el mismo fichero
            // que se está leyendo: se impide para no sobrescribir la entrada.
            if (salida.equals(p)) {
                System.err.println("La salida coincide con la entrada; elige otro formato.");
                return;
            }

            switch (formato.toLowerCase()) {
                case "csv" -> escribirCSV(pedidos, salida);
                case "xml" -> escribirXML(pedidos, salida);
                case "json" -> escribirJson(pedidos, salida);
                default -> System.err.println("Formato de salida no soportado: " + formato);
            }
        } catch (UncheckedIOException e) {
            System.err.println("No he podido leer o escribir un fichero. Comprueba que existe, "
                    + "que tienes permisos y que la carpeta es correcta. (Más detalle en conversor.log)");
            registrar("Error de E/S: " + e.getMessage(), e);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("El contenido del fichero no es válido: " + e.getMessage()
                    + " (Más detalle en conversor.log)");
            registrar("Datos no válidos: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Lee un fichero CSV de pedidos.
     *
     * <p>La primera línea tiene que ser exactamente la cabecera
     * {@code numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario}.
     * Cada fila es una línea de pedido, y las filas con el mismo {@code numero} se agrupan
     * en un único pedido. Una fila con el artículo vacío representa un pedido sin líneas.
     * Las líneas en blanco se ignoran.</p>
     *
     * @param p ruta del fichero CSV, codificado en UTF-8
     * @return los pedidos en el mismo orden en que aparecen en el fichero
     * @throws IllegalArgumentException si el fichero está vacío, la cabecera no es la esperada,
     *         una fila no tiene 7 campos o algún número o fecha está mal formado
     * @throws UncheckedIOException si no se puede leer el fichero
     */
    static List<Pedido> leerCSV(Path p) {
        // LinkedHashMap y no HashMap: hay que agrupar por número y además conservar el orden
        // del fichero, para que una conversión de ida y vuelta deje el CSV idéntico.
        LinkedHashMap<Integer, Pedido> mapa = new LinkedHashMap<>();
        try (BufferedReader bf = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String cabecera = bf.readLine();
            if (cabecera == null) {
                throw new IllegalArgumentException("El fichero CSV está vacío");
            }
            if (!cabecera.trim().equals("numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario")) {
                throw new IllegalArgumentException("El fichero no tiene la cabecera de CSV esperada");
            }

            String linea;
            int nLinea = 1;
            while ((linea = bf.readLine()) != null) {
                nLinea++;
                if (linea.isBlank()) continue;

                String[] campos = partirCsv(linea);
                if (campos.length != 7) {
                    throw new IllegalArgumentException("Línea " + nLinea + ": se esperaban 7 campos y hay " + campos.length);
                }
                try {
                    int numero = Integer.parseInt(campos[0].trim());
                    Pedido ped = mapa.get(numero);
                    // Si un número se repite, se usan la fecha y el cliente de la primera fila;
                    // los de las filas siguientes no se comprueban.
                    if (ped == null) {
                        ped = new Pedido(numero,
                                LocalDate.parse(campos[1].trim()),
                                new Cliente(Integer.parseInt(campos[2].trim()), campos[3]),
                                new ArrayList<>());
                        mapa.put(numero, ped);
                    }
                    // Artículo vacío = pedido sin líneas (así lo escribe escribirCSV): se crea
                    // el pedido pero no se le añade ninguna línea.
                    if (!campos[4].isBlank()) {
                        ped.addLinea(new LineaPedido(campos[4],
                                Integer.parseInt(campos[5].trim()),
                                Double.parseDouble(campos[6].trim())));
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    throw new IllegalArgumentException("Línea " + nLinea + ": dato mal formado (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer el CSV: " + e.getMessage(), e);
        }
        return new ArrayList<>(mapa.values());
    }

    /**
     * Parte una línea CSV en campos respetando las comillas dobles.
     *
     * <p>Una coma dentro de comillas forma parte del texto, y dos comillas seguidas
     * ({@code ""}) dentro de comillas representan una comilla literal. Es la operación
     * inversa del entrecomillado que hace {@link #escribirCSV(List, Path)}.</p>
     *
     * <p>No se usa {@code linea.split(",")} porque partiría también las comas que van
     * dentro de un texto entrecomillado, como en {@code "López, Hermanos"}.</p>
     *
     * @param linea una línea del fichero, sin el salto de línea final
     * @return los campos de la línea, sin las comillas que los delimitan
     */
    static String[] partirCsv(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');            // "" dentro de comillas = una comilla literal
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else {
                if (c == '"') entreComillas = true;
                else if (c == ',') {
                    campos.add(actual.toString());
                    actual.setLength(0);
                } else actual.append(c);
            }
        }
        // El último campo no termina en coma, así que hay que añadirlo al salir del bucle.
        // Puede ser vacío: en ",,," salen 4 campos, no 3.
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }

    /**
     * Lee un fichero XML de pedidos.
     *
     * <p>Espera una raíz {@code <pedidos>} con elementos
     * {@code <pedido numero="…" fecha="…">}. Cada pedido contiene un
     * {@code <cliente id="…">nombre</cliente>} y un {@code <lineas>} con elementos
     * {@code <linea articulo="…" unidades="…" precioUnitario="…"/>}.</p>
     *
     * <p>Por seguridad se rechaza cualquier documento que tenga una declaración
     * {@code <!DOCTYPE>}.</p>
     *
     * @param p ruta del fichero XML
     * @return los pedidos en el orden del documento
     * @throws IllegalArgumentException si el XML está mal formado (el mensaje indica la línea
     *         y la columna) o algún atributo numérico o de fecha no es válido
     * @throws UncheckedIOException si no se puede leer el fichero
     */
    static List<Pedido> leerXML(Path p){
        ArrayList<Pedido> ps=new ArrayList<>();

        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Protección contra ataques XXE (XML External Entity): sin estas opciones, un XML con
            // un <!DOCTYPE> malicioso puede hacer que el parser lea ficheros del equipo, como
            // C:\Windows\win.ini, y los meta en los datos. Los pedidos nunca necesitan DOCTYPE,
            // así que se prohíbe del todo.
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document xmlDoc = builder.parse(p.toFile());

            NodeList pedidosXML=xmlDoc.getElementsByTagName("pedido");
            for(int i=0; i<pedidosXML.getLength();i++){
                Element ped= (Element) pedidosXML.item(i);
                int numero=Integer.parseInt(ped.getAttribute("numero"));
                LocalDate fecha=LocalDate.parse(ped.getAttribute("fecha"));
                Element cli = (Element) ped.getElementsByTagName("cliente").item(0);
                Cliente c = new Cliente(
                        Integer.parseInt(cli.getAttribute("id")),
                        cli.getTextContent());
                // Se buscan las <linea> dentro de "ped" y no en todo el documento; si no,
                // cada pedido se quedaría con las líneas de todos los pedidos.
                NodeList lineas=ped.getElementsByTagName("linea");
                ArrayList<LineaPedido> lineasPedidos=new ArrayList<>();
                for (int j=0;j<lineas.getLength();j++){
                    Element lineaNode=(Element) lineas.item(j);
                    String n= lineaNode.getAttribute("articulo");
                    int uni=Integer.parseInt(lineaNode.getAttribute("unidades"));
                    double precio=Double.parseDouble(lineaNode.getAttribute("precioUnitario"));
                    LineaPedido l=new LineaPedido(n,uni,precio);
                    lineasPedidos.add(l);
                }
                Pedido pedido=new Pedido(numero,fecha,c,lineasPedidos);
                ps.add(pedido);
            }

        }catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SAXException e) {
            if (e instanceof SAXParseException spe) {
                throw new IllegalArgumentException("XML mal formado en la línea " + spe.getLineNumber()
                        + ", columna " + spe.getColumnNumber(), e);
            }
            throw new IllegalArgumentException("XML mal formado", e);
        }catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Dato mal formado en el XML: " + e.getMessage(), e);
        }
        return ps;
    }

    /**
     * Lee un fichero JSON de pedidos.
     *
     * <p>La raíz tiene que ser un array de objetos con las claves {@code numero},
     * {@code fecha}, {@code cliente} (un objeto con {@code id} y {@code nombre}) y
     * {@code lineas} (un array, que puede estar vacío, de objetos con {@code articulo},
     * {@code unidades} y {@code precioUnitario}).</p>
     *
     * <p><b>Limitación conocida:</b> si falta una clave o un valor no es del tipo esperado,
     * se lanza {@link ClassCastException} o {@link NullPointerException}, que no se capturan.</p>
     *
     * @param p ruta del fichero JSON, codificado en UTF-8
     * @return los pedidos en el orden del array
     * @throws IllegalArgumentException si el JSON está mal formado o la fecha no es válida
     * @throws UncheckedIOException si no se puede leer el fichero
     */
    @SuppressWarnings("unchecked")
    static List<Pedido> leerJson(Path p) {
        try {
            String texto = Files.readString(p, StandardCharsets.UTF_8);
            List<Object> raiz = (List<Object>) new JsonParser(texto).parse();

            List<Pedido> pedidos = new ArrayList<>();
            for (Object o : raiz) {
                Map<String, Object> n = (Map<String, Object>) o;
                // JsonParser devuelve todos los números como Double, por eso se hace intValue().
                // Ojo: intValue() no avisa si el valor no cabe en un int (2147483648 se queda en
                // 2147483647) ni si tiene decimales (2.9 se queda en 2).
                int numero = ((Number) n.get("numero")).intValue();
                LocalDate fecha = LocalDate.parse((String) n.get("fecha"));

                Map<String, Object> cli = (Map<String, Object>) n.get("cliente");
                Cliente c = new Cliente(((Number) cli.get("id")).intValue(), (String) cli.get("nombre"));

                Pedido pedido = new Pedido(numero, fecha, c, new ArrayList<>());
                for (Object lo : (List<Object>) n.get("lineas")) {
                    Map<String, Object> l = (Map<String, Object>) lo;
                    pedido.addLinea(new LineaPedido(
                            (String) l.get("articulo"),
                            ((Number) l.get("unidades")).intValue(),
                            ((Number) l.get("precioUnitario")).doubleValue()));
                }
                pedidos.add(pedido);
            }
            return pedidos;
        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer el JSON: " + e.getMessage(), e);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Dato mal formado en el XML: " + e.getMessage(), e);
        }
    }

    /**
     * Escribe los pedidos en un fichero CSV con codificación UTF-8.
     *
     * <p>Se escribe una fila por cada línea de pedido, repitiendo en cada fila los datos del
     * pedido y del cliente. Un pedido sin líneas se escribe con las tres últimas columnas
     * vacías. Los textos que contienen comas, comillas o saltos de línea van entre comillas
     * dobles, con las comillas interiores duplicadas.</p>
     *
     * @param pedidos pedidos que se van a escribir
     * @param salida  ruta del fichero de salida; si ya existe, se sobrescribe
     * @throws UncheckedIOException si no se puede escribir el fichero
     */
    static void escribirCSV(List<Pedido> pedidos, Path salida) {
        // Solo se entrecomilla cuando hace falta, para que un texto normal quede igual que en
        // el original y la conversión de ida y vuelta devuelva el mismo fichero.
        Function<String, String> campo = s ->
                (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
                        ? "\"" + s.replace("\"", "\"\"") + "\""
                        : s;

        StringBuilder sb = new StringBuilder();
        sb.append("numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario\n");

        for (Pedido ped : pedidos) {
            String inicio = ped.getNumero() + "," + ped.getFecha() + ","
                    + ped.getCliente().getId() + "," + campo.apply(ped.getCliente().getNombre());

            // Sin esta fila, un pedido sin líneas desaparecería al pasar a CSV, porque en este
            // formato las filas son líneas de pedido, no pedidos.
            if (ped.getLineasPedido().isEmpty()) {
                sb.append(inicio).append(",,,\n");
            } else {
                for (LineaPedido lp : ped.getLineasPedido()) {
                    sb.append(inicio).append(",")
                            .append(campo.apply(lp.getArticulo())).append(",")
                            .append(lp.getUnidades()).append(",")
                            .append(lp.getPrecioUnitario()).append("\n");
                }
            }
        }

        try {
            Files.writeString(salida, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al escribir el CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Escribe los pedidos en un fichero JSON con codificación UTF-8.
     *
     * <p>La raíz es un array de pedidos. Los textos (nombre del cliente y artículo) se escapan
     * con {@link #escapar(String)}. Un pedido sin líneas se escribe con {@code "lineas": []}.</p>
     *
     * @param pedidos pedidos que se van a escribir
     * @param salida  ruta del fichero de salida; si ya existe, se sobrescribe
     * @throws UncheckedIOException si no se puede escribir el fichero
     */
    static void escribirJson(List<Pedido> pedidos, Path salida) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < pedidos.size(); i++) {
            Pedido ped = pedidos.get(i);
            sb.append("  {\n");
            sb.append("    \"numero\": ").append(ped.getNumero()).append(",\n");
            sb.append("    \"fecha\": \"").append(ped.getFecha()).append("\",\n");     // LocalDate.toString() ya da AAAA-MM-DD
            sb.append("    \"cliente\": {\"id\": ").append(ped.getCliente().getId())
                    .append(", \"nombre\": \"").append(escapar(ped.getCliente().getNombre()))
                    .append("\"},\n");

            sb.append("    \"lineas\": [");
            List<LineaPedido> lineas = ped.getLineasPedido();
            for (int j = 0; j < lineas.size(); j++) {
                LineaPedido lp = lineas.get(j);
                sb.append(j == 0 ? "\n" : ",\n");        // JSON no admite una coma tras el último elemento
                sb.append("      {\"articulo\": \"").append(escapar(lp.getArticulo()))
                        .append("\", \"unidades\": ").append(lp.getUnidades())
                        .append(", \"precioUnitario\": ").append(lp.getPrecioUnitario()).append("}");
            }
            sb.append(lineas.isEmpty() ? "]\n" : "\n    ]\n");
            sb.append("  }").append(i < pedidos.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]\n");

        try {
            Files.writeString(salida, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al escribir el JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Prepara un texto para que pueda ir entre comillas dentro de un JSON.
     *
     * <p>Escapa las comillas, las barras invertidas y los caracteres de control. Por ejemplo,
     * {@code Tornillo de 3" galvanizado} se convierte en {@code Tornillo de 3\" galvanizado}.
     * Sin este paso, un nombre con comillas cerraría la cadena antes de tiempo y el JSON
     * resultante quedaría mal formado.</p>
     *
     * @param s texto original
     * @return el texto escapado, sin las comillas exteriores
     */
    static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    // El estándar JSON prohíbe cualquier carácter de control (< 0x20) sin escapar,
                    // no solo los anteriores.
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Escribe los pedidos en un fichero XML con codificación UTF-8 e indentado.
     *
     * <p>Genera la estructura que lee {@link #leerXML(Path)}: una raíz {@code <pedidos>} y,
     * por cada pedido, un {@code <cliente>} y un {@code <lineas>}. Si un pedido no tiene
     * líneas, se escribe {@code <lineas/>}. Los caracteres especiales ({@code &}, {@code <},
     * comillas…) los escapa automáticamente la API DOM.</p>
     *
     * @param pedidos pedidos que se van a escribir
     * @param salida  ruta del fichero de salida; si ya existe, se sobrescribe
     * @throws UncheckedIOException si no se puede crear o escribir el documento
     */
    static void escribirXML(List<Pedido> pedidos, Path salida) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document xmlDoc = builder.newDocument();

            Element raiz = xmlDoc.createElement("pedidos");
            xmlDoc.appendChild(raiz);

            for (Pedido ped : pedidos) {

                Element pedido = xmlDoc.createElement("pedido");
                pedido.setAttribute("numero", String.valueOf(ped.getNumero()));
                pedido.setAttribute("fecha", ped.getFecha().toString());

                Element cliente = xmlDoc.createElement("cliente");
                cliente.setAttribute("id", String.valueOf(ped.getCliente().getId()));
                cliente.setTextContent(ped.getCliente().getNombre());
                pedido.appendChild(cliente);

                Element lineas = xmlDoc.createElement("lineas");
                for (LineaPedido lp : ped.getLineasPedido()) {
                    Element linea = xmlDoc.createElement("linea");
                    linea.setAttribute("articulo", lp.getArticulo());
                    linea.setAttribute("unidades", String.valueOf(lp.getUnidades()));
                    linea.setAttribute("precioUnitario", String.valueOf(lp.getPrecioUnitario()));
                    lineas.appendChild(linea);
                }
                pedido.appendChild(lineas);
                raiz.appendChild(pedido);
            }
            // Sin esto, el Transformer añade standalone="no" a la declaración <?xml ...?>.
            xmlDoc.setXmlStandalone(true);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.transform(new DOMSource(xmlDoc), new StreamResult(salida.toFile()));

        } catch (ParserConfigurationException | TransformerException e) {
            // Se envuelve como UncheckedIOException para que main lo trate igual que cualquier
            // otro fallo de escritura y muestre el mismo mensaje al usuario.
            throw new UncheckedIOException("Error al escribir el XML: " + e.getMessage(), new IOException(e));
        }
    }

    /**
     * Analizador JSON mínimo, escrito a mano para no depender de librerías externas como
     * Jackson o Gson.
     *
     * <p>Convierte el texto JSON en objetos de Java: un objeto pasa a {@link Map} (se usa
     * {@link LinkedHashMap} para conservar el orden de las claves), un array a {@link List},
     * una cadena a {@link String}, un número a {@link Double} (todos, también los enteros) y
     * {@code true}, {@code false} y {@code null} a sus equivalentes en Java.</p>
     *
     * <p>Es un analizador recursivo: cada método lee un tipo de valor a partir de la posición
     * actual y deja la posición justo después de él.</p>
     */
    static class JsonParser {
        /** Texto JSON completo. */
        private final String s;
        /** Posición del siguiente carácter que se va a leer. */
        private int i = 0;

        /**
         * Crea un analizador para el texto indicado.
         *
         * @param s texto JSON completo
         */
        JsonParser(String s) { this.s = s; }

        /**
         * Analiza el texto completo.
         *
         * @return el valor raíz: un {@link Map}, una {@link List}, un {@link String},
         *         un {@link Double}, un {@link Boolean} o {@code null}
         * @throws IllegalArgumentException si el JSON está mal formado o hay texto después
         *         del valor raíz; el mensaje indica la posición del error
         */
        Object parse() {
            Object v = valor();
            saltar();
            // Sin esta comprobación se aceptaría "[1,2] basura", ignorando lo que sobra.
            if (i < s.length()) throw error("Texto sobrante");
            return v;
        }

        private Object valor() {
            saltar();
            if (i >= s.length()) throw error("Fin inesperado");
            char c = s.charAt(i);
            if (c == '{') return objeto();
            if (c == '[') return array();
            if (c == '"') return cadena();
            if (s.startsWith("true", i))  { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i))  { i += 4; return null; }
            return numero();
        }

        private Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            saltar();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                saltar();
                String clave = cadena();
                saltar();
                esperar(':');
                m.put(clave, valor());
                saltar();
                char c = s.charAt(i++);
                if (c == '}') break;
                if (c != ',') throw error("Se esperaba ',' o '}'");
            }
            return m;
        }

        private List<Object> array() {
            List<Object> l = new ArrayList<>();
            i++;
            saltar();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                l.add(valor());
                saltar();
                char c = s.charAt(i++);
                if (c == ']') break;
                if (c != ',') throw error("Se esperaba ',' o ']'");
            }
            return l;
        }

        private String cadena() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw error("Texto sin cerrar");
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c != '\\') { sb.append(c); continue; }
                char e = s.charAt(i++);
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }   // \u00f1 → ñ
                    default  -> throw error("Escape no válido: \\" + e);
                }
            }
            return sb.toString();
        }

        // Todos los números se devuelven como Double, aunque sean enteros: así basta con un
        // solo tipo, y quien los usa decide con intValue() o doubleValue().
        private Double numero() {
            int ini = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (ini == i) throw error("Valor no válido");
            return Double.parseDouble(s.substring(ini, i));
        }

        private void saltar() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private void esperar(char c) {
            if (i >= s.length() || s.charAt(i) != c) throw error("Se esperaba '" + c + "'");
            i++;
        }

        private RuntimeException error(String msg) {
            return new IllegalArgumentException("JSON mal formado en la posición " + i + ": " + msg);
        }
    }

    /**
     * Fichero de registro de errores. Es una ruta relativa, así que se crea en la carpeta
     * desde la que se ejecuta el programa.
     */
    static final Path LOG = Path.of("conversor.log");

    /**
     * Añade una entrada al fichero de registro {@link #LOG}, con la fecha y la hora y, si
     * se indica, la traza completa de la excepción.
     *
     * <p>Si no se puede escribir el registro, se avisa por la salida de error pero no se
     * interrumpe el programa: un fallo del log no debe ocultar el error original.</p>
     *
     * @param mensaje descripción breve del error
     * @param e       excepción que lo causó, o {@code null} si no hay ninguna
     */
    static void registrar(String mensaje, Exception e) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG.toFile(), true))) {   // true = añadir al final, no sobrescribir
            pw.println(LocalDateTime.now() + " " + mensaje);
            if (e != null) e.printStackTrace(pw);
        } catch (IOException ex) {
            System.err.println("(No se pudo escribir el log: " + ex.getMessage() + ")");
        }
    }
}
