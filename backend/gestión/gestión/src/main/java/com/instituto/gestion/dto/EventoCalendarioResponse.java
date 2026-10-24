package com.instituto.gestion.dto;

import com.instituto.gestion.model.TipoEvento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Evento del calendario listo para mostrar en la grilla y en las tarjetas. */
public class EventoCalendarioResponse {

    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final LocalTime hora;
    private final TipoEvento tipo;
    private final String tipoEtiqueta;
    private final String color;
    private final Long cursoId;
    private final String cursoNombre;
    private final String cursoTurno;

    public EventoCalendarioResponse(
            Long id,
            String titulo,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalTime hora,
            TipoEvento tipo,
            String tipoEtiqueta,
            String color,
            Long cursoId,
            String cursoNombre,
            String cursoTurno
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.hora = hora;
        this.tipo = tipo;
        this.tipoEtiqueta = tipoEtiqueta;
        this.color = color;
        this.cursoId = cursoId;
        this.cursoNombre = cursoNombre;
        this.cursoTurno = cursoTurno;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public LocalTime getHora() { return hora; }
    public TipoEvento getTipo() { return tipo; }
    public String getTipoEtiqueta() { return tipoEtiqueta; }
    public String getColor() { return color; }
    public Long getCursoId() { return cursoId; }
    public String getCursoNombre() { return cursoNombre; }
    public String getCursoTurno() { return cursoTurno; }
}
