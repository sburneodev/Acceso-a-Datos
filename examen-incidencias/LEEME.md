# Plantilla examen práctico: incidencias (CSV → TXT, CSV → XML con JAXB, JUnit)

> Borra este fichero antes de entregar si no lo piden.

## Estructura

```
examen-incidencias/
├── pom.xml                         JAXB (api + runtime) y JUnit 5
├── respuesta.txt                   pregunta de desarrollo (borrador: reescríbelo)
├── datos/incidencias.csv           CSV de prueba (cámbialo por el del examen)
└── src/
    ├── main/java/incidencias/
    │   ├── Incidencia.java         POJO + anotaciones JAXB (constructor vacío OBLIGATORIO)
    │   ├── Incidencias.java        clase raíz <incidencias> con la lista (JAXB la necesita)
    │   ├── LectorIncidencias.java  lee el CSV: válidas + números de líneas rotas
    │   ├── Ejercicio1.java         main: resumen → salida/resumen.txt
    │   ├── ConversorXml.java       guardar (Marshaller) y leer (Unmarshaller)
    │   └── Ejercicio2.java         main: CSV → objetos → XML → comprueba sin pérdidas
    └── test/java/incidencias/
        └── ConversorXmlTest.java   @TempDir + ida y vuelta + líneas rotas
```

## Comandos

```bash
mvn compile
mvn test
mvn -q compile exec:java "-Dexec.mainClass=incidencias.Ejercicio1"
mvn -q compile exec:java "-Dexec.mainClass=incidencias.Ejercicio2"
```

En IntelliJ: clic derecho en `Ejercicio1` → Run. El directorio de trabajo tiene que ser la raíz del proyecto (donde está `datos/`), si no sale `ERROR: no encuentra el archivo`.

## Qué adaptar en clase (busca `ADAPTAR` en el código)

1. **Nombres de clases y métodos** exactos del enunciado (el corrector los busca por nombre). Si renombras una clase, renombra también el fichero.
2. **Textos de salida** carácter a carácter (consola y TXT): `Incidencias válidas: `, `Abiertas: `, `Por aula:`...
3. **Nombres de ficheros**: `salida/resumen.txt`, `salida/incidencias.xml`.
4. **XML**: si el `id` tiene que ser etiqueta y no atributo, cambia `@XmlAttribute` por `@XmlElement` y añade `"id"` al principio de `propOrder`.
5. **Segundo método público del lector**: aquí es `getLineasRotas()`; cámbialo si el enunciado da otro nombre o firma.

## JAXB en 30 segundos (lo nuevo para ti)

- Anotas el POJO: `@XmlRootElement` (puede ser raíz), `@XmlAccessorType(FIELD)` (lee campos), `@XmlElement` / `@XmlAttribute` (etiqueta o atributo), `@XmlType(propOrder=...)` (orden).
- Una lista no puede ser raíz: hace falta la clase contenedora `Incidencias` con `@XmlElement(name = "incidencia") List<Incidencia>`.
- Escribir: `JAXBContext.newInstance(Incidencias.class)` → `createMarshaller()` → `setProperty(JAXB_FORMATTED_OUTPUT, true)` → `marshal(objeto, fichero)`.
- Leer: `createUnmarshaller()` → `(Incidencias) unmarshal(fichero)` → `getLista()`.
- Errores: todo lanza `JAXBException`; su `getMessage()` puede ser null, la causa real está en `getLinkedException()`.
- Si falta el constructor vacío o una anotación está mal, falla al crear el `JAXBContext` (también al guardar).
- Paquete `jakarta.xml.bind`, NO `javax.xml.bind`.

## Antes de entregar

- `mvn compile` y `mvn test` en verde.
- Un único `.zip`, sin `target/`, `.idea/` ni `salida/`. Descomprímelo en otra carpeta y comprueba que está todo.
- Si algo del ejercicio 2 no compila, coméntalo: un proyecto que no compila deja sin nota los dos ejercicios.
