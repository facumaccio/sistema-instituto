package com.instituto.gestion.service;

import com.instituto.gestion.dto.LoginRequest;
import com.instituto.gestion.dto.LoginResponse;
import com.instituto.gestion.dto.RegistroRequest;
import com.instituto.gestion.dto.UsuarioResponse;
import com.instituto.gestion.model.RolUsuario;
import com.instituto.gestion.model.Usuario;
import com.instituto.gestion.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{5,}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Pattern DNI_PATTERN =
            Pattern.compile("^\\d{6,9}$");

    private static final int PASSWORD_MAX_BYTES = 72;
    private static final int NOMBRE_MAX_LENGTH = 120;
    private static final int CORREO_MAX_LENGTH = 120;
    private static final int NOMBRE_USUARIO_MAX_LENGTH = 120;
    private static final int DNI_MAX_LENGTH = 20;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Crea una cuenta de usuario. La primera cuenta del sistema queda como
     * DUEÑO; las siguientes se crean con el rol PROFESOR. Los roles
     * administrativos se asignarán desde el módulo de administración.
     */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del usuario son obligatorios.");
        }

        String nombre = normalizarNombre(request.getNombre());
        String dni = normalizarDni(request.getDni());
        String correo = normalizarCorreo(request.getCorreo());

        validarNombre(nombre);
        validarDni(dni);
        validarCorreo(correo);
        validarPassword(request.getPassword());

        if (repository.existsByCorreo(correo) || repository.existsByNombreUsuario(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese correo electrónico.");
        }

        if (repository.existsByDni(dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese DNI.");
        }

        boolean esPrimerUsuario = repository.count() == 0;
        RolUsuario rol = esPrimerUsuario ? RolUsuario.DUEÑO : RolUsuario.PROFESOR;

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setDni(dni);
        usuario.setCorreo(correo);
        usuario.setNombreUsuario(correo);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);

        Usuario guardado = repository.save(usuario);

        return new UsuarioResponse(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getDni(),
                guardado.getCorreo(),
                guardado.getNombreUsuario(),
                guardado.getRol(),
                guardado.getRol().getEtiqueta());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá tu usuario y contraseña.");
        }

        String identificador = normalizarIdentificador(request.getNombreUsuario());
        String password = request.getPassword();

        if (identificador.isEmpty() || password == null || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá tu usuario y contraseña.");
        }

        Usuario usuario = repository.findByNombreUsuario(identificador)
                .or(() -> repository.findByCorreo(identificador.toLowerCase()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }

        return new LoginResponse(
                "Login correcto.",
                usuario.getNombreUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getRol().getEtiqueta());
    }

    private String normalizarNombre(String nombre) {
        return nombre == null ? "" : nombre.trim();
    }

    private String normalizarDni(String dni) {
        return dni == null ? "" : dni.replaceAll("[.\\s-]", "");
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    private String normalizarIdentificador(String identificador) {
        return identificador == null ? "" : identificador.trim();
    }

    private void validarNombre(String nombre) {
        if (nombre.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }
        if (nombre.length() > NOMBRE_MAX_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar los " + NOMBRE_MAX_LENGTH + " caracteres.");
        }
    }

    private void validarDni(String dni) {
        if (dni.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI es obligatorio.");
        }
        if (dni.length() > DNI_MAX_LENGTH || !DNI_PATTERN.matcher(dni).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI debe contener solo números (entre 6 y 9 dígitos).");
        }
    }

    private void validarCorreo(String correo) {
        if (correo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es obligatorio.");
        }
        if (correo.length() > CORREO_MAX_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico no puede superar los " + CORREO_MAX_LENGTH + " caracteres.");
        }
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ingresá un correo electrónico válido.");
        }
        if (correo.length() > NOMBRE_USUARIO_MAX_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es demasiado largo.");
        }
    }

    private void validarPassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 5 caracteres, incluir una letra mayúscula y un número.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es demasiado larga. Usá un máximo de " + PASSWORD_MAX_BYTES + " bytes.");
        }
    }
}
