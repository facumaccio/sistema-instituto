package com.instituto.gestion.dto;

import com.instituto.gestion.model.TipoMovimiento;

import java.math.BigDecimal;

/** Total acumulado y cantidad de movimientos de una categoría. */
public class CategoriaResumen {

    private final Long categoriaId;
    private final String nombre;
    private final TipoMovimiento tipo;
    private final BigDecimal total;
    private final long cantidad;

    public CategoriaResumen(Long categoriaId, String nombre, TipoMovimiento tipo, BigDecimal total, long cantidad) {
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.tipo = tipo;
        this.total = total;
        this.cantidad = cantidad;
    }

    public Long getCategoriaId() { return categoriaId; }
    public String getNombre() { return nombre; }
    public TipoMovimiento getTipo() { return tipo; }
    public BigDecimal getTotal() { return total; }
    public long getCantidad() { return cantidad; }
}
