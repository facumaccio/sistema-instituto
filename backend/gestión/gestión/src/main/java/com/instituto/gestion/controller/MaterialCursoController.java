package com.instituto.gestion.controller;

import com.instituto.gestion.model.MaterialCurso;
import com.instituto.gestion.service.MaterialCursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class MaterialCursoController {

    @Autowired
    private MaterialCursoService service;

    /** Materiales de un curso, agrupables por unidad/clase en el frontend. */
    @GetMapping("/api/cursos/{cursoId}/materiales")
    public List<MaterialCurso> listarPorCurso(@PathVariable Long cursoId) {
        return service.listarPorCurso(cursoId);
    }

    /** Listado general o filtrado por curso. */
    @GetMapping("/api/materiales")
    public List<MaterialCurso> listar(@RequestParam(required = false) Long cursoId) {
        return cursoId != null ? service.listarPorCurso(cursoId) : service.listarTodos();
    }

    @GetMapping("/api/materiales/{id}")
    public MaterialCurso obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/api/cursos/{cursoId}/materiales")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialCurso crear(
            @PathVariable Long cursoId,
            @RequestBody MaterialCurso material
    ) {
        return service.crear(cursoId, material);
    }

    @PutMapping("/api/materiales/{id}")
    public MaterialCurso actualizar(
            @PathVariable Long id,
            @RequestBody MaterialCurso material
    ) {
        return service.actualizar(id, material);
    }

    @DeleteMapping("/api/materiales/{id}")
    public Map<String, String> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return Map.of("mensaje", "Material eliminado correctamente.");
    }
}
