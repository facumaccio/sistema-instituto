package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.util.List;

/** Balance de caja y desglose de egresos para el período consultado. */
public class ResumenFinancieroResponse {

    private final BigDecimal totalIngresos;
    private final BigDecimal totalEgresos;
    private final BigDecimal balanceNeto;
    private final long cantidadMovimientos;
    private final List<CategoriaResumen> gastosPorCategoria;

    public ResumenFinancieroResponse(
            BigDecimal totalIngresos,
            BigDecimal totalEgresos,
            BigDecimal balanceNeto,
            long cantidadMovimientos,
            List<CategoriaResumen> gastosPorCategoria
    ) {
        this.totalIngresos = totalIngresos;
        this.totalEgresos = totalEgresos;
        this.balanceNeto = balanceNeto;
        this.cantidadMovimientos = cantidadMovimientos;
        this.gastosPorCategoria = gastosPorCategoria;
    }

    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public BigDecimal getTotalEgresos() { return totalEgresos; }
    public BigDecimal getBalanceNeto() { return balanceNeto; }
    public long getCantidadMovimientos() { return cantidadMovimientos; }
    public List<CategoriaResumen> getGastosPorCategoria() { return gastosPorCategoria; }
}
