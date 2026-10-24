package com.instituto.gestion.dto;

import java.time.LocalDate;

/** Resultado de la generación masiva de cuotas. */
public class GenerarCuotasResponse {

    private Long cursoId;
    private String cursoNombre;
    private Integer mes;
    private Integer anio;
    private Double monto;
    private LocalDate fechaVencimiento;
    private int totalAlumnosActivos;
    private int cuotasCreadas;
    private int cuotasOmitidas;
    private String mensaje;

    public GenerarCuotasResponse(Long cursoId, String cursoNombre, Integer mes, Integer anio,
                                 Double monto, LocalDate fechaVencimiento, int totalAlumnosActivos,
                                 int cuotasCreadas, int cuotasOmitidas, String mensaje) {
        this.cursoId = cursoId;
        this.cursoNombre = cursoNombre;
        this.mes = mes;
        this.anio = anio;
        this.monto = monto;
        this.fechaVencimiento = fechaVencimiento;
        this.totalAlumnosActivos = totalAlumnosActivos;
        this.cuotasCreadas = cuotasCreadas;
        this.cuotasOmitidas = cuotasOmitidas;
        this.mensaje = mensaje;
    }

    public Long getCursoId() { return cursoId; }
    public String getCursoNombre() { return cursoNombre; }
    public Integer getMes() { return mes; }
    public Integer getAnio() { return anio; }
    public Double getMonto() { return monto; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public int getTotalAlumnosActivos() { return totalAlumnosActivos; }
    public int getCuotasCreadas() { return cuotasCreadas; }
    public int getCuotasOmitidas() { return cuotasOmitidas; }
    public String getMensaje() { return mensaje; }
}
