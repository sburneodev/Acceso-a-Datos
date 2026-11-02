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
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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

public class Conversor{
    public static void main(String[] args) {
        if (args.length != 3 || !args[1].equals("--a")) {
            System.err.println("Uso: java Conversor <fichero> --a <csv|xml|json>");
            return;
        }
        String fichero = args[0];
        String formato=args[2];
        Path p=Path.of(fichero);
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
        if (base.endsWith("_convertido")) {
            base = base.substring(0, base.length() - "_convertido".length());
        }
        Path salida = p.resolveSibling(base + "_convertido." + formato.toLowerCase());
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
    }

    static List<Pedido> leerCSV(Path p) {
        LinkedHashMap<Integer, Pedido> mapa = new LinkedHashMap<>();   // conserva el orden del fichero
        try (BufferedReader bf = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            bf.readLine();                                             // salta la cabecera
            String linea;
            int nLinea = 1;
            while ((linea = bf.readLine()) != null) {
                nLinea++;
                if (linea.isBlank()) continue;                         // ignora líneas vacías

                String[] campos = partirCsv(linea);
                // numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario
                if (campos.length != 7) {
                    throw new IllegalArgumentException("Línea " + nLinea + ": se esperaban 7 campos y hay " + campos.length);
                }
                try {
                    int numero = Integer.parseInt(campos[0].trim());
                    Pedido ped = mapa.get(numero);
                    if (ped == null) {                                 // pedido nuevo: se crea SIEMPRE, con lista vacía
                        ped = new Pedido(numero,
                                LocalDate.parse(campos[1].trim()),
                                new Cliente(Integer.parseInt(campos[2].trim()), campos[3]),
                                new ArrayList<>());
                        mapa.put(numero, ped);
                    }
                    if (!campos[4].isBlank()) {                        // solo hay línea si trae artículo
                        ped.addLinea(new LineaPedido(campos[4],
                                Integer.parseInt(campos[5].trim()),
                                Double.parseDouble(campos[6].trim())));
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    throw new IllegalArgumentException("Línea " + nLinea + ": dato mal formado (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el CSV: " + e.getMessage(), e);
        }
        return new ArrayList<>(mapa.values());
    }

    // Parte una línea CSV en campos respetando las comillas (inverso de "campo" en escribirCSV)
    static String[] partirCsv(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;                 // ¿estoy dentro de "..."?

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');            // "" dentro de comillas = una comilla literal
                        i++;
                    } else {
                        entreComillas = false;         // comilla de cierre
                    }
                } else {
                    actual.append(c);                  // coma u otro carácter: es texto
                }
            } else {
                if (c == '"') entreComillas = true;    // comilla de apertura
                else if (c == ',') {                   // coma fuera de comillas: separador
                    campos.add(actual.toString());
                    actual.setLength(0);
                } else actual.append(c);
            }
        }
        campos.add(actual.toString());                 // último campo (puede ser vacío)
        return campos.toArray(new String[0]);
    }


    static List<Pedido> leerXML(Path p){
        ArrayList<Pedido> ps=new ArrayList<>();

        try{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
           throw new RuntimeException(e);
       } catch (SAXException e) {
           throw new RuntimeException(e);
       }
       return ps;
    }

    @SuppressWarnings("unchecked")
    static List<Pedido> leerJson(Path p) {
        try {
            String texto = Files.readString(p, StandardCharsets.UTF_8);
            List<Object> raiz = (List<Object>) new JsonParser(texto).parse();   // la raíz es un array

            List<Pedido> pedidos = new ArrayList<>();
            for (Object o : raiz) {
                Map<String, Object> n = (Map<String, Object>) o;                // cada pedido es un objeto
                int numero = ((Number) n.get("numero")).intValue();
                LocalDate fecha = LocalDate.parse((String) n.get("fecha"));

                Map<String, Object> cli = (Map<String, Object>) n.get("cliente");
                Cliente c = new Cliente(((Number) cli.get("id")).intValue(), (String) cli.get("nombre"));

                Pedido pedido = new Pedido(numero, fecha, c, new ArrayList<>());
                for (Object lo : (List<Object>) n.get("lineas")) {              // si es [], no entra
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
            throw new RuntimeException("Error al leer el JSON: " + e.getMessage(), e);
        }
    }


    static void escribirCSV(List<Pedido> pedidos, Path salida) {
        // Función local: pone comillas si el texto lleva coma, comilla o salto de línea,
        // y duplica las comillas internas (Tornillo de 3" → "Tornillo de 3"" galvanizado")
        Function<String, String> campo = s ->
                (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
                        ? "\"" + s.replace("\"", "\"\"") + "\""
                        : s;

        StringBuilder sb = new StringBuilder();
        sb.append("numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario\n");  // cabecera

        for (Pedido ped : pedidos) {
            // Las 4 primeras columnas salen del pedido y se repiten en cada fila
            String inicio = ped.getNumero() + "," + ped.getFecha() + ","
                    + ped.getCliente().getId() + "," + campo.apply(ped.getCliente().getNombre());

            if (ped.getLineasPedido().isEmpty()) {// pedido sin líneas: columnas de línea vacías
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
            throw new RuntimeException("Error al escribir el CSV: " + e.getMessage(), e);
        }
    }


    static void escribirJson(List<Pedido> pedidos, Path salida) {
        StringBuilder sb = new StringBuilder();          // aquí vamos montando el texto
        sb.append("[\n");                                // raíz: array de pedidos

        for (int i = 0; i < pedidos.size(); i++) {
            Pedido ped = pedidos.get(i);
            sb.append("  {\n");
            sb.append("    \"numero\": ").append(ped.getNumero()).append(",\n");          // número: sin comillas
            sb.append("    \"fecha\": \"").append(ped.getFecha()).append("\",\n");        // texto: con comillas, ISO
            sb.append("    \"cliente\": {\"id\": ").append(ped.getCliente().getId())
                    .append(", \"nombre\": \"").append(escapar(ped.getCliente().getNombre()))  // texto de usuario: SIEMPRE escapar
                    .append("\"},\n");

            sb.append("    \"lineas\": [");
            List<LineaPedido> lineas = ped.getLineasPedido();
            for (int j = 0; j < lineas.size(); j++) {
                LineaPedido lp = lineas.get(j);
                sb.append(j == 0 ? "\n" : ",\n");        // coma entre elementos, nunca tras el último
                sb.append("      {\"articulo\": \"").append(escapar(lp.getArticulo()))
                        .append("\", \"unidades\": ").append(lp.getUnidades())
                        .append(", \"precioUnitario\": ").append(lp.getPrecioUnitario()).append("}");
            }
            sb.append(lineas.isEmpty() ? "]\n" : "\n    ]\n");   // sin líneas queda []
            sb.append("  }").append(i < pedidos.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]\n");

        try {
            Files.writeString(salida, sb.toString(), StandardCharsets.UTF_8);   // UTF-8 para los acentos
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el JSON: " + e.getMessage(), e);
        }
    }

    // Convierte un texto en un valor válido para ir entre comillas en JSON
    static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");          // Tornillo de 3" → 3\"
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));  // otros caracteres de control
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
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
                pedido.appendChild(lineas);   // si no hay líneas, queda <lineas/>
                raiz.appendChild(pedido);
            }
            xmlDoc.setXmlStandalone(true);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.transform(new DOMSource(xmlDoc), new StreamResult(salida.toFile()));

        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException("Error al escribir el XML: " + e.getMessage(), e);
        }
    }



//Clase creada  para aprsear el json sin uso de librerias como Jackson(ya que no se si la correccion lo usaria)
    static class JsonParser {
        private final String s;   // texto completo
        private int i = 0;        // posición actual

        JsonParser(String s) { this.s = s; }

        Object parse() {
            Object v = valor();
            saltar();
            if (i < s.length()) throw error("Texto sobrante");
            return v;
        }

        // Decide qué tipo de valor viene según el primer carácter
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

        // { "clave": valor, ... }  →  Map (LinkedHashMap conserva el orden)
        private Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;                                   // salta '{'
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

        // [ valor, ... ]  →  List
        private List<Object> array() {
            List<Object> l = new ArrayList<>();
            i++;                                   // salta '['
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

        // "texto" con escapes  →  String
        private String cadena() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw error("Texto sin cerrar");
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c != '\\') { sb.append(c); continue; }
                char e = s.charAt(i++);            // carácter tras la barra
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                    default  -> throw error("Escape no válido: \\" + e);
                }
            }
            return sb.toString();
        }


        private Double numero() {
            int ini = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (ini == i) throw error("Valor no válido");
            return Double.parseDouble(s.substring(ini, i));
        }

        // ignora espacios y saltos de línea
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


}