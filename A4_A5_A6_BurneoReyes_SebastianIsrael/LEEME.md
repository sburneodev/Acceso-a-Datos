# Conversor de pedidos (CSV ⇄ XML ⇄ JSON)

## 1. Qué hace

Convierte un fichero de pedidos de clientes entre tres formatos: CSV, XML y JSON. Lee el fichero de entrada, detecta su formato por la extensión y escribe la versión convertida en la misma carpeta, con el sufijo `_convertido`.

## 2. Qué necesita

- **JDK 25.**
- No hace falta compilar a mano: el programa se lanza directamente desde el código fuente con `java Conversor.java`, y Java compila en memoria `Conversor.java` y las clases que usa (`Cliente.java`, `Pedido.java` y `LineaPedido.java`). Para eso, los cuatro ficheros tienen que estar **en la misma carpeta** y sin `package`. En el proyecto esa carpeta es `src/main/java/`, pero el programa funciona igual fuera del proyecto, en cualquier carpeta, siempre que los cuatro `.java` estén juntos. Lanzar así un programa formado por varios ficheros requiere JDK 22 o superior.
- No hace falta ninguna librería externa: el XML se procesa con la API estándar de Java (DOM) y el JSON con un analizador propio incluido en `Conversor.java`.

Las pruebas automáticas (JUnit 5, en `src/test/java/ConversorTest.java`) se lanzan con Maven, desde la carpeta del `pom.xml`:

```bash
mvn test
```

## 3. Cómo se ejecuta

Desde la carpeta donde está `Conversor.java`:

```bash
java Conversor.java <fichero> --a <csv|xml|json>
```

- `--a` es **obligatorio** y va siempre en segundo lugar.
- Detrás de `--a` solo se admite `csv`, `xml` o `json`.
- `<fichero>` puede ser un nombre o una ruta. Si es relativa, cuenta desde la carpeta en la que se ejecuta el comando.

Los cuatro ejemplos siguientes se pueden copiar tal cual:

```bash
# CSV → JSON   (crea pedidos_convertido.json)
java Conversor.java pedidos.csv --a json

# CSV → XML    (crea pedidos_convertido.xml)
java Conversor.java pedidos.csv --a xml

# JSON → XML   (crea pedidos_convertido.xml a partir del JSON)
java Conversor.java pedidos_convertido.json --a xml

# XML → CSV    (crea pedidos_convertido.csv)
java Conversor.java pedidos_convertido.xml --a csv
```

**Cómo se nombra la salida:** el fichero de salida se llama `<nombre>_convertido.<formato>` y se guarda junto al de entrada. Si la entrada ya termina en `_convertido`, no se añade otro sufijo: `pedidos_convertido.json --a xml` da `pedidos_convertido.xml`, no `pedidos_convertido_convertido.xml`.

**Registro de errores:** los errores se anotan con su detalle técnico en `conversor.log`, en la carpeta desde la que se ejecuta el programa. El fichero se crea si no existe y cada error nuevo se añade al final.

## 4. Formatos admitidos y su estructura

El formato de entrada se reconoce por la extensión (`.csv`, `.xml` o `.json`, da igual en mayúsculas o minúsculas). Los tres formatos representan los mismos datos:

- **Pedido:** número (entero), fecha (`AAAA-MM-DD`) y cliente.
- **Cliente:** id (entero) y nombre.
- **Línea de pedido:** artículo, unidades (entero) y precio unitario (decimal con punto). Un pedido puede tener **cero o más líneas**.

### CSV

- Codificación UTF-8 y separador coma.
- La primera línea tiene que ser **exactamente** esta cabecera:

```
numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario
```

- Hay **una fila por cada línea de pedido**, y las filas con el mismo `numero` se agrupan en un único pedido.
- Un pedido sin líneas se escribe con las tres últimas columnas vacías: `4001,2026-05-02,2,Sin Líneas,,,`
- Un texto que contenga comas o comillas va entre comillas dobles, y las comillas interiores se escriben duplicadas: `"López, Hermanos"`, `"Tornillo de 3"" galvanizado"`.
- Las líneas en blanco se ignoran.

```
numero,fecha,clienteId,clienteNombre,articulo,unidades,precioUnitario
1001,2026-03-15,7,Ferretería Núñez,Martillo,2,12.5
1001,2026-03-15,7,Ferretería Núñez,Clavos,100,0.05
1002,2026-03-16,8,Bricolaje Peña,Taladro,1,89.9
```

### XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<pedidos>
    <pedido numero="1001" fecha="2026-03-15">
        <cliente id="7">Ferretería Núñez</cliente>
        <lineas>
            <linea articulo="Martillo" unidades="2" precioUnitario="12.5"/>
            <linea articulo="Clavos" unidades="100" precioUnitario="0.05"/>
        </lineas>
    </pedido>
