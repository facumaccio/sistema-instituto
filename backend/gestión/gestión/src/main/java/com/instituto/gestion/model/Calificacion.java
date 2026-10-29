package com.instituto.gestion.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Nota de un alumno en un examen concreto de un curso.
 *
 * <p>El examen es un {@link EventoCalendario} de tipo EXAMEN asociado al
 * curso. La combinación alumno + examen es única: si se vuelve a guardar la
 * nota se actualiza el registro existente.</p>
 */
@Entity
@Table(
    name = "calificacion",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_calificacion_alumno_evento",
            columnNames = {"alumno_id", "evento_examen_id"}
        )
    },
    indexes = {
        @Index(name = "idx_calificacion_curso", columnList = "curso_id"),
        @Index(name = "idx_calificacion_alumno", columnList = "alumno_id"),
        @Index(name = "idx_calificacion_evento", columnList = "evento_examen_id")
    }
)
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_examen_id", nullable = false)
    private EventoCalendario eventoExamen;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal nota;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDate fechaCarga;

    public Calificacion() {}

    @PrePersist
    private void completarFechaCarga() {
        if (fechaCarga == null) {
            fechaCarga = LocalDate.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public EventoCalendario getEventoExamen() { return eventoExamen; }
    public void setEventoExamen(EventoCalendario eventoExamen) { this.eventoExamen = eventoExamen; }

    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDate getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDate fechaCarga) { this.fechaCarga = fechaCarga; }
}
