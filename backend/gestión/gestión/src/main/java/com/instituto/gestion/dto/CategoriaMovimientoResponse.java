package com.instituto.gestion.dto;

import com.instituto.gestion.model.TipoMovimiento;

/** Categoría disponible para clasificar ingresos o egresos. */
public class CategoriaMovimientoResponse {

    private final Long id;
    private final String nombre;
    private final TipoMovimiento tipo;

    public CategoriaMovimientoResponse(Long id, String nombre, TipoMovimiento tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoMovimiento getTipo() { return tipo; }
}
