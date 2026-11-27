# A10 - Biblioteca

Aplicación de consola para gestionar los socios, libros y préstamos de una biblioteca de barrio. Funciona con MySQL o con H2, y para pasar de una a otra solo hay que cambiar `db.properties`.

## Cómo se arranca

1. Levantar la base de datos del laboratorio: en la carpeta `RA2_jdbc`, ejecutar `docker compose up -d`. Esto arranca el contenedor `mysql-ra2` con la base `empresa`.
2. Abrir la carpeta `biblioteca` en IntelliJ como proyecto Maven y esperar a que descargue las dependencias. Hace falta JDK 25.
3. Revisar `src/main/resources/db.properties`. Por defecto apunta a MySQL. Para usar H2 en memoria hay que comentar las tres líneas de MySQL (poniendo `#` delante) y descomentar las de H2.
4. Ejecutar `com.Main` para usar el menú, o `com.Demostracion` para ver la aplicación entera de principio a fin sin tener que escribir nada.

No hace falta crear las tablas a mano: la aplicación las crea al arrancar si no existen.

## El conector

El conector (en este caso MySQL Connector/J) es el driver JDBC que hace de intermediario entre la aplicación Java y el servidor MySQL: implementa la API estándar de JDBC (`DriverManager`, `Connection`...) y se encarga por debajo de abrir la conexión, autenticarse y traducir cada llamada al protocolo propio de MySQL. La aplicación usa uno en lugar de comunicarse directamente con MySQL porque implementar ese protocolo a mano (sockets, paquetes, autenticación, tipos de datos) sería muy costoso y propenso a errores, y así el programador solo trabaja con una interfaz común y le basta con una URL, un usuario y una contraseña. Como ventaja, el código queda desacoplado del motor de base de datos, de modo que cambiar de MySQL a otro SGBD solo exige cambiar el driver y la URL, sin reescribir el acceso a datos. Como inconveniente, se añade una dependencia externa que hay que mantener en el `pom.xml` con su versión, y si no está en el classpath la aplicación ni siquiera conecta (`No suitable driver found`).

## MySQL y H2

Al cambiar a H2 la mayoría de cosas funcionaron a la primera. Las tablas se crearon sin tocar nada porque en la URL puse `MODE=MySQL`, y así H2 acepta la sintaxis de MySQL, hasta el `ENGINE=InnoDB`. Las altas devolviendo el id, la carga del catálogo en lote, las consultas y los préstamos con transacción también funcionaron igual. La primera vez me dio "No suitable driver", pero era porque IntelliJ no había recargado el `pom.xml` y H2 no estaba en el classpath.

Lo que no funcionó fueron tres cosas. La primera, el procedimiento `prestamos_activos`: H2 no tiene procedimientos escritos en SQL, y el `CREATE PROCEDURE` daba error de sintaxis. Para arreglarlo añadí en `Conexiones` un método `esH2()` que mira con los metadatos de la conexión a qué base estamos conectados. Si es H2, no se crea el procedimiento y la opción del menú hace la misma consulta (`SELECT COUNT(*)`) directamente desde Java. Si es MySQL, se sigue llamando al procedimiento como antes. La segunda, en la consulta de préstamos de un socio leía las columnas con el alias delante (`rs.getInt("p.id")`). Eso el driver de MySQL lo acepta, pero no es lo normal, así que lo cambié para leer por el nombre de la columna y le puse `AS id_prestamo` al id. La tercera, los mensajes de "el libro tiene préstamos" y "el socio no existe" los sacaba mirando los códigos de error 1451 y 1452, que solo existen en MySQL. Lo cambié para comprobar si la excepción es una `SQLIntegrityConstraintViolationException`, que la lanzan los dos drivers cuando falla una clave ajena. Todos los cambios valen para las dos bases, y la versión de MySQL sigue funcionando igual.

Para esta biblioteca elegiría un servidor como MySQL. H2 en memoria pierde todos los datos al cerrar el programa, y aunque se puede guardar en un fichero, entonces solo puede usarla una aplicación a la vez. En una biblioteca lo normal es que haya más de un ordenador (el mostrador, la oficina...) consultando los mismos préstamos, y con un servidor todos ven los mismos datos al momento y es más fácil hacer copias de seguridad. H2 sí me parece útil para hacer pruebas, porque no hay que instalar nada ni levantar Docker.