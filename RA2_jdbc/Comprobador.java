import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Comprueba que el proyecto y tu equipo estan listos para trabajar: la version
 * de Java, la estructura del proyecto, los ficheros de datos que espera el
 * codigo, la compilacion con Maven y la base de datos que necesita.
 *
 * Sirve para un proyecto suelto y para el paquete con todos: si en la carpeta
 * no hay un pom.xml pero si lo hay en varias de dentro, los comprueba todos.
 * Con  --sin-maven  se salta la compilacion, que es lo que mas tarda.
 *
 * No forma parte del temario y no hay que entenderlo: es una herramienta.
 * Se ejecuta sin compilar nada y no deja ficheros .class:
 *
 *     java Comprobador.java
 *
 * O con doble clic en comprobar.bat (Windows) / sh comprobar.sh (Linux y macOS).
 */
public class Comprobador {

    static final boolean WINDOWS =
            System.getProperty("os.name").toLowerCase().startsWith("windows");

    static final int JAVA_NECESARIO = 25;

    static int correctos = 0;
    static final List<String> fallos = new ArrayList<>();
    static final List<String> avisos = new ArrayList<>();

    static Path raiz;
    static boolean conMaven = true;
    /** Ejecutable de Maven que se va a usar, o null si no hay ninguno. */
    static String maven;

    public static void main(String[] args) throws Exception {
        for (String a : args) if (a.equals("--sin-maven")) conMaven = false;

        List<Path> proyectos = localizarProyectos();

        System.out.println("=== Comprobacion de Acceso a Datos ===");
        System.out.println("Sistema: " + System.getProperty("os.name")
                + "  |  Java: " + System.getProperty("java.version"));
        if (proyectos.size() > 1) {
            System.out.println("Proyectos encontrados: " + proyectos.size());
            if (conMaven) {
                System.out.println("Se van a compilar todos. La primera vez Maven se descarga");
                System.out.println("sus librerias y puede tardar un buen rato; con --sin-maven");
                System.out.println("se salta esa parte.");
            }
        }
        System.out.println();

        comprobarJava();
        if (conMaven) localizarMaven();

        for (Path p : proyectos) {
            raiz = p;
            if (proyectos.size() > 1) {
                System.out.println();
                System.out.println("-- " + rutaCorta(raiz));
            }
            comprobarEstructura();
            comprobarFicherosDeDatos();
            comprobarBaseDeDatos();
            if (conMaven) comprobarMaven();
        }

        System.out.println();
        if (fallos.isEmpty()) {
            System.out.println("TODO CORRECTO: " + correctos + " comprobaciones"
                    + (avisos.isEmpty() ? "." : ", con " + avisos.size() + " aviso(s)."));
        } else {
            System.out.println("HAY " + fallos.size() + " PROBLEMA(S) que resolver:");
            for (String f : fallos) System.out.println("   - " + f);
        }
        if (!avisos.isEmpty()) {
            System.out.println();
            System.out.println("Avisos (no impiden trabajar):");
            Map<String, Integer> cuantos = new LinkedHashMap<>();
            for (String a : avisos) cuantos.merge(a, 1, Integer::sum);
            for (var e : cuantos.entrySet()) {
                System.out.println("   - " + e.getKey()
                        + (e.getValue() > 1 ? "   (en " + e.getValue() + " proyectos)" : ""));
            }
        }
    }

    // ------------------------------------------------------------ apartados

    static void comprobarJava() {
        int version = Runtime.version().feature();
        if (version >= JAVA_NECESARIO) {
            ok("Java " + version + " (los proyectos piden " + JAVA_NECESARIO + " o superior)");
        } else {
            error("Tienes Java " + version + " y hace falta " + JAVA_NECESARIO,
                  "Instala Eclipse Temurin " + JAVA_NECESARIO + " desde https://adoptium.net/",
                  "y seleccionalo en IntelliJ: File > Project Structure > Project > SDK.");
        }
    }

    static void comprobarEstructura() {
        if (Files.isRegularFile(raiz.resolve("pom.xml"))) {
            ok("pom.xml encontrado");
        } else {
            error("No hay pom.xml en " + raiz,
                  "Ejecuta el comprobador dentro de la carpeta del proyecto.");
            return;
        }
        Path fuentes = raiz.resolve("src/main/java");
        long n = contar(fuentes, ".java");
        if (n > 0) ok("src/main/java con " + n + " ficheros .java");
        else error("No hay codigo en src/main/java",
                   "El zip puede haberse extraido a medias: vuelve a descomprimirlo.");
    }

