package com.instituto.gestion.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento del calendario académico. Puede ser general del instituto o estar
 * asociado a un curso puntual (por ejemplo, un examen o una entrega de TP).
 */
@Entity
@Table(
    name = "evento_calendario",
    indexes = {
        @Index(name = "idx_evento_calendario_fecha_inicio", columnList = "fecha_inicio"),
        @Index(name = "idx_evento_calendario_tipo", columnList = "tipo"),
        @Index(name = "idx_evento_calendario_curso", columnList = "curso_id")
    }
)
public class EventoCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "hora")
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipo;

    @Column(nullable = false, length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    public EventoCalendario() {}

    /**
     * Asegura que siempre exista un color persistido, incluso si el evento se
     * guarda desde otro punto de la aplicación que no pase por el servicio.
     */
    @PrePersist
    @PreUpdate
    private void asegurarColor() {
        if ((color == null || color.isBlank()) && tipo != null) {
            color = tipo.getColor();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}
