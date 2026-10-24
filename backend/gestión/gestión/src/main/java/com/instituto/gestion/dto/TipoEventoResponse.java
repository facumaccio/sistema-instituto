package com.instituto.gestion.dto;

import com.instituto.gestion.model.TipoEvento;

/** Tipo de evento con su etiqueta y color para los filtros y la leyenda. */
public class TipoEventoResponse {

    private final TipoEvento valor;
    private final String etiqueta;
    private final String color;

    public TipoEventoResponse(TipoEvento valor, String etiqueta, String color) {
        this.valor = valor;
        this.etiqueta = etiqueta;
        this.color = color;
    }

    public TipoEvento getValor() { return valor; }
    public String getEtiqueta() { return etiqueta; }
    public String getColor() { return color; }
}
