package com.instituto.gestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;

import java.time.LocalDate;

@Entity
public class Alumno extends Persona {

    private String legajo;
    private Boolean esRegular;

    @Column(length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVO'")
    private EstadoAlumno estado = EstadoAlumno.ACTIVO;

    @Column(name = "fecha_ultima_actividad")
    private LocalDate fechaUltimaActividad;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "motivo_baja", length = 255)
    private String motivoBaja;

    // Constructor vacío obligatorio para el ORM
    public Alumno() {}

    @PrePersist
    private void completarValoresIniciales() {
        if (estado == null) {
            estado = EstadoAlumno.ACTIVO;
        }
        if (fechaUltimaActividad == null) {
            fechaUltimaActividad = LocalDate.now();
        }
    }

    // Getters y Setters
    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }

    public Boolean getEsRegular() { return esRegular; }
    public void setEsRegular(Boolean esRegular) { this.esRegular = esRegular; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EstadoAlumno getEstado() {
        return estado != null ? estado : EstadoAlumno.ACTIVO;
    }
    public void setEstado(EstadoAlumno estado) { this.estado = estado; }

    public LocalDate getFechaUltimaActividad() { return fechaUltimaActividad; }
    public void setFechaUltimaActividad(LocalDate fechaUltimaActividad) {
        this.fechaUltimaActividad = fechaUltimaActividad;
    }

    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }

    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }
}
