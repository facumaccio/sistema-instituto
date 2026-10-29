package com.instituto.gestion.dto;

import java.math.BigDecimal;

/** Nota de un alumno dentro de un guardado masivo de un examen. */
public class CalificacionItemRequest {

    private Long alumnoId;
    private BigDecimal nota;
    private String observaciones;

    public CalificacionItemRequest() {}

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
