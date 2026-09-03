package com.instituto.gestion.service;

import com.instituto.gestion.model.Curso;
import com.instituto.gestion.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository repository;

    public List<Curso> obtenerTodos() {
        return repository.findAll();
    }

    public Curso guardarCurso(Curso curso) {
        return repository.save(curso);
    }
}