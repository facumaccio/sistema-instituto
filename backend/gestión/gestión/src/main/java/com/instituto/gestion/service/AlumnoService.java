package com.instituto.gestion.service;

import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository repository;

    public List<Alumno> obtenerTodos() {
        return repository.findAll();
    }

    public Alumno guardarAlumno(Alumno alumno) {
        return repository.save(alumno);
    }

    public boolean eliminarAlumno(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Alumno actualizarAlumno(Long id, Alumno alumnoActualizado) {
        Optional<Alumno> alumnoExistente = repository.findById(id);
        if (alumnoExistente.isPresent()) {
            Alumno alumno = alumnoExistente.get();
            alumno.setNya(alumnoActualizado.getNya());
            alumno.setDni(alumnoActualizado.getDni());
            return repository.save(alumno);
        }
        return null; 
    }
}