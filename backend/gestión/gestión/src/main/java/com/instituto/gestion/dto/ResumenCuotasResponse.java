package com.instituto.gestion.dto;

/** Totales generales para el tablero de pagos. */
public class ResumenCuotasResponse {

    private long pendientes;
    private long pagadas;
    private long vencidas;
    private double totalPendiente;
    private double totalPagado;
    private double totalVencido;
    private long alumnosMorosos;

    public ResumenCuotasResponse(long pendientes, long pagadas, long vencidas,
                                 double totalPendiente, double totalPagado, double totalVencido,
                                 long alumnosMorosos) {
        this.pendientes = pendientes;
        this.pagadas = pagadas;
        this.vencidas = vencidas;
        this.totalPendiente = totalPendiente;
        this.totalPagado = totalPagado;
        this.totalVencido = totalVencido;
        this.alumnosMorosos = alumnosMorosos;
    }

    public long getPendientes() { return pendientes; }
    public long getPagadas() { return pagadas; }
    public long getVencidas() { return vencidas; }
    public double getTotalPendiente() { return totalPendiente; }
    public double getTotalPagado() { return totalPagado; }
    public double getTotalVencido() { return totalVencido; }
    public long getAlumnosMorosos() { return alumnosMorosos; }
}
