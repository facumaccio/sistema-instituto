package com.instituto.gestion.controller;

import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.service.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@CrossOrigin(origins = "*")
public class AlumnoController {

    @Autowired
    private AlumnoService service;

    @GetMapping
    public List<Alumno> listar() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Alumno crear(@RequestBody Alumno alumno) {
        return service.guardarAlumno(alumno);
    }

    // Endpoint para Eliminar (DELETE)
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        boolean eliminado = service.eliminarAlumno(id);
        if (eliminado) {
            return "Alumno con ID " + id + " eliminado correctamente.";
        }
        return "No se encontró el alumno con ID " + id;
    }

    // Endpoint para Modificar (PUT)
    @PutMapping("/{id}")
    public Alumno actualizar(@PathVariable Long id, @RequestBody Alumno alumno) {
        return service.actualizarAlumno(id, alumno);
    }
}