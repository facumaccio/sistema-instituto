package com.instituto.gestion.model;

/**
 * Clasificación de los eventos del calendario académico.
 * Cada tipo tiene una etiqueta visible y un color fijo para que la grilla
 * mensual se pueda interpretar de un vistazo.
 */
public enum TipoEvento {

    EXAMEN("Examen", "#dc2626"),
    FESTEJO("Festejo", "#7c3aed"),
    FERIADO("Feriado", "#0ea5e9"),
    REUNION("Reunión", "#f97316"),
    ENTREGA_TP("Entrega de TP", "#16a34a"),
    OTRO("Otro", "#64748b");

    private final String etiqueta;
    private final String color;

    TipoEvento(String etiqueta, String color) {
        this.etiqueta = etiqueta;
        this.color = color;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getColor() {
        return color;
    }
}
