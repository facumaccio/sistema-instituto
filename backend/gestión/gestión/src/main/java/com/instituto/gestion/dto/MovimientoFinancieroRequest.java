package com.instituto.gestion.dto;

import com.instituto.gestion.model.MetodoPago;
import com.instituto.gestion.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos necesarios para registrar o editar un movimiento de caja. */
public class MovimientoFinancieroRequest {

    private TipoMovimiento tipo;
    private Long categoriaId;
    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;
    private MetodoPago metodoPago;

    public MovimientoFinancieroRequest() {}

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
}