</pedidos>
```

Un pedido sin líneas se escribe con `<lineas/>`.

### JSON

La raíz es un **array** de pedidos:

```json
[
  {
    "numero": 1001,
    "fecha": "2026-03-15",
    "cliente": {"id": 7, "nombre": "Ferretería Núñez"},
    "lineas": [
      {"articulo": "Martillo", "unidades": 2, "precioUnitario": 12.5}
    ]
  }
]
```

Un pedido sin líneas se escribe con `"lineas": []`.

## 5. Errores frecuentes y qué significan

| Mensaje | Qué pasa | Qué hacer |
|---|---|---|
| `Uso: java Conversor <fichero> --a <csv\|xml\|json>` | Faltan argumentos o están mal escritos (por ejemplo `-a` en vez de `--a`) | Escribir exactamente: fichero, `--a`, formato |
| `No encuentro el fichero '…'` | La ruta o el nombre del fichero no son correctos | Comprobar la ruta; las rutas relativas cuentan desde la carpeta donde se ejecuta el programa |
| `El fichero: … no tiene uno de los formatos admitidos` | La extensión no es `.csv`, `.xml` ni `.json` | Renombrar el fichero o convertirlo antes |
| `Formato de salida no soportado: …` | Lo que va detrás de `--a` no es `csv`, `xml` ni `json` | Usar uno de los tres |
| `La salida coincide con la entrada; elige otro formato.` | Se ha pedido convertir un `_convertido.csv` a CSV, y se sobrescribiría a sí mismo | Elegir otro formato de salida |
| `El fichero CSV está vacío` | El CSV no tiene ni siquiera la cabecera | Revisar el fichero |
| `El fichero no tiene la cabecera de CSV esperada` | La primera línea no es la cabecera exacta, o el fichero usa `;` como separador, o tiene BOM (ver punto 6) | Corregir la cabecera |
| `Línea N: se esperaban 7 campos y hay M` | A esa fila le faltan o le sobran columnas, a menudo por una coma sin comillas dentro de un texto | Poner comillas al texto que lleve comas |
| `Línea N: dato mal formado (…)` | Un número no es un número, o la fecha no sigue el formato `AAAA-MM-DD` | Corregir el dato de esa línea |
| `XML mal formado en la línea N, columna M` | El XML tiene una etiqueta sin cerrar, un carácter inválido o un `<!DOCTYPE>` | Revisar el XML en esa posición |
| `Dato mal formado en el XML: …` | Un atributo numérico o la fecha no son válidos, o falta el atributo | Revisar los atributos `numero`, `fecha`, `id`, `unidades` y `precioUnitario` |
| `JSON mal formado en la posición N: …` | El JSON tiene un error de sintaxis (falta una coma, una llave o unas comillas…). `N` es el número de carácter contando desde 0 | Revisar el JSON cerca de esa posición |
| `No he podido leer o escribir un fichero…` | No hay permisos, el disco está lleno o ya existe una carpeta con el nombre de la salida | Ver el detalle en `conversor.log` |

Cuando el XML está mal formado, aparece además una línea `[Fatal Error] …` que imprime el propio analizador de Java. Es normal.

## 6. Qué no hace: límites conocidos

Todo lo de esta lista está comprobado ejecutando el programa. No son suposiciones.

**Datos que se pierden o cambian sin aviso**

- **JSON con números que no caben en un `int`:** `"numero": 2147483648` se convierte en `2147483647` sin dar ningún error. En CSV y en XML el mismo caso sí da error.
- **JSON con decimales en campos enteros:** `"unidades": 2.9` se guarda como `2`, sin avisar.
- **Pedidos repetidos en CSV con datos distintos:** si dos filas tienen el mismo `numero` pero distinta fecha o distinto cliente, se usan los datos de la primera y los de las demás se descartan sin avisar.
- **Precios grandes:** un precio como `12345678.0` se escribe en notación científica (`1.2345678E7`). El propio conversor lo vuelve a leer bien, pero otros programas, como Excel, pueden no entenderlo. Los precios se guardan como `double`, así que no tienen precisión exacta de céntimos.
- **Sobrescritura:** si el fichero de salida ya existe, **se sobrescribe sin preguntar**.

**Entradas que no admite**

- **Saltos de línea dentro de un campo CSV:** el conversor los escribe bien entre comillas, pero no puede volver a leerlos y da el error `se esperaban 7 campos`. Un nombre con salto de línea puede pasar de JSON a CSV, pero no volver.
- **CSV con BOM** (como los que guarda Excel con «CSV UTF-8»): lo rechaza con el error de cabecera.
- **CSV con otro separador** (`;`) **o con las columnas en otro orden:** no se admiten. La cabecera tiene que ser exacta.
- **XML con `<!DOCTYPE>`:** se rechaza siempre. Se hace a propósito, para evitar ataques XXE.
- **JSON con tipos incorrectos o con claves que faltan** (por ejemplo `"numero": "abc"`, o un pedido sin `"cliente"`): **el programa termina con un error de Java** (`ClassCastException` o `NullPointerException`) en lugar de mostrar un mensaje claro, y ese error no se anota en `conversor.log`.
- **JSON cuya raíz no es un array:** también termina con `ClassCastException`.

**Validaciones que no hace**

- No comprueba que las unidades o los precios sean positivos, ni que la fecha tenga sentido para el negocio: acepta tanto `-5` unidades como fechas del año 3000.
- Lee el fichero entero en memoria, así que no está pensado para ficheros muy grandes.

**Comportamiento con los códigos de salida**

- Los errores de argumentos, de formato de salida no soportado y de salida igual a la entrada muestran un mensaje, pero **terminan con código 0**, como si todo hubiera ido bien. Hay que tenerlo en cuenta si se usa el conversor desde un script. El resto de errores termina con código 1.
- En los errores de JSON, el mensaje dice «Dato mal formado en el XML» aunque el fichero sea un JSON.

**Otros**

- Solo convierte un fichero cada vez.
- La salida se guarda siempre en la misma carpeta que la entrada y no se puede elegir otra.
