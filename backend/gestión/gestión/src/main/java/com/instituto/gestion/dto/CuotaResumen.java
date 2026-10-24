package com.instituto.gestion.dto;

import com.instituto.gestion.model.EstadoCuota;

import java.time.LocalDate;

/** Detalle resumido de una cuota adeudada por un alumno moroso. */
public class CuotaResumen {

    private Long id;
    private Long cursoId;
    private String cursoNombre;
    private Integer mes;
    private Integer anio;
    private String periodo;
    private Double monto;
    private LocalDate fechaVencimiento;
    private EstadoCuota estado;
    private long diasMora;

    public CuotaResumen(Long id, Long cursoId, String cursoNombre, Integer mes, Integer anio,
                        String periodo, Double monto, LocalDate fechaVencimiento,
                        EstadoCuota estado, long diasMora) {
        this.id = id;
        this.cursoId = cursoId;
        this.cursoNombre = cursoNombre;
        this.mes = mes;
        this.anio = anio;
        this.periodo = periodo;
        this.monto = monto;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.diasMora = diasMora;
    }

    public Long getId() { return id; }
    public Long getCursoId() { return cursoId; }
    public String getCursoNombre() { return cursoNombre; }
    public Integer getMes() { return mes; }
    public Integer getAnio() { return anio; }
    public String getPeriodo() { return periodo; }
    public Double getMonto() { return monto; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public EstadoCuota getEstado() { return estado; }
    public long getDiasMora() { return diasMora; }
}
