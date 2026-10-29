package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fila de carga de notas: un alumno activo del curso y su nota en el examen. */
public class NotaExamenAlumnoResponse {

    private final Long alumnoId;
    private final String alumnoNombre;
    private final String alumnoDni;
    private final String alumnoLegajo;
    private final Long calificacionId;
    private final BigDecimal nota;
    private final String observaciones;
    private final LocalDate fechaCarga;
    private final Boolean aprobado;

    public NotaExamenAlumnoResponse(
            Long alumnoId,
            String alumnoNombre,
            String alumnoDni,
            String alumnoLegajo,
            Long calificacionId,
            BigDecimal nota,
            String observaciones,
            LocalDate fechaCarga,
            Boolean aprobado
    ) {
        this.alumnoId = alumnoId;
        this.alumnoNombre = alumnoNombre;
        this.alumnoDni = alumnoDni;
        this.alumnoLegajo = alumnoLegajo;
        this.calificacionId = calificacionId;
        this.nota = nota;
        this.observaciones = observaciones;
        this.fechaCarga = fechaCarga;
        this.aprobado = aprobado;
    }

    public Long getAlumnoId() { return alumnoId; }
    public String getAlumnoNombre() { return alumnoNombre; }
    public String getAlumnoDni() { return alumnoDni; }
    public String getAlumnoLegajo() { return alumnoLegajo; }
    public Long getCalificacionId() { return calificacionId; }
    public BigDecimal getNota() { return nota; }
    public String getObservaciones() { return observaciones; }
    public LocalDate getFechaCarga() { return fechaCarga; }
    public Boolean getAprobado() { return aprobado; }
}
