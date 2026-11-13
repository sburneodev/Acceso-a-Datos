-- ============================================================
-- Base de datos de las actividades del RA2
--
-- Este script lo ejecuta el contenedor de MySQL la PRIMERA vez que
-- se levanta. Crea la base `empresa`, la tabla `empleados` con datos
-- y los dos procedimientos que necesitan las actividades 1 y 5.
--
-- El resto de tablas (proyectos, cuentas, logs, asignaciones...) las
-- crean las propias actividades al ejecutarse: es parte del ejercicio.
-- ============================================================

-- Los ficheros estan en UTF-8. Sin este SET NAMES, el cliente de MySQL los
-- interpreta como latin1 y las tildes se guardan mal: "Marquez" acaba siendo
-- "MÃ¡rquez" al leerlo desde Java.
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS empresa;
CREATE DATABASE empresa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE empresa;

CREATE TABLE empleados (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    salario DECIMAL(10,2) NOT NULL CHECK (salario > 0)
);

INSERT INTO empleados (nombre, salario) VALUES
('Ana Ruiz',      1500.00),
('Luis Márquez',  1800.00),
('Marta Peña',    2000.00),
('Pedro Íñiguez', 1750.00);
