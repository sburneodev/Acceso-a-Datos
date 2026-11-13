-- ============================================================
-- Procedimientos almacenados de las actividades 1 y 5
--
-- DELIMITER cambia el separador de sentencias mientras se escribe el
-- cuerpo del procedimiento: sin el, el cliente cortaria en el primer
-- punto y coma de dentro y daria un error de sintaxis.
-- ============================================================

-- Los ficheros estan en UTF-8. Sin este SET NAMES, el cliente de MySQL los
-- interpreta como latin1 y las tildes se guardan mal: "Marquez" acaba siendo
-- "MÃ¡rquez" al leerlo desde Java.
SET NAMES utf8mb4;

USE empresa;

DROP PROCEDURE IF EXISTS obtener_empleado;

DELIMITER $$
CREATE PROCEDURE obtener_empleado(IN p_id INT)
BEGIN
    SELECT id, nombre, salario FROM empleados WHERE id = p_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS incrementar_salario;

DELIMITER $$
CREATE PROCEDURE incrementar_salario(
    IN  p_id         INT,
    IN  p_incremento DECIMAL(10,2),
    OUT p_nuevo      DECIMAL(10,2))
BEGIN
    DECLARE v_existe INT DEFAULT 0;
    SELECT COUNT(*) INTO v_existe FROM empleados WHERE id = p_id;
    IF v_existe = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El empleado indicado no existe';
    END IF;
    UPDATE empleados SET salario = salario + p_incremento WHERE id = p_id;
    SELECT salario INTO p_nuevo FROM empleados WHERE id = p_id;
END$$
DELIMITER ;
