package com.instituto.gestion.model;

/**
 * Estados posibles de una cuota.
 * - PENDIENTE: todavía no venció y no fue abonada.
 * - PAGADO: el alumno registró el pago.
 * - VENCIDO: pasó la fecha de vencimiento sin registrarse el pago.
 */
public enum EstadoCuota {
    PENDIENTE,
    PAGADO,
    VENCIDO
}
