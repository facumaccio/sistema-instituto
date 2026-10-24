package com.instituto.gestion.controller;

import com.instituto.gestion.dto.DashboardReporteResponse;
import com.instituto.gestion.dto.ReporteDescarga;
import com.instituto.gestion.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private ReporteService service;

    /** KPIs, alumnos activos por curso, ocupación y evolución mensual. */
    @GetMapping("/dashboard")
    public DashboardReporteResponse dashboard() {
        return service.obtenerDashboard();
    }

    /** Descarga el listado completo de alumnos de un curso en CSV o PDF. */
    @GetMapping("/cursos/{cursoId}/alumnos")
    public ResponseEntity<byte[]> descargarAlumnos(
            @PathVariable Long cursoId,
            @RequestParam(defaultValue = "csv") String formato) {

        ReporteDescarga descarga = service.generarReporteAlumnos(cursoId, formato);

        String disposicion = ContentDisposition.attachment()
                .filename(descarga.nombreArchivo())
                .build()
                .toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(descarga.contentType()))
                .body(descarga.contenido());
    }
}
