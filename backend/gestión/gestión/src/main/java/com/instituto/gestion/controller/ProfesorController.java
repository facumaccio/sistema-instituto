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

    @GetMapping("/{id}")
    public Profesor obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/dni/{dni}")
    public Profesor obtenerPorDni(@PathVariable String dni) {
        return service.obtenerPorDni(dni);
    }

    @PostMapping
    public Profesor crear(@RequestBody Profesor profesor) {
        return service.guardarProfesor(profesor);
    }

    @PutMapping("/{id}")
    public Profesor actualizar(@PathVariable Long id, @RequestBody Profesor profesor) {
        return service.actualizarProfesor(id, profesor);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminarProfesor(id);
        return "Profesor con ID " + id + " eliminado correctamente.";
    }
}
