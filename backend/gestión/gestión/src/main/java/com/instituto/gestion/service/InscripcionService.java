package com.instituto.gestion.service;

import com.instituto.gestion.model.Inscripcion;
import com.instituto.gestion.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository repository;

    public List<Inscripcion> obtenerTodas() {
        return repository.findAll();
    }

    public Inscripcion guardarInscripcion(Inscripcion inscripcion) {
        // Setea la fecha de hoy automáticamente al crear la inscripción
        inscripcion.setFechaInscripcion(LocalDate.now()); 
        return repository.save(inscripcion);
    }

    public List<Inscripcion> buscarPorCurso(Long cursoId) {
        return repository.findByCurso_Id(cursoId);
    }

    // Método de Baja
    public boolean eliminarInscripcion(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método de Modificación / Actualización
    public Inscripcion actualizarInscripcion(Long id, Inscripcion inscripcionNueva) {
        Optional<Inscripcion> inscripcionExistente = repository.findById(id);
        if (inscripcionExistente.isPresent()) {
            Inscripcion inscripcionAModificar = inscripcionExistente.get();
            inscripcionAModificar.setFechaInscripcion(inscripcionNueva.getFechaInscripcion());
            inscripcionAModificar.setAlumno(inscripcionNueva.getAlumno());
            inscripcionAModificar.setCurso(inscripcionNueva.getCurso());
            return repository.save(inscripcionAModificar);
        }
        return null; // O lanzar excepción si no existe
    }
}