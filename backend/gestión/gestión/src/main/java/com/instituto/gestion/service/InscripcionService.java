package com.instituto.gestion.service;

import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.Inscripcion;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository repository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    public List<Inscripcion> obtenerTodas() {
        return repository.findAll();
    }

    public Inscripcion guardarInscripcion(Inscripcion inscripcion) {
        Alumno alumno = resolverAlumno(inscripcion);
        Curso curso = resolverCurso(inscripcion);

        if (repository.existsByAlumnoAndCursoIds(alumno.getId(), curso.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno ya se encuentra inscripto en este curso.");
        }

        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        inscripcion.setFechaInscripcion(LocalDate.now());
        return repository.save(inscripcion);
    }

    public List<Inscripcion> buscarPorCurso(Long cursoId) {
        return repository.findByCurso_Id(cursoId);
    }

    public void eliminarInscripcion(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró la inscripción con ID " + id + ".");
        }
        repository.deleteById(id);
    }

    public Inscripcion actualizarInscripcion(Long id, Inscripcion inscripcionNueva) {
        Inscripcion inscripcion = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la inscripción con ID " + id + "."));

        Alumno alumno = resolverAlumno(inscripcionNueva);
        Curso curso = resolverCurso(inscripcionNueva);

        if (repository.existsByAlumno_IdAndCurso_IdAndIdNot(alumno.getId(), curso.getId(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno ya se encuentra inscripto en este curso.");
        }

        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        if (inscripcionNueva.getFechaInscripcion() != null) {
            inscripcion.setFechaInscripcion(inscripcionNueva.getFechaInscripcion());
        }
        return repository.save(inscripcion);
    }

    private Alumno resolverAlumno(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getAlumno() == null || inscripcion.getAlumno().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná un alumno.");
        }

        return alumnoRepository.findById(inscripcion.getAlumno().getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alumno seleccionado no existe."));
    }

    private Curso resolverCurso(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getCurso() == null || inscripcion.getCurso().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná un curso.");
        }

        return cursoRepository.findById(inscripcion.getCurso().getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El curso seleccionado no existe."));
    }
}
