package com.instituto.gestion.dto;

import com.instituto.gestion.model.MetodoPago;
import com.instituto.gestion.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimiento financiero listo para mostrar en la tabla de caja. */
public class MovimientoFinancieroResponse {

    private final Long id;
    private final TipoMovimiento tipo;
    private final Long categoriaId;
    private final String categoriaNombre;
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String descripcion;
    private final MetodoPago metodoPago;

    public MovimientoFinancieroResponse(
            Long id,
            TipoMovimiento tipo,
            Long categoriaId,
            String categoriaNombre,
            BigDecimal monto,
            LocalDate fecha,
            String descripcion,
            MetodoPago metodoPago
    ) {
        this.id = id;
        this.tipo = tipo;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.metodoPago = metodoPago;
    }

    public Long getId() { return id; }
    public TipoMovimiento getTipo() { return tipo; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public MetodoPago getMetodoPago() { return metodoPago; }
}
