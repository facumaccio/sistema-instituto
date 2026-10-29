package com.instituto.gestion.dto;

import com.instituto.gestion.model.RolUsuario;

/** Datos públicos del usuario, sin incluir la contraseña. */
public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String dni;
    private final String correo;
    private final String nombreUsuario;
    private final RolUsuario rol;
    private final String rolEtiqueta;

    public UsuarioResponse(
            Long id,
            String nombre,
            String dni,
            String correo,
            String nombreUsuario,
            RolUsuario rol,
            String rolEtiqueta) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.correo = correo;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.rolEtiqueta = rolEtiqueta;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
    public String getCorreo() { return correo; }
    public String getNombreUsuario() { return nombreUsuario; }
    public RolUsuario getRol() { return rol; }
    public String getRolEtiqueta() { return rolEtiqueta; }
}
