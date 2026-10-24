package com.instituto.gestion.service;

import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.Profesor;
import com.instituto.gestion.repository.CuotaRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.instituto.gestion.repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository repository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    public List<Curso> obtenerTodos() {
        return repository.findAll();
    }

    public Curso guardarCurso(Curso curso) {
        validarCurso(curso);
        curso.setNombre(curso.getNombre().trim());
        curso.setTurno(normalizarTurno(curso.getTurno()));
        curso.setCupo(validarCupo(curso.getCupo()));
        curso.setProfesor(resolverProfesor(curso.getProfesor()));
        return repository.save(curso);
    }

    public Curso actualizarCurso(Long id, Curso cursoActualizado) {
        Curso curso = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso con ID " + id + "."));

        validarCurso(cursoActualizado);

        curso.setNombre(cursoActualizado.getNombre().trim());
        curso.setTurno(normalizarTurno(cursoActualizado.getTurno()));
        curso.setCupo(validarCupo(cursoActualizado.getCupo()));
        curso.setProfesor(resolverProfesor(cursoActualizado.getProfesor()));
        return repository.save(curso);
    }

    public void eliminarCurso(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso con ID " + id + ".");
        }

        if (inscripcionRepository.existsByCurso_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el curso porque tiene alumnos inscriptos. Primero dá de baja esas inscripciones.");
        }

        if (cuotaRepository.existsByCurso_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el curso porque tiene cuotas asociadas. Conservá el historial de pagos.");
        }

        repository.deleteById(id);
    }

    private void validarCurso(Curso curso) {
        if (curso == null || curso.getNombre() == null || curso.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del curso es obligatorio.");
        }
    }

    private String normalizarTurno(String turno) {
        return turno == null || turno.isBlank() ? null : turno.trim();
    }

    private Integer validarCupo(Integer cupo) {
        if (cupo == null) {
            return null;
        }
        if (cupo < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El cupo debe ser mayor a cero o quedar vacío si el curso no tiene límite.");
        }
        return cupo;
    }

    private Profesor resolverProfesor(Profesor profesor) {
        if (profesor == null || profesor.getId() == null) {
            return null;
        }

        return profesorRepository.findById(profesor.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se encontró el profesor seleccionado."));
    }
}