    /**
     * Comprueba los ficheros de datos que el codigo LEE. Los que el propio
     * programa escribe no cuentan: es normal que no vengan en el zip. Solo
     * se avisa de un fichero que se lee, nunca se escribe y no esta.
     */
    static void comprobarFicherosDeDatos() {
        Pattern literal = Pattern.compile(
                "\"([\\w./\\\\-]+\\.(?:txt|xml|csv|json|properties|dat|ser))\"");
        Set<String> leidos = new LinkedHashSet<>();
        Set<String> escritos = new LinkedHashSet<>();

        for (Path f : buscar(raiz, ".java")) {
            // Las pruebas trabajan con carpetas temporales, no con ficheros del zip.
            if (f.toString().replace('\\', '/').contains("/src/test/")) continue;
            String codigo = leerFichero(f);
            Matcher m = literal.matcher(codigo);
            while (m.find()) {
                String nombre = m.group(1).replace('\\', '/');
                String antes = codigo.substring(Math.max(0, m.start() - 90), m.start());
                String despues = codigo.substring(m.end(), Math.min(codigo.length(), m.end() + 30));
                // RandomAccessFile en modo "rw" escribe tambien, no solo lee.
                boolean accesoLecturaEscritura =
                        antes.contains("RandomAccessFile") && despues.contains("\"rw\"");
                if (accesoLecturaEscritura || pinta(antes, ESCRITURA)) escritos.add(nombre);
                if (pinta(antes, LECTURA)) leidos.add(nombre);
            }
        }
        leidos.removeAll(escritos);
        if (leidos.isEmpty()) return;

        List<String> ausentes = new ArrayList<>();
        for (String cita : leidos) {
            String nombre = cita.substring(cita.lastIndexOf('/') + 1);
            if (buscar(raiz, nombre).isEmpty()) ausentes.add(cita);
        }
        int presentes = leidos.size() - ausentes.size();
        if (presentes > 0) ok(presentes + " fichero(s) de datos que el codigo lee estan en el proyecto");
        if (!ausentes.isEmpty()) {
            aviso("El codigo lee " + ausentes.size() + " fichero(s) que no vienen en el proyecto: "
                    + String.join(", ", ausentes),
                  "Si son ficheros que creas tu durante la actividad, es lo normal.",
                  "Si tenian que venir en el zip, avisa al profesor con este mensaje.");
        }
    }

    static final String[] LECTURA = {
        "FileReader", "FileInputStream", "newBufferedReader", "readAllLines",
        "readString", "readAllBytes", "\\.parse(", "new Scanner(", "RandomAccessFile",
        "ObjectInputStream", "getResourceAsStream", "\\.lines(", "\\.exists(" };

    static final String[] ESCRITURA = {
        "FileWriter", "FileOutputStream", "newBufferedWriter", "Files.write",
        "PrintWriter", "ObjectOutputStream", "\\.transform(", "createNewFile",
        "\\.marshal(", "StreamResult",
        // metodos propios del proyecto que lo que hacen es escribir
        "guardar(", "escribir(", "grabar(", "exportar(", "volcar(", "save(",
        "convertir(", "generar(" };

    /** Dice si el trozo de codigo anterior al nombre encaja con alguna pista. */
    static boolean pinta(String contexto, String[] pistas) {
        for (String p : pistas) {
            if (contexto.contains(p.replace("\\", ""))) return true;
        }
        return false;
    }

