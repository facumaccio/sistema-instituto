package com.instituto.gestion.dto;

/** Cantidad de inscripciones registradas en un mes. */
public class EvolucionInscripciones {

    private final String periodo;
    private final String etiqueta;
    private final long total;

    public EvolucionInscripciones(String periodo, String etiqueta, long total) {
        this.periodo = periodo;
        this.etiqueta = etiqueta;
        this.total = total;
    }

    public String getPeriodo() { return periodo; }
    public String getEtiqueta() { return etiqueta; }
    public long getTotal() { return total; }
}
