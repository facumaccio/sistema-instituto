package com.instituto.gestion.dto;

/** Datos para crear una cuenta de usuario. */
public class RegistroRequest {

    private String nombre;
    private String dni;
    private String correo;
    private String password;

    public RegistroRequest() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
