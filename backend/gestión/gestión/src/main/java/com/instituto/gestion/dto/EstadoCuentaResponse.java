package com.instituto.gestion.dto;

/** Estado de cuenta consolidado de un alumno en un curso. */
public class EstadoCuentaResponse {

    private Long alumnoId;
    private String nya;
    private String dni;
    private String legajo;
    private Long cursoId;
    private String cursoNombre;
    private int cuotasTotales;
    private int pagadas;
    private int pendientes;
    private int vencidas;
    private double totalFacturado;
    private double totalPagado;
    private double saldo;
    private String estado; // MOROSO, CON_DEUDA, AL_DIA

    public EstadoCuentaResponse(Long alumnoId, String nya, String dni, String legajo,
                                Long cursoId, String cursoNombre, int cuotasTotales,
                                int pagadas, int pendientes, int vencidas,
                                double totalFacturado, double totalPagado, double saldo,
                                String estado) {
        this.alumnoId = alumnoId;
        this.nya = nya;
        this.dni = dni;
        this.legajo = legajo;
        this.cursoId = cursoId;
        this.cursoNombre = cursoNombre;
        this.cuotasTotales = cuotasTotales;
        this.pagadas = pagadas;
        this.pendientes = pendientes;
        this.vencidas = vencidas;
        this.totalFacturado = totalFacturado;
        this.totalPagado = totalPagado;
        this.saldo = saldo;
        this.estado = estado;
    }

    public Long getAlumnoId() { return alumnoId; }
    public String getNya() { return nya; }
    public String getDni() { return dni; }
    public String getLegajo() { return legajo; }
    public Long getCursoId() { return cursoId; }
    public String getCursoNombre() { return cursoNombre; }
    public int getCuotasTotales() { return cuotasTotales; }
    public int getPagadas() { return pagadas; }
    public int getPendientes() { return pendientes; }
    public int getVencidas() { return vencidas; }
    public double getTotalFacturado() { return totalFacturado; }
    public double getTotalPagado() { return totalPagado; }
    public double getSaldo() { return saldo; }
    public String getEstado() { return estado; }
}
