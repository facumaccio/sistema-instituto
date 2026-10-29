package com.instituto.gestion.dto;

import java.util.List;

/** Datos para guardar o actualizar todas las notas de un examen. */
public class CalificacionLoteRequest {

    private Long eventoId;
    private List<CalificacionItemRequest> calificaciones;

    public CalificacionLoteRequest() {}

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public List<CalificacionItemRequest> getCalificaciones() { return calificaciones; }
    public void setCalificaciones(List<CalificacionItemRequest> calificaciones) {
        this.calificaciones = calificaciones;
    }
}