    /** Lee las URL jdbc de la configuracion y comprueba si esa base de datos responde. */
    static void comprobarBaseDeDatos() {
        Pattern url = Pattern.compile("jdbc:([a-z0-9]+):([^\"'\\s<>]+)");
        Set<String> urls = new LinkedHashSet<>();
        for (String ext : new String[]{".properties", ".xml", ".yml", ".yaml", ".java"}) {
            for (Path f : buscar(raiz, ext)) {
                if (f.toString().replace('\\', '/').contains("/target/")) continue;
                Matcher m = url.matcher(sinComentarios(f));
                while (m.find()) urls.add(m.group(0));
            }
        }
        // eXist-db no se conecta por JDBC, sino por su API XML:DB o por REST.
        Set<String> existdb = new LinkedHashSet<>();
        Pattern rest = Pattern.compile("(?:xmldb:exist://|http://)([\\w.-]+):(\\d+)/exist");
        // Primero la configuracion. Solo si no dice nada se mira el valor por
        // defecto que tenga el codigo: en Spring, application.properties manda.
        for (String[] grupo : new String[][]{
                {".properties", ".yml", ".yaml", ".xml"}, {".java"}}) {
            for (String ext : grupo) {
                for (Path f : buscar(raiz, ext)) {
                    if (f.toString().replace('\\', '/').contains("/target/")) continue;
                    Matcher m = rest.matcher(sinComentarios(f));
                    while (m.find()) existdb.add(m.group(1) + ":" + m.group(2));
                }
            }
            if (!existdb.isEmpty()) break;
        }
        for (String hp : existdb) {
            String[] partes = hp.split(":");
            if (responde(partes[0], Integer.parseInt(partes[1]))) {
                if (pareceExist(partes[0], Integer.parseInt(partes[1]))) {
                    ok("eXist-db responde en " + hp);
                } else {
                    aviso("En " + hp + " responde algo, pero no parece eXist-db",
                          "Tienes ese puerto ocupado por otro programa.",
                          "Cambia el puerto de eXist en el .env del entorno y en",
                          "el application.properties de este proyecto.");
                }
            } else {
                aviso("eXist-db NO responde en " + hp,
                      "Levanta el entorno del modulo antes de ejecutar el proyecto:",
                      "   docker compose up -d     (en la carpeta entorno-acceso-a-datos)",
                      "Es el Paso 0 de la guia del modulo.");
            }
        }

        if (urls.isEmpty()) {
            if (existdb.isEmpty()) ok("Este proyecto no necesita ninguna base de datos");
            return;
        }
        Set<String> vistos = new LinkedHashSet<>();
        for (String u : urls) {
            if (u.startsWith("jdbc:h2:mem")) {
                if (vistos.add("h2")) ok("Usa H2 en memoria: no hay que levantar nada");
                continue;
            }
            Matcher hp = Pattern.compile("//([\\w.-]+)(?::(\\d+))?").matcher(u);
            String motor = u.split(":")[1];
            if (!hp.find()) { ok("Base de datos local (" + u + ")"); continue; }
            String host = hp.group(1);
            int puerto = hp.group(2) != null ? Integer.parseInt(hp.group(2)) : puertoPorDefecto(motor);
            if (!vistos.add(host + ":" + puerto)) continue;   // ya se ha comprobado
            if (responde(host, puerto)) {
                ok(motor + " responde en " + host + ":" + puerto);
            } else {
                aviso(motor + " NO responde en " + host + ":" + puerto,
                      "Levanta el entorno del modulo antes de ejecutar el proyecto:",
                      "   docker compose up -d     (en la carpeta entorno-acceso-a-datos)",
                      "Es el Paso 0 de la guia del modulo.");
            }
        }
    }

    /**
     * Busca Maven una sola vez: primero en el PATH y, si no esta, el que trae
     * IntelliJ dentro. Si no aparece ninguno, se dice una vez y no se compila.
     */
    static void localizarMaven() {
        if (ejecutar(List.of(WINDOWS ? "mvn.cmd" : "mvn", "-v"), 60)
                .salida.contains("Apache Maven")) {
            maven = WINDOWS ? "mvn.cmd" : "mvn";
            ok("Maven encontrado en el PATH");
            return;
        }
        String otro = mavenAlternativo();
        if (otro != null) {
            maven = otro;
            ok("Maven encontrado fuera del PATH: " + otro);
            return;
        }
        conMaven = false;
        aviso("No he encontrado Maven, asi que no compruebo la compilacion",
              "No es un problema: IntelliJ trae el suyo y compila igual.",
              "Abre cada proyecto con File > Open eligiendo su pom.xml,",
              "y compila con Build > Build Project.",
              "(Darle a \"Load Maven Project\" en IntelliJ no anade mvn al PATH:",
              " solo configura el proyecto dentro del IDE.)",
              "Todo lo demas si se ha comprobado.");
    }

