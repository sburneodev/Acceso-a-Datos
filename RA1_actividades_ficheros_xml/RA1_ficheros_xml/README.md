# RA1 · Actividades de ficheros y XML

Las doce actividades del RA1, ya escritas y organizadas en un proyecto Maven que
se abre directamente en IntelliJ. Todas se han compilado y ejecutado antes de
publicarlas.

---

## 1. Qué necesitas

| | |
|---|---|
| **JDK 25** | Eclipse Temurin, desde <https://adoptium.net/> |
| **IntelliJ IDEA Community** | Trae Maven incorporado: no hace falta instalarlo aparte |

No hace falta ni base de datos ni Docker: todo el RA1 trabaja con ficheros.

## 2. Abrir el proyecto en IntelliJ

1. Descomprime el `.zip` en una ruta **sin espacios ni tildes**, por ejemplo
   `C:\aulaforge\ra1-ficheros\`.
2. **File → Open**, y selecciona el fichero **`pom.xml`** (no la carpeta).
3. Elige **Open as Project** y, si lo pide, **Trust Project**.
4. Espera a que termine de descargar JAXB e indexar.

Comprueba el JDK en **File → Project Structure → Project**: en *SDK* tiene que
poner **25**. Si no lo tienes, en ese mismo desplegable, *Download JDK…* →
versión 25, Eclipse Temurin.

> **IMPORTANTE**
>
> En **File → Settings → Editor → File Encodings**, deja las tres opciones en
> **UTF-8**. En este RA la codificación no es un detalle: es la mitad del
> contenido, y un proyecto configurado en Windows-1252 da resultados distintos
> de los que aparecen en el libro.

## 3. Ejecutar una actividad

Abre la clase y pulsa el **triángulo verde** junto a `main`. Desde la terminal:

```
mvn compile exec:java -Dexec.mainClass=act08.LeerLibrosDOM
```

Los dos objetivos van juntos a propósito: `exec:java` **no compila**, así que
lanzarlo solo ejecutaría la versión anterior del código.

## 4. Qué hay en cada paquete

| Paquete | Actividad | Clases |
|---|---|---|
| `act01` | 1 · Acceso secuencial y aleatorio | `TamanoFichero`, `LecturaSecuencial`, `AccesoAleatorio`, `AccesoAleatorioUTF8` |
| `act02` | 2 · Rutas relativas y absolutas | `RutaRelativa` |
| `act03` | 3 · Gestión de ficheros con `File` | `GestionFicheros` |
| `act04` | 4 · Lectura y escritura secuencial | `GestorFicheroTexto` |
| `act05` | 5 · Acceso aleatorio con registros | `AccesoAleatorioEjercicio` |
| `act06` | 6 · NIO: `Path` y `Files` | `NioPractica` |
| `act07` | 7 · Serialización binaria | `Producto`, `EscribirProducto`, `LeerProducto` |
| `act08` | 8 · Leer XML con DOM y con SAX | `LeerLibrosDOM`, `LeerLibrosSAX` |
| `act09` | 9 · JAXP y XPath | `ConsultaProductosXPath` |
| `com.dam.xml` | 10 y 11 · JAXB | `Empleado`, `Empleados`, `GuardarEmpleadoXML`, `LeerEmpleadoXML` |
| `act12` | 12 · Gestión de excepciones | `GestorAlumnos` |

Cada actividad va en **su propio paquete** porque varias usan nombres de clase
parecidos y, en el paquete por defecto, chocarían. Las actividades 10 y 11
conservan el paquete `com.dam.xml` que aparece en el libro.

## 5. Los ficheros de datos

En `datos/` vienen los ficheros que las actividades **leen** y no pueden
inventarse:

- `datos/datos.txt` — actividad 1 (tamaño, lectura secuencial y acceso aleatorio)
- `datos/libros.xml` — actividad 8 (DOM y SAX)
- `datos/productos.xml` — actividad 9 (XPath)

El resto los **crean los propios programas** al ejecutarse: `registros.dat`,
`catalogo.dat`, `empleado.xml`, los CSV de la actividad 12… Por eso puedes
lanzarlas las veces que quieras.

> **Sobre `datos.txt`**
>
> Está en UTF-8 y su primera línea mide exactamente **15 bytes** contando el
> salto de línea. No es casualidad: 15 es la posición que trae `AccesoAleatorio`
> en la variable `posicion`, así que el puntero cae justo al principio de la
> segunda línea, que es la que lleva tildes y eñes.
>
> Por eso `AccesoAleatorio` la muestra rota (`caÃ±Ã³n`) y `AccesoAleatorioUTF8`
> la muestra bien (`cañón`). Ese contraste **es** la actividad. Si cambias el
> valor de `posicion` a 0, 10 o 57, verás qué pasa al caer en mitad de un
> carácter de varios bytes.

## 6. Si algo falla

| Mensaje | Qué pasa |
|---|---|
| `error: invalid source release: 25` | El JDK del proyecto no es el 25. *Project Structure → Project → SDK* |
| `package jakarta.xml.bind does not exist` | Maven no ha terminado de descargar; recarga el proyecto desde el panel Maven |
| `FileNotFoundException: datos/libros.xml` | Estás ejecutando desde otra carpeta. El directorio de trabajo debe ser la raíz del proyecto |
| Las tildes salen como `Ã©` o `?` | El fichero no está en UTF-8, o el proyecto no lo está. Ver el apartado 2 |
| `FileNotFoundException: datos/datos.txt` | Estás ejecutando desde otra carpeta: el directorio de trabajo debe ser la raíz del proyecto |

Sobre el directorio de trabajo: IntelliJ usa por omisión la raíz del proyecto, y
por eso las rutas `datos/libros.xml` funcionan. Si alguna vez no encuentra los
ficheros, míralo en **Run → Edit Configurations… → Working directory**.
---

## Salidas que parecen un error y no lo son

Varias actividades **provocan fallos a propósito**, porque enseñan qué pasa
cuando algo va mal. Verás la palabra `ERROR`, nombres de excepciones y, en
IntelliJ, texto en **rojo** —es la salida de error, que el editor resalta—.

Para distinguir una demostración de un problema de verdad, no mires el color:
mira cómo termina.

- Si el programa **termina** y el mensaje es una frase escrita por la actividad,
  forma parte del ejercicio.
- Si el programa **se corta** con `Exception in thread "main"` y decenas de
  líneas de `at com...`, ahí sí hay un fallo.

En este proyecto son dos casos concretos:

| Actividad | Qué verás | Por qué está bien |
|---|---|---|
| 12 · `act12.GestorAlumnos` | `RECHAZADO ...`, `ERROR ... no esta guardado en UTF-8` | La actividad va de clasificar ficheros mal formados: si no salieran, no habría nada que ver |
| 1 · `act01.AccesoAleatorio` | Tildes rotas al leer sin UTF-8 | Es el experimento: enseña qué pasa cuando la codificación no coincide |
