package com.instituto.gestion.dto;

import com.instituto.gestion.model.MetodoPago;

import java.time.LocalDate;

/**
 * Permite registrar el pago indicando una fecha distinta a la de hoy y el
 * método utilizado. Ambos datos son opcionales: la fecha por defecto es hoy y
 * el método por defecto es efectivo.
 */
public class RegistrarPagoRequest {

    private LocalDate fechaPago;
    private MetodoPago metodoPago;

    public RegistrarPagoRequest() {}

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
}
