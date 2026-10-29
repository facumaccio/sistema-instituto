package com.instituto.gestion.model;

/** Roles disponibles para los usuarios del sistema. */
public enum RolUsuario {

    DUEÑO("Dueño"),
    ADMINISTRADOR("Administrador"),
    PROFESOR("Profesor");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
