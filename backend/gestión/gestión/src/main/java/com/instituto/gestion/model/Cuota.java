package com.instituto.gestion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Cuota mensual de un alumno para un curso determinado.
 * Se impide duplicar la cuota de un mismo alumno, curso, mes y año.
 */
@Entity
@Table(
    name = "cuota",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cuota_alumno_curso_periodo",
        columnNames = {"alumno_id", "curso_id", "periodo_mes", "periodo_anio"}
    )
)
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "periodo_mes", nullable = false)
    private Integer periodoMes;

    @Column(name = "periodo_anio", nullable = false)
    private Integer periodoAnio;

    @Column(nullable = false)
    private Double monto;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    /**
     * Indica si el pago de esta cuota ya generó su ingreso en Finanzas.
     * Se conserva en true aunque luego se elimine el movimiento para evitar
     * recrearlo automáticamente en el siguiente arranque.
     */
    @JsonIgnore
    @Column(name = "ingreso_registrado")
    private Boolean ingresoRegistrado = Boolean.FALSE;

    public Cuota() {}

    /** Período legible (MM/AAAA) que se incluye automáticamente en el JSON. */
    @Transient
    public String getPeriodo() {
        if (periodoMes == null || periodoAnio == null) {
            return null;
        }
        return String.format("%02d/%d", periodoMes, periodoAnio);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public Integer getPeriodoMes() { return periodoMes; }
    public void setPeriodoMes(Integer periodoMes) { this.periodoMes = periodoMes; }

    public Integer getPeriodoAnio() { return periodoAnio; }
    public void setPeriodoAnio(Integer periodoAnio) { this.periodoAnio = periodoAnio; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public EstadoCuota getEstado() { return estado; }
    public void setEstado(EstadoCuota estado) { this.estado = estado; }

    public Boolean getIngresoRegistrado() { return ingresoRegistrado; }
    public void setIngresoRegistrado(Boolean ingresoRegistrado) { this.ingresoRegistrado = ingresoRegistrado; }
}
