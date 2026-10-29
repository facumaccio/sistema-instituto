package com.instituto.gestion.dto;

import com.instituto.gestion.model.RolUsuario;

/** Respuesta entregada al frontend cuando el login es correcto. */
public class LoginResponse {

    private final String mensaje;
    private final String nombreUsuario;
    private final String nombre;
    private final String correo;
    private final RolUsuario rol;
    private final String rolEtiqueta;

    public LoginResponse(
            String mensaje,
            String nombreUsuario,
            String nombre,
            String correo,
            RolUsuario rol,
            String rolEtiqueta) {
        this.mensaje = mensaje;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.rolEtiqueta = rolEtiqueta;
    }

    public String getMensaje() { return mensaje; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public RolUsuario getRol() { return rol; }
    public String getRolEtiqueta() { return rolEtiqueta; }
}
