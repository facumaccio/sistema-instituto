package com.instituto.gestion.service;

import com.instituto.gestion.model.Profesor;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ProfesorService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s.]{6,40}$");
    private static final String PREFIJO_FOTO = "data:image/";
    private static final int MAX_FOTO = 2_000_000;

    @Autowired
    private ProfesorRepository repository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public List<Profesor> obtenerTodos() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "apellido", "nombre"));
    }

    @Transactional(readOnly = true)
    public Profesor obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el profesor con ID " + id + "."));
    }

    @Transactional(readOnly = true)
    public Profesor obtenerPorDni(String dni) {
        String dniNormalizado = normalizarDni(dni);
        return repository.findByDni(dniNormalizado).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró un profesor con el DNI " + dniNormalizado + "."));
    }

    @Transactional
    public Profesor guardarProfesor(Profesor profesor) {
        validarYNormalizar(profesor, null);
        verificarDniDisponible(profesor.getDni(), null);
        return repository.save(profesor);
    }

    @Transactional
    public Profesor actualizarProfesor(Long id, Profesor profesorActualizado) {
        Profesor profesor = obtenerPorId(id);
        validarYNormalizar(profesorActualizado, id);
        verificarDniDisponible(profesorActualizado.getDni(), id);

        profesor.setNombre(profesorActualizado.getNombre());
        profesor.setApellido(profesorActualizado.getApellido());
        profesor.setDni(profesorActualizado.getDni());
        profesor.setNya(profesorActualizado.getNya());
        profesor.setFechaNacimiento(profesorActualizado.getFechaNacimiento());
        profesor.setEdad(profesorActualizado.getEdad());
        profesor.setEmail(profesorActualizado.getEmail());
        profesor.setTelefono(profesorActualizado.getTelefono());
        profesor.setEspecialidad(profesorActualizado.getEspecialidad());
        profesor.setFotoPerfil(profesorActualizado.getFotoPerfil());

        return repository.save(profesor);
    }

    @Transactional
    public void eliminarProfesor(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró el profesor con ID " + id + ".");
        }

        if (cursoRepository.existsByProfesor_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el profesor porque tiene cursos asignados. Reasigná esos cursos primero.");
        }

        repository.deleteById(id);
    }

    private void validarYNormalizar(Profesor profesor, Long idExcluir) {
        if (profesor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del profesor son obligatorios.");
        }

        String nombre = limpiar(profesor.getNombre());
        String apellido = limpiar(profesor.getApellido());
        if (nombre == null || apellido == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre y el apellido son obligatorios.");
        }
        if (nombre.length() > 80 || apellido.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre y el apellido no pueden superar los 80 caracteres.");
        }

        String dni = normalizarDni(profesor.getDni());
        if (!dni.matches("\\d{7,8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El DNI debe ser un número válido de 7 u 8 cifras.");
        }

        LocalDate fechaNacimiento = profesor.getFechaNacimiento();
        if (fechaNacimiento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento es obligatoria.");
        }
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser posterior a hoy.");
        }
        int edad = Period.between(fechaNacimiento, hoy).getYears();
        if (edad < 18 || edad > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento debe corresponder a una persona de entre 18 y 100 años.");
        }

        String email = limpiar(profesor.getEmail());
        if (email != null) {
            if (email.length() > 120 || !EMAIL.matcher(email).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El email no tiene un formato válido.");
            }
            email = email.toLowerCase(Locale.ROOT);
        }

        String telefono = limpiar(profesor.getTelefono());
        if (telefono != null && !TELEFONO.matcher(telefono).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El teléfono no tiene un formato válido.");
        }

        String especialidad = limpiar(profesor.getEspecialidad());
        if (especialidad != null && especialidad.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La especialidad o título no puede superar los 120 caracteres.");
        }

        profesor.setNombre(nombre);
        profesor.setApellido(apellido);
        profesor.setDni(dni);
        profesor.setNya(nombre + " " + apellido);
        profesor.setFechaNacimiento(fechaNacimiento);
        profesor.setEdad(edad);
        profesor.setEmail(email);
        profesor.setTelefono(telefono);
        profesor.setEspecialidad(especialidad);
        profesor.setFotoPerfil(normalizarFotoPerfil(profesor.getFotoPerfil()));
    }

    private void verificarDniDisponible(String dni, Long idExcluir) {
        if (idExcluir == null) {
            if (repository.existsByDni(dni)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un profesor con el DNI " + dni + ".");
            }
        } else if (repository.existsByDniAndIdNot(dni, idExcluir)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe otro profesor con el DNI " + dni + ".");
        }

        if (alumnoRepository.existsByDni(dni)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un alumno con el DNI " + dni + ". El DNI debe ser único por persona.");
        }
    }

    private String normalizarDni(String dni) {
        return dni == null ? "" : dni.replaceAll("[.\\s]", "").trim();
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private String normalizarFotoPerfil(String foto) {
        String valor = limpiar(foto);
        if (valor == null) {
            return null;
        }

        if (valor.length() > MAX_FOTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La foto de perfil es demasiado grande. Probá con una imagen más liviana.");
        }

        int coma = valor.indexOf(',');
        if (valor.toLowerCase(Locale.ROOT).startsWith(PREFIJO_FOTO)) {
            if (coma < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La foto de perfil no tiene un formato Base64 válido.");
            }

            String cabecera = valor.substring(0, coma).toLowerCase(Locale.ROOT);
            if (!cabecera.matches("data:image/(png|jpe?g|webp);base64")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La foto debe ser una imagen JPG, PNG o WebP.");
            }

            String base64 = valor.substring(coma + 1).replaceAll("\\s", "");
            if (!base64.matches("[A-Za-z0-9+/=]+")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La foto de perfil no tiene un formato Base64 válido.");
            }
            return cabecera + "," + base64;
        }

        String base64 = valor.replaceAll("\\s", "");
        if (!base64.matches("[A-Za-z0-9+/=]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La foto de perfil no tiene un formato Base64 válido.");
        }
        return "data:image/jpeg;base64," + base64;
    }
}
