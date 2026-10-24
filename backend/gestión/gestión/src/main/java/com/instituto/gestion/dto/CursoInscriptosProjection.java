package com.instituto.gestion.dto;

/**
 * Proyección agregada de cursos con la cantidad de alumnos activos inscriptos.
 * Se resuelve en una sola consulta para evitar cargar todas las inscripciones.
 */
public interface CursoInscriptosProjection {

    Long getCursoId();

    String getNombre();

    String getTurno();

    Integer getCupo();

    String getProfesor();

    Long getInscriptos();
}
