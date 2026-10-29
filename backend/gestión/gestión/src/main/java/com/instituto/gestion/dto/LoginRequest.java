package com.instituto.gestion.dto;

/** Credenciales enviadas desde la pantalla de login. */
public class LoginRequest {

    private String nombreUsuario;
    private String password;

    public LoginRequest() {}

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
