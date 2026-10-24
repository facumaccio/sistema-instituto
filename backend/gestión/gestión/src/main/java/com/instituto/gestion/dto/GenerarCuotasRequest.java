package com.instituto.gestion.dto;

import java.time.LocalDate;

/** Datos necesarios para generar las cuotas de un período para todo un curso. */
public class GenerarCuotasRequest {

    private Long cursoId;
    private Integer mes;
    private Integer anio;
    private Double monto;
    private LocalDate fechaVencimiento;

    public GenerarCuotasRequest() {}

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