    /**
     * Busca un Maven que no este en el PATH: el de MAVEN_HOME, o el que IntelliJ
     * instala dentro de si mismo. Devuelve null si no encuentra ninguno.
     *
     * Ojo: darle a "Load Maven Project" en IntelliJ NO anade mvn al PATH; solo
     * configura el proyecto dentro del IDE. Por eso hay que ir a buscarlo.
     *
     * No se rastrean carpetas enteras (Program Files tiene subcarpetas que no se
     * pueden leer y el recorrido se aborta): se prueba la ruta concreta en la que
     * IntelliJ lo deja, dentro de cada instalacion que se encuentre.
     */
    static String mavenAlternativo() {
        String ejecutable = WINDOWS ? "mvn.cmd" : "mvn";

        for (String variable : new String[]{"MAVEN_HOME", "M2_HOME"}) {
            String valor = System.getenv(variable);
            if (valor == null) continue;
            Path p = Path.of(valor, "bin", ejecutable);
            if (Files.isRegularFile(p)) return p.toString();
        }
        if (!WINDOWS) return null;

        // Donde se instala IntelliJ, segun como lo hayas instalado.
        List<Path> bases = new ArrayList<>();
        for (String variable : new String[]{"ProgramFiles", "ProgramFiles(x86)", "LOCALAPPDATA"}) {
            String valor = System.getenv(variable);
            if (valor == null) continue;
            bases.add(Path.of(valor, "JetBrains"));                      // instalador normal
            bases.add(Path.of(valor, "Programs"));                       // instalacion por usuario
            bases.add(Path.of(valor, "JetBrains", "Toolbox", "apps"));   // JetBrains Toolbox
        }
        for (Path base : bases) {
            String encontrado = buscarMavenBajo(base, 3);
            if (encontrado != null) return encontrado;
        }
        return null;
    }

    /** Prueba plugins/maven/lib/maven3/bin/mvn.cmd en esta carpeta y sus hijas. */
    static String buscarMavenBajo(Path carpeta, int niveles) {
        if (niveles < 0 || !Files.isDirectory(carpeta)) return null;
        Path candidato = carpeta.resolve("plugins").resolve("maven")
                                .resolve("lib").resolve("maven3")
                                .resolve("bin").resolve("mvn.cmd");
        if (Files.isRegularFile(candidato)) return candidato.toString();

        try (Stream<Path> hijas = Files.list(carpeta)) {
            for (Path h : hijas.toList()) {
                if (!Files.isDirectory(h)) continue;
                String encontrado = buscarMavenBajo(h, niveles - 1);
                if (encontrado != null) return encontrado;
            }
        } catch (IOException ignorada) {
            // Carpeta sin permisos: se salta y se sigue con las demas.
        }
        return null;
    }

    static void comprobarMaven() {
        System.out.println("         (compilando con Maven, la primera vez puede tardar)");
        Resultado r = ejecutar(ordenMaven("-B", "-q", "clean", "compile"), 900);
        if (r.codigo == 0) {
            ok("El proyecto compila con Maven");
        } else if (r.salida.contains("Could not find artifact")) {
            Matcher m = Pattern.compile("Could not find artifact ([\\w.]+):([\\w.-]+):")
                               .matcher(r.salida);
            String cual = m.find() ? m.group(2) : "otro proyecto del modulo";
            aviso("Este proyecto depende de " + cual + ", que no esta en tu equipo todavia",
                  cual + " es otro de los proyectos del modulo: no viene de internet,",
                  "lo construyes tu. Descomprimelo y, desde su carpeta, ejecuta:",
                  "   mvn install",
                  "Despues vuelve a pasar este comprobador. Lo explica el README.");
        } else {
            error("El proyecto NO compila", primerasLineas(r.salida, 8));
        }
    }

