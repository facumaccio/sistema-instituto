package com.instituto.gestion.controller;

import com.instituto.gestion.model.Inscripcion;
import com.instituto.gestion.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@CrossOrigin(origins = "*")
public class InscripcionController {

    @Autowired
    private InscripcionService service;

    @GetMapping
    public List<Inscripcion> listar() {
        return service.obtenerTodas();
    }

    @PostMapping
    public Inscripcion crear(@RequestBody Inscripcion inscripcion) {
        return service.guardarInscripcion(inscripcion);
    }

    @GetMapping("/curso/{cursoId}")
    public List<Inscripcion> listarPorCurso(@PathVariable Long cursoId) {
        return service.buscarPorCurso(cursoId);
    }

    // Endpoint para Eliminar (DELETE)
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        boolean eliminado = service.eliminarInscripcion(id);
        if (eliminado) {
            return "Inscripción con ID " + id + " eliminada correctamente.";
        }
        return "No se encontró la inscripción con ID " + id;
    }

    // Endpoint para Modificar (PUT)
    @PutMapping("/{id}")
    public Inscripcion actualizar(@PathVariable Long id, @RequestBody Inscripcion inscripcion) {
        return service.actualizarInscripcion(id, inscripcion);
    }
}