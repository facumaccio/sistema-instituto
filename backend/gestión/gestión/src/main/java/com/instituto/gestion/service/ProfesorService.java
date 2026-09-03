package com.instituto.gestion.service;

import com.instituto.gestion.model.Profesor;
import com.instituto.gestion.repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProfesorService {

    @Autowired
    private ProfesorRepository repository;

    public List<Profesor> obtenerTodos() {
        return repository.findAll();
    }

    public Profesor guardarProfesor(Profesor profesor) {
        return repository.save(profesor);
    }
}