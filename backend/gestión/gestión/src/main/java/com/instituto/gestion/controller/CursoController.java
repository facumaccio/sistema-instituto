package com.instituto.gestion.controller;

import com.instituto.gestion.model.Curso;
import com.instituto.gestion.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    @Autowired
    private CursoService service;

    @GetMapping
    public List<Curso> listar() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Curso crear(@RequestBody Curso curso) {
        return service.guardarCurso(curso);
    }
}