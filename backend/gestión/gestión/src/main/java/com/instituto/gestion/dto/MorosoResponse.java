package com.instituto.gestion.dto;

import java.util.List;

/** Alumno con al menos una cuota vencida impaga. */
public class MorosoResponse {

    private Long alumnoId;
    private String nya;
    private String dni;
    private String legajo;
    private int cuotasVencidas;
    private double totalAdeudado;
    private long diasMoraMaximo;
    private List<CuotaResumen> cuotas;

    public MorosoResponse(Long alumnoId, String nya, String dni, String legajo,
                          int cuotasVencidas, double totalAdeudado, long diasMoraMaximo,
                          List<CuotaResumen> cuotas) {
        this.alumnoId = alumnoId;
        this.nya = nya;
        this.dni = dni;
        this.legajo = legajo;
        this.cuotasVencidas = cuotasVencidas;
        this.totalAdeudado = totalAdeudado;
        this.diasMoraMaximo = diasMoraMaximo;
        this.cuotas = cuotas;
    }

    public Long getAlumnoId() { return alumnoId; }
    public String getNya() { return nya; }
    public String getDni() { return dni; }
    public String getLegajo() { return legajo; }
    public int getCuotasVencidas() { return cuotasVencidas; }
    public double getTotalAdeudado() { return totalAdeudado; }
    public long getDiasMoraMaximo() { return diasMoraMaximo; }
    public List<CuotaResumen> getCuotas() { return cuotas; }
}
