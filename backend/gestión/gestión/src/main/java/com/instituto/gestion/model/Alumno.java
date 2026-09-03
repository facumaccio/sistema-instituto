package com.instituto.gestion.model;

import jakarta.persistence.Entity;

@Entity
public class Alumno extends Persona {

    private String legajo;
    private Boolean esRegular;

    // Constructor vacío obligatorio para el ORM
    public Alumno() {}

    // Getters y Setters
    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }

    public Boolean getEsRegular() { return esRegular; }
    public void setEsRegular(Boolean esRegular) { this.esRegular = esRegular; }
}