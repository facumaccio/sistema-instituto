package com.instituto.gestion.model;

/**
 * Situación administrativa del alumno.
 *
 * <p>Los registros nunca se eliminan físicamente: una baja manual pasa a
 * DESAFILIADO y la inactividad prolongada pasa a INACTIVO_OCULTO.</p>
 */
public enum EstadoAlumno {
    ACTIVO,
    DESAFILIADO,
    INACTIVO_OCULTO
}
