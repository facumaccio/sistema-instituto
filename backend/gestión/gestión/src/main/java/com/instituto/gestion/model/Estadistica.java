package com.instituto.gestion.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Foto estadística del instituto en un período mensual. Se guarda como
 * snapshot para poder comparar la evolución entre cierres.
 */
@Entity
@Table(
    name = "estadistica",
    indexes = {
        @Index(name = "idx_estadistica_periodo", columnList = "periodo"),
        @Index(name = "idx_estadistica_fecha_generacion", columnList = "fecha_generacion")
    }
)
public class Estadistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /** Período mensual en formato YYYY-MM, por ejemplo 2026-10. */
    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "total_alumnos_activos", nullable = false)
    private Integer totalAlumnosActivos;

    @Column(name = "total_profesores", nullable = false)
    private Integer totalProfesores;

    @Column(name = "total_cursos", nullable = false)
    private Integer totalCursos;

    @Column(name = "tasa_asistencia_promedio", nullable = false)
    private Double tasaAsistenciaPromedio;

    @Column(name = "total_ingresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIngresos;

    @Column(name = "total_egresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalEgresos;

    @Column(name = "balance_neto", nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceNeto;

    /** Desgloses completos: cursos, evolución, flujo mensual, gastos y eventos. */
    @Column(name = "datos_detallados_json", columnDefinition = "TEXT")
    private String datosDetalladosJson;

    public Estadistica() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getTotalAlumnosActivos() { return totalAlumnosActivos; }
    public void setTotalAlumnosActivos(Integer totalAlumnosActivos) { this.totalAlumnosActivos = totalAlumnosActivos; }

    public Integer getTotalProfesores() { return totalProfesores; }
    public void setTotalProfesores(Integer totalProfesores) { this.totalProfesores = totalProfesores; }

    public Integer getTotalCursos() { return totalCursos; }
    public void setTotalCursos(Integer totalCursos) { this.totalCursos = totalCursos; }

    public Double getTasaAsistenciaPromedio() { return tasaAsistenciaPromedio; }
    public void setTasaAsistenciaPromedio(Double tasaAsistenciaPromedio) { this.tasaAsistenciaPromedio = tasaAsistenciaPromedio; }

    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }

    public BigDecimal getTotalEgresos() { return totalEgresos; }
    public void setTotalEgresos(BigDecimal totalEgresos) { this.totalEgresos = totalEgresos; }

    public BigDecimal getBalanceNeto() { return balanceNeto; }
    public void setBalanceNeto(BigDecimal balanceNeto) { this.balanceNeto = balanceNeto; }

    public String getDatosDetalladosJson() { return datosDetalladosJson; }
    public void setDatosDetalladosJson(String datosDetalladosJson) { this.datosDetalladosJson = datosDetalladosJson; }
}
