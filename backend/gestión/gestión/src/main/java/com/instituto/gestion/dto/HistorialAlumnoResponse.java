package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.util.List;

/** Historial de notas de un alumno dentro de un curso y su promedio. */
public class HistorialAlumnoResponse {

    private final Long alumnoId;
    private final String alumnoNombre;
    private final String alumnoDni;
    private final String alumnoLegajo;
    private final BigDecimal promedio;
    private final int cantidadNotas;
    private final List<CalificacionResponse> calificaciones;

    public HistorialAlumnoResponse(
            Long alumnoId,
            String alumnoNombre,
            String alumnoDni,
            String alumnoLegajo,
            BigDecimal promedio,
            int cantidadNotas,
            List<CalificacionResponse> calificaciones
    ) {
        this.alumnoId = alumnoId;
        this.alumnoNombre = alumnoNombre;
        this.alumnoDni = alumnoDni;
        this.alumnoLegajo = alumnoLegajo;
        this.promedio = promedio;
        this.cantidadNotas = cantidadNotas;
        this.calificaciones = calificaciones;
    }

    public Long getAlumnoId() { return alumnoId; }
    public String getAlumnoNombre() { return alumnoNombre; }
    public String getAlumnoDni() { return alumnoDni; }
    public String getAlumnoLegajo() { return alumnoLegajo; }
    public BigDecimal getPromedio() { return promedio; }
    public int getCantidadNotas() { return cantidadNotas; }
    public List<CalificacionResponse> getCalificaciones() { return calificaciones; }
}
