package ejemplos;

import java.math.BigDecimal;

/** Tema 4: el objeto que guarda una fila de empleados. Un record: solo datos, y no cambia. */
public record Empleado(int id, String nombre, BigDecimal salario) {
}
