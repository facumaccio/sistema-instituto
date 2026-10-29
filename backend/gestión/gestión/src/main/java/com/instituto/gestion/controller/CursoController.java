package com.instituto.gestion.controller;

import com.instituto.gestion.dto.EventoCalendarioResponse;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.service.CursoService;
import com.instituto.gestion.service.EventoCalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    @Autowired
    private CursoService service;

    @Autowired
    private EventoCalendarioService eventoCalendarioService;

    @GetMapping
    public List<Curso> listar() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Curso obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    /**
     * Exámenes del calendario asignados a este curso. Se usan como instancias
     * de evaluación para cargar notas.
     */
    @GetMapping("/{cursoId}/examenes")
    public List<EventoCalendarioResponse> listarExamenes(@PathVariable Long cursoId) {
        return eventoCalendarioService.listarExamenesPorCurso(cursoId);
    }

    @PostMapping
    public Curso crear(@RequestBody Curso curso) {
        return service.guardarCurso(curso);
    }

    @PutMapping("/{id}")
    public Curso actualizar(@PathVariable Long id, @RequestBody Curso curso) {
        return service.actualizarCurso(id, curso);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminarCurso(id);
        return "Curso con ID " + id + " eliminado correctamente.";
    }
}
