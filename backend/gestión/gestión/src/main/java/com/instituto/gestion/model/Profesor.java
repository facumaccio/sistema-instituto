package com.instituto.gestion.model;

import jakarta.persistence.Entity;

@Entity
public class Profesor extends Persona {

    private String especialidad;
    private Double sueldoBasico;

    public Profesor() {}

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public Double getSueldoBasico() { return sueldoBasico; }
    public void setSueldoBasico(Double sueldoBasico) { this.sueldoBasico = sueldoBasico; }
}