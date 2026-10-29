package com.instituto.gestion.controller;

import com.instituto.gestion.dto.DesafiliarAlumnoRequest;
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

    /**
     * Listado operativo. Por defecto solo devuelve alumnos ACTIVO.
     */
    @GetMapping
    public List<Alumno> listar() {
        return service.obtenerActivos();
    }

    /**
     * Alumnos desafiliados o archivados automáticamente por inactividad.
     */
    @GetMapping("/archivados")
    public List<Alumno> listarArchivados() {
        return service.obtenerArchivados();
    }

    @PostMapping
    public Alumno crear(@RequestBody Alumno alumno) {
        return service.guardarAlumno(alumno);
    }

    /**
     * Baja lógica. El registro y su historial se conservan.
     */
    @PutMapping("/{id}/desafiliar")
    public Alumno desafiliar(
            @PathVariable Long id,
            @RequestBody(required = false) DesafiliarAlumnoRequest request) {
        return service.desafiliarAlumno(id, request != null ? request.motivo() : null);
    }

    /**
     * Reincorpora al alumno a la operatoria diaria.
     */
    @PutMapping("/{id}/reactivar")
    public Alumno reactivar(@PathVariable Long id) {
        return service.reactivarAlumno(id);
    }

    @PutMapping("/{id}")
    public Alumno actualizar(@PathVariable Long id, @RequestBody Alumno alumno) {
        return service.actualizarAlumno(id, alumno);
    }
}
