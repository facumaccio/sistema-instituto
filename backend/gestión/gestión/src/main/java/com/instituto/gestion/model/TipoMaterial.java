package com.instituto.gestion.model;

/**
 * Clasificación de los recursos externos que se comparten en un curso.
 */
public enum TipoMaterial {

    PDF_DOCUMENTO("PDF / Documento"),
    VIDEO("Video"),
    CARPETA_DRIVE("Carpeta Drive"),
    IMAGEN("Imagen"),
    LINK_WEB("Link web"),
    OTRO("Otro");

    private final String etiqueta;

    TipoMaterial(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
