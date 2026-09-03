package com.instituto.gestion.controller;

import com.instituto.gestion.model.Profesor;
import com.instituto.gestion.service.ProfesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profesores")
@CrossOrigin(origins = "*")
public class ProfesorController {

    @Autowired
    private ProfesorService service;

    @GetMapping
    public List<Profesor> listar() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Profesor crear(@RequestBody Profesor profesor) {
        return service.guardarProfesor(profesor);
    }
}