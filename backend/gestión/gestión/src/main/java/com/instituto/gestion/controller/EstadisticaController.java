package com.instituto.gestion.controller;

import com.instituto.gestion.dto.EstadisticaActualResponse;
import com.instituto.gestion.dto.EstadisticaHistorialResponse;
import com.instituto.gestion.service.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = "*")
public class EstadisticaController {

    @Autowired
    private EstadisticaService service;

    /** Consolidado en tiempo real: KPIs, académicas, finanzas y eventos. */
    @GetMapping("/actual")
    public EstadisticaActualResponse actual() {
        return service.obtenerActual();
    }

    /** Guarda o actualiza la foto estadística del período actual. */
    @PostMapping("/guardar-cierre")
    public EstadisticaHistorialResponse guardarCierre() {
        return service.guardarCierre();
    }

    /** Cierres guardados, del más reciente al más antiguo. */
    @GetMapping("/historial")
    public List<EstadisticaHistorialResponse> historial() {
        return service.listarHistorial();
    }
}
