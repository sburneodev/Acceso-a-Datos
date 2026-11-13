# RA2 · Actividades de JDBC avanzado

Las ocho actividades del RA2, ya escritas, con su base de datos MySQL y su
`pom.xml`. Todas se han compilado y ejecutado antes de publicarlas.

---

## 1. Qué necesitas

| | |
|---|---|
| **JDK 25** | Eclipse Temurin, desde <https://adoptium.net/> |
| **IntelliJ IDEA Community** | Trae Maven incorporado: no hace falta instalarlo aparte |
| **Docker Desktop** | Para la base de datos. Arrancado, con la ballena quieta |

## 2. Abrir el proyecto en IntelliJ

1. Descomprime el `.zip` en una ruta **sin espacios ni tildes**, por ejemplo
   `C:\aulaforge\ra2-jdbc\`.
2. En IntelliJ: **File → Open**, y selecciona el fichero **`pom.xml`** de la
   carpeta (no la carpeta: el `pom.xml`).
3. Cuando pregunte, elige **Open as Project**. Y si sale un aviso de proyecto no
   confiable, **Trust Project**.
4. Espera a que termine la barra de abajo: está descargando el driver de MySQL y
   HikariCP, e indexando. La primera vez tarda un par de minutos.

Después comprueba el JDK: **File → Project Structure → Project**, y en *SDK*
tiene que aparecer un **25**. Si no lo tienes, ahí mismo, en el desplegable,
*Download JDK…* → versión 25, distribución Eclipse Temurin.

> **CONSEJO**
>
> Si el editor subraya en rojo `com.mysql` o `com.zaxxer`, no es que falte nada:
> es que Maven aún no ha terminado. Espera, o pulsa el icono de recargar del
> panel Maven, a la derecha.

## 3. Levantar la base de datos

Abre una terminal **en la carpeta del proyecto** —en IntelliJ, la pestaña
*Terminal* de abajo ya se abre ahí— y escribe:

```
docker compose up -d
```

La primera vez tarda un minuto: descarga MySQL 8.4, lo arranca y ejecuta solo
los dos scripts de `sql/`, que crean la base `empresa`, la tabla `empleados` con
cuatro filas y los procedimientos `obtener_empleado` e `incrementar_salario`.

Comprueba que está en marcha:

```
docker compose ps
```

Tiene que aparecer `mysql-ra2` con estado `Up` y `(healthy)`.

## 4. Ejecutar una actividad

En IntelliJ, abre la clase que quieras de `src/main/java/sol/` y pulsa el
**triángulo verde** que aparece junto a `public static void main`. Desde la
terminal, el equivalente es:

```
mvn compile exec:java "-Dexec.mainClass=sol.Act01Fundamentos"
```

Los dos objetivos van juntos a propósito: `exec:java` **no compila**, así que si
lo lanzas solo después de tocar el código, ejecuta la versión anterior — o falla
si todavía no habías compilado nunca.

> **CUIDADO EN WINDOWS**
>
> Las comillas de `"-Dexec.mainClass=..."` no son opcionales: la terminal de
> IntelliJ en Windows es PowerShell, que sin ellas parte el argumento por el punto,
> y Maven contesta `Unknown lifecycle phase ".mainClass=..."`. Y si PowerShell no
> reconoce `mvn`, es que Maven no está instalado aparte: usa el triángulo verde o,
> en el panel Maven, *Execute Maven Goal*.

| Clase | Actividad | Qué demuestra |
|---|---|---|
| `conexion.TestConexion` | Maven | Que la conexión funciona; empieza siempre por esta |
| `sol.Act01Fundamentos` | 1 | `Statement`, `PreparedStatement` y `CallableStatement`, y la inyección SQL |
| `sol.Act02Pool` | 2 | Pool de conexiones con HikariCP: tres consultas con conexiones prestadas |
| `sol.Act03DdlDml` | 3 | DDL y DML: crear tabla, insertar, actualizar y borrar |
| `sol.Act04Metadatos` | 4 | `ResultSet`, `ResultSetMetaData` y `DatabaseMetaData` |
| `sol.Act05Procedimiento` | 5 | `CallableStatement` con parámetro de salida |
| `sol.Act06Transacciones` | 6 | `commit`, `rollback` y savepoints |
| `sol.Act07Integrado` | 7 | Caso completo: tablas, procedimiento y transacción |

Cada actividad **crea y borra sus propias tablas** al ejecutarse, así que puedes
lanzarlas en cualquier orden y las veces que quieras.

Además, en `src/main/java/ejemplos/` están los programas que el libro ejecuta en
cada tema, para que veas en tu equipo lo mismo que hay en las páginas:

| Clase | Tema | Qué enseña |
|---|---|---|
| `ejemplos.EjemploPool` | 2 | El ejemplo mínimo de HikariCP del libro: configurar, pedir una conexión y cerrar el pool |
| `ejemplos.MedirConexiones` | 2 | Cuánto cuesta abrir una conexión, medido: `DriverManager` frente al pool |
| `ejemplos.PoolReutiliza` | 2 | Que el pool reutiliza la conexión: se lo pregunta al servidor con `CONNECTION_ID()` |
| `ejemplos.PoolAgotado` | 2 | Diez usuarios para cinco conexiones, y la conexión que nadie cierra |
| `ejemplos.CrearTabla` | 3 | `CREATE TABLE` con `executeUpdate`; la segunda vez, el error de que ya existe |
| `ejemplos.InsertarEmpleado` | 3 | `INSERT` con `PreparedStatement` |
| `ejemplos.ActualizarEliminar` | 3 | `UPDATE` y `DELETE`, y el `UPDATE` que afecta a 0 filas |
| `ejemplos.OperacionesIntegradas` | 3 | `INSERT`, `UPDATE` y `DELETE` seguidos sobre la misma fila |
| `ejemplos.ErroresExecute` | 3 | Los errores de `executeQuery`/`executeUpdate` mal elegidos y de parametrizar un nombre de tabla |
| `ejemplos.DdlConfirma` | 3 | Que en MySQL un `CREATE TABLE` confirma la transacción y el `rollback` ya no deshace |
| `ejemplos.ClaveGenerada` | 3 | Recuperar el id generado por `AUTO_INCREMENT` |
| `ejemplos.InsertarPorLotes` | 3 | 5.000 filas una a una, por lotes y por lotes con `rewriteBatchedStatements` |
| `ejemplos.CursorBasico` | 4 | El recorrido de siempre, `while (rs.next())` |
| `ejemplos.CursorDesplazable` | 4 | `last()`, `first()`, `absolute()`, `previous()`, y qué tipos de cursor admite el driver |
| `ejemplos.ErroresCursor` | 4 | Leer sin `next()`, la columna 0 y el `ResultSet` ya cerrado |
| `ejemplos.LeerNulos` | 4 | Qué devuelve `getInt` con un `NULL`, `wasNull()` y `getObject` |
| `ejemplos.ResultSetActualizable` | 4 | Modificar desde el `ResultSet`, y las tres consultas que no lo permiten |
| `ejemplos.MetadatosResultado` | 4 | `ResultSetMetaData`: columnas y tipos de un resultado |
| `ejemplos.MetadatosBase` | 4 | `DatabaseMetaData`: producto, tablas, columnas y clave primaria |
| `ejemplos.EmpleadoDAO` | 4 | Del `ResultSet` a objetos: un `record`, una lista y un `Optional` |
| `ejemplos.LlamarProcedimiento` | 5 | `CallableStatement` con un parámetro `IN` y uno `OUT` (antes, crear `obtener_salario` en el cliente) |
| `ejemplos.LlamarFuncion` | 5 | La sintaxis `{? = call ...}` de una función (antes, crear `contar_empleados`) |
| `ejemplos.ErroresCallable` | 5 | Función llamada como procedimiento, `OUT` sin registrar y un parámetro de menos |
| `ejemplos.EstadoConexion` | 6 | Cómo viene una conexión: autocommit y nivel de aislamiento |
| `ejemplos.CrearCuentas` | 6 | Crea las tablas `cuentas` y `logs` de los ejemplos del tema 6 (ejecutarla primero) |
| `ejemplos.TransferenciaBancaria` | 6 | El patrón completo: conexión fuera del `try`, `commit`, `rollback` y `finally` |
| `ejemplos.Savepoints` | 6 | Deshacer solo el paso que ha fallado, con un `Savepoint` |
| `ejemplos.PoolAutocommit` | 6 | Qué hace HikariCP con una conexión devuelta sin restaurar el autocommit |
| `ejemplos.NivelesAislamiento` | 6 | Dos conexiones a la vez: lectura sucia y lectura no repetible según el nivel |
| `ejemplos.CrearProyectos` | 7 | Crea `proyectos`, `asignaciones` y el procedimiento `asignar_empleado` (ejecutarla primero; y otra vez después de `sol.Act07Integrado`, que usa otra tabla `asignaciones`) |
| `ejemplos.ConsultarProyectos` | 7 | Insertar un proyecto y consultar por presupuesto |
| `ejemplos.AsignarEmpleado` | 7 | Llamar a `asignar_empleado` con `CallableStatement` |
| `ejemplos.TransaccionIntegrada` | 7 | Subir un sueldo y descontarlo de un proyecto, todo o nada |

Y en `src/main/java/revision/` está `GestionEmpleados`, la clase que hay que
**revisar** en la actividad 7 de Classroom: compila y parece que funciona, pero tiene
problemas. No la uses como modelo.

Los tiempos que imprimen cambian de un equipo a otro y de una ejecución a otra.
Los del tema 3 **cambian la tabla `empleados`** (y `CrearTabla` deja creada
`departamentos`). Para volver al punto de partida: `docker compose down -v` y
`docker compose up -d`.

## 5. Si algo falla

| Mensaje | Qué pasa |
|---|---|
| `Communications link failure` | La base de datos no está levantada: `docker compose up -d` |
| `Access denied for user 'root'` | La contraseña no es `root`, o quedó un MySQL viejo: `docker compose down -v` y vuelve a subir |
| `Unknown database 'empresa'` | El contenedor arrancó sin ejecutar `sql/`. Se arregla con `docker compose down -v` y `up -d` |
| `Public Key Retrieval is not allowed` | Falta `allowPublicKeyRetrieval=true` en la URL. Las clases ya lo llevan |
| `port is already allocated` | Tienes otro MySQL en el 3306. Ver el apartado siguiente |
| `error: invalid source release: 25` | El JDK del proyecto no es el 25. *Project Structure → Project → SDK* |

### Si el puerto 3306 está ocupado

Es lo más habitual si ya tenías MySQL instalado en Windows. Se cambia en dos
sitios, y **tienen que coincidir**:

1. En `docker-compose.yml`, el número de la **izquierda**:
   `- "127.0.0.1:3307:3306"`.
2. En el código, la constante `URL` de cada clase, y en
   `src/main/resources/db.properties`.

El de la derecha del compose, el 3306, es el puerto **dentro** del contenedor y
no se toca nunca.

## 6. Cuando termines

```
docker compose stop         # para la base de datos, conserva los datos
docker compose down -v      # la borra entera y la deja como nueva
```

Esa `-v` es útil aquí: como los scripts de `sql/` solo se ejecutan la primera
vez, es la forma de volver al punto de partida si has dejado la base hecha un
lío.
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

En este proyecto son tres casos concretos:

| Actividad | Qué verás | Por qué está bien |
|---|---|---|
| 1 · `Act01Fundamentos` | La búsqueda de `' OR '1'='1` no devuelve ninguna fila | Eso demuestra que `PreparedStatement` frena la inyección: busca a alguien con ese nombre literal y no existe |
| 5 · `Act05Procedimiento` | `error controlado: El empleado indicado no existe` | El procedimiento lanza el error a propósito y Java lo recoge |
| 6 · `Act06Transacciones` | `Transferencia de 99.999 EUR (debe fallar)` y `Rollback completo: no se ha movido nada` | El propio título lo dice: la transferencia tiene que fallar para poder ver el `rollback` |
