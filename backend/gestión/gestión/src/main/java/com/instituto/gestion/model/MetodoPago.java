package com.instituto.gestion.model;

/**
 * Medios de pago disponibles para registrar un movimiento de caja.
 */
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA,
    TARJETA_DEBITO,
    TARJETA_CREDITO,
    MERCADO_PAGO,
    CHEQUE,
    OTRO
}
