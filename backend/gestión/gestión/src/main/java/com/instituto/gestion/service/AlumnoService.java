package com.instituto.gestion.service;

import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CuotaRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.instituto.gestion.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository repository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    public List<Alumno> obtenerTodos() {
        return repository.findAll();
    }

    public Alumno guardarAlumno(Alumno alumno) {
        validarYNormalizar(alumno);

        if (repository.existsByDni(alumno.getDni())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno con el DNI " + alumno.getDni() + ".");
        }

        return repository.save(alumno);
    }

    public Alumno actualizarAlumno(Long id, Alumno alumnoActualizado) {
        Alumno alumno = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el alumno con ID " + id + "."));

        validarYNormalizar(alumnoActualizado);

        if (repository.existsByDniAndIdNot(alumnoActualizado.getDni(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro alumno con el DNI " + alumnoActualizado.getDni() + ".");
        }

        alumno.setNya(alumnoActualizado.getNya());
        alumno.setDni(alumnoActualizado.getDni());
        alumno.setEdad(alumnoActualizado.getEdad());
        alumno.setLegajo(alumnoActualizado.getLegajo());
        alumno.setEsRegular(alumnoActualizado.getEsRegular());
        return repository.save(alumno);
    }

    public void eliminarAlumno(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el alumno con ID " + id + ".");
        }

        if (inscripcionRepository.existsByAlumno_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el alumno porque tiene inscripciones. Primero dá de baja esas inscripciones.");
        }

        if (cuotaRepository.existsByAlumno_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el alumno porque tiene cuotas asociadas. Conservá el historial de pagos.");
        }

        if (pagoRepository.existsByAlumno_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el alumno porque tiene pagos registrados.");
        }

        repository.deleteById(id);
    }

    private void validarYNormalizar(Alumno alumno) {
        if (alumno == null || alumno.getNya() == null || alumno.getNya().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y apellido es obligatorio.");
        }

        if (alumno.getDni() == null || alumno.getDni().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI es obligatorio.");
        }

        String dni = alumno.getDni().replaceAll("[.\\s]", "").trim();
        if (!dni.matches("\\d{7,8}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI debe ser un número válido de 7 u 8 cifras.");
        }

        if (alumno.getEdad() != null && (alumno.getEdad() < 1 || alumno.getEdad() > 120)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La edad debe estar entre 1 y 120 años.");
        }

        alumno.setNya(alumno.getNya().trim());
        alumno.setDni(dni);
        alumno.setLegajo(normalizarLegajo(alumno.getLegajo()));
        if (alumno.getEsRegular() == null) {
            alumno.setEsRegular(true);
        }
    }

    private String normalizarLegajo(String legajo) {
        return legajo == null || legajo.isBlank() ? null : legajo.trim();
    }
}
