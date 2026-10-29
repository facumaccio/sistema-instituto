package com.instituto.gestion.controller;

import com.instituto.gestion.dto.CalificacionLoteRequest;
import com.instituto.gestion.dto.CalificacionResponse;
import com.instituto.gestion.dto.HistorialAlumnoResponse;
import com.instituto.gestion.dto.NotaExamenAlumnoResponse;
import com.instituto.gestion.service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calificaciones")
@CrossOrigin(origins = "*")
public class CalificacionController {

    @Autowired
    private CalificacionService service;

    /** Notas del examen para todos los alumnos activos del curso. */
    @GetMapping("/examen/{eventoId}")
    public List<NotaExamenAlumnoResponse> listarPorExamen(@PathVariable Long eventoId) {
        return service.listarNotasPorExamen(eventoId);
    }

    /** Guarda o actualiza en lote las notas de un examen. */
    @PostMapping("/lote")
    public List<CalificacionResponse> guardarLote(@RequestBody CalificacionLoteRequest request) {
        return service.guardarLote(request);
    }

    /** Historial de notas y promedio por alumno dentro de un curso. */
    @GetMapping({"/curso/{cursoId}/historial", "/curso/{cursoId}"})
    public List<HistorialAlumnoResponse> historialPorCurso(@PathVariable Long cursoId) {
        return service.historialPorCurso(cursoId);
    }
}
