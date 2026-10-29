package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Nota lista para mostrar en el historial y en la carga de un examen. */
public class CalificacionResponse {

    private final Long id;
    private final Long alumnoId;
    private final String alumnoNombre;
    private final String alumnoDni;
    private final String alumnoLegajo;
    private final Long cursoId;
    private final Long eventoId;
    private final String eventoNombre;
    private final LocalDate eventoFecha;
    private final LocalTime eventoHora;
    private final BigDecimal nota;
    private final String observaciones;
    private final LocalDate fechaCarga;
    private final Boolean aprobado;

    public CalificacionResponse(
            Long id,
            Long alumnoId,
            String alumnoNombre,
            String alumnoDni,
            String alumnoLegajo,
            Long cursoId,
            Long eventoId,
            String eventoNombre,
            LocalDate eventoFecha,
            LocalTime eventoHora,
            BigDecimal nota,
            String observaciones,
            LocalDate fechaCarga,
            Boolean aprobado
    ) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.alumnoNombre = alumnoNombre;
        this.alumnoDni = alumnoDni;
        this.alumnoLegajo = alumnoLegajo;
        this.cursoId = cursoId;
        this.eventoId = eventoId;
        this.eventoNombre = eventoNombre;
        this.eventoFecha = eventoFecha;
        this.eventoHora = eventoHora;
        this.nota = nota;
        this.observaciones = observaciones;
        this.fechaCarga = fechaCarga;
        this.aprobado = aprobado;
    }

    public Long getId() { return id; }
    public Long getAlumnoId() { return alumnoId; }
    public String getAlumnoNombre() { return alumnoNombre; }
    public String getAlumnoDni() { return alumnoDni; }
    public String getAlumnoLegajo() { return alumnoLegajo; }
    public Long getCursoId() { return cursoId; }
    public Long getEventoId() { return eventoId; }
    public String getEventoNombre() { return eventoNombre; }
    public LocalDate getEventoFecha() { return eventoFecha; }
    public LocalTime getEventoHora() { return eventoHora; }
    public BigDecimal getNota() { return nota; }
    public String getObservaciones() { return observaciones; }
    public LocalDate getFechaCarga() { return fechaCarga; }
    public Boolean getAprobado() { return aprobado; }
}