    /** Pregunta por HTTP si lo que hay en ese puerto es de verdad eXist-db. */
    static boolean pareceExist(String host, int puerto) {
        try {
            java.net.URL u = java.net.URI.create(
                    "http://" + host + ":" + puerto + "/exist/").toURL();
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) u.openConnection();
            con.setConnectTimeout(2000);
            con.setReadTimeout(3000);
            con.setRequestMethod("GET");
            int codigo = con.getResponseCode();
            con.disconnect();
            return codigo >= 200 && codigo < 400;
        } catch (Exception e) {
            return false;
        }
    }

    /** El nombre del proyecto con su carpeta padre: "RA3/academia-jpa". */
    static String rutaCorta(Path p) {
        Path padre = p.getParent();
        return padre == null ? p.getFileName().toString()
                             : padre.getFileName() + "/" + p.getFileName();
    }

    // --------------------------------------------------------------- apoyos

    /**
     * Encuentra los proyectos que hay que comprobar: el de esta carpeta si tiene
     * pom.xml, y si no, todos los que cuelguen de ella. Asi el mismo fichero vale
     * para un proyecto suelto y para el paquete con todos.
     */
    static List<Path> localizarProyectos() {
        Path aqui = Path.of("").toAbsolutePath();
        if (Files.isRegularFile(aqui.resolve("pom.xml"))) return List.of(aqui);

        List<Path> encontrados = new ArrayList<>();
        try (Stream<Path> rutas = Files.walk(aqui, 4)) {
            for (Path p : rutas.toList()) {
                if (!p.getFileName().toString().equals("pom.xml")) continue;
                Path proyecto = p.getParent();
                // Los pom.xml de dentro de src/ o target/ no son proyectos aparte.
                String ruta = proyecto.toString().replace('\\', '/');
                if (ruta.contains("/src/") || ruta.contains("/target/")) continue;
                encontrados.add(proyecto);
            }
        } catch (IOException ignorada) { }
        encontrados.sort(Comparator.comparing(Path::toString));
        return encontrados.isEmpty() ? List.of(aqui) : encontrados;
    }

    /** La orden completa para lanzar el Maven que se haya encontrado. */
    static List<String> ordenMaven(String... args) {
        List<String> orden = new ArrayList<>();
        orden.add(maven);
        orden.addAll(List.of(args));
        return orden;
    }

    static int puertoPorDefecto(String motor) {
        return switch (motor) {
            case "mysql", "mariadb" -> 3306;
            case "postgresql" -> 5432;
            case "oracle" -> 1521;
            case "sqlserver" -> 1433;
            case "exist" , "xmldb" -> 8080;
            default -> 0;
        };
    }

    static boolean responde(String host, int puerto) {
        if (puerto == 0) return false;
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, puerto), 1500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    record Resultado(int codigo, String salida) { }

    static Resultado ejecutar(List<String> orden, int segundos) {
        try {
            // raiz todavia es null cuando se busca Maven, antes del primer proyecto.
            ProcessBuilder pb = new ProcessBuilder(orden).redirectErrorStream(true);
            if (raiz != null) pb.directory(raiz.toFile());
            Process p = pb.start();
            p.getOutputStream().close();
            String salida = new String(p.getInputStream().readAllBytes(), Charset.defaultCharset());
            if (!p.waitFor(segundos, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return new Resultado(-1, "se ha quedado colgado mas de " + segundos + " segundos");
            }
            return new Resultado(p.exitValue(), salida);
        } catch (IOException | InterruptedException e) {
            return new Resultado(-1, e.toString());
        }
    }

    static List<Path> buscar(Path desde, String terminacion) {
        try (Stream<Path> rutas = Files.walk(desde)) {
            return rutas.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(terminacion))
                        .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    static long contar(Path desde, String terminacion) {
        return buscar(desde, terminacion).size();
    }

    static String leerFichero(Path f) {
        try { return Files.readString(f); }
        catch (IOException e) { return ""; }
    }

    /**
     * Devuelve el fichero sin sus comentarios. Sin esto, una linea como
     *   # spring.datasource.url=jdbc:postgresql://localhost:5432/biblioteca
     * se tomaria por configuracion de verdad y se avisaria de una base de datos
     * que el proyecto no usa.
     */
    static String sinComentarios(Path f) {
        String texto = leerFichero(f);
        String nombre = f.getFileName().toString();
        if (nombre.endsWith(".xml")) {
            return texto.replaceAll("(?s)<!--.*?-->", "");
        }
        if (nombre.endsWith(".java")) {
            texto = texto.replaceAll("(?s)/\\*.*?\\*/", "");
        }
        StringBuilder limpio = new StringBuilder();
        for (String linea : texto.split("\n")) {
            String sinEspacios = linea.stripLeading();
            if (sinEspacios.startsWith("#") || sinEspacios.startsWith("//")
                    || sinEspacios.startsWith("--")) {
                continue;
            }
            limpio.append(linea).append('\n');
        }
        return limpio.toString();
    }

    static String[] primerasLineas(String texto, int cuantas) {
        List<String> utiles = new ArrayList<>();
        for (String l : texto.split("\n")) {
            if (!l.isBlank() && utiles.size() < cuantas) utiles.add(l.strip());
        }
        return utiles.toArray(new String[0]);
    }

    static void ok(String que) {
        correctos++;
        System.out.println("[OK]     " + que);
    }

    static void aviso(String que, String... detalle) {
        avisos.add(que);
        System.out.println("[AVISO]  " + que);
        for (String d : detalle) System.out.println("         " + d);
    }

    static void error(String que, String... detalle) {
        fallos.add(que);
        System.out.println("[ERROR]  " + que);
        for (String d : detalle) System.out.println("         " + d);
    }
}
