package com.instituto.gestion.controller;

import com.instituto.gestion.dto.EventoCalendarioRequest;
import com.instituto.gestion.dto.EventoCalendarioResponse;
import com.instituto.gestion.dto.TipoEventoResponse;
import com.instituto.gestion.model.TipoEvento;
import com.instituto.gestion.service.EventoCalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/calendario", "/api/eventos"})
@CrossOrigin(origins = "*")
public class EventoCalendarioController {

    @Autowired
    private EventoCalendarioService service;

    /** Listado de eventos con filtros opcionales por rango de fechas, tipo y curso. */
    @GetMapping
    public List<EventoCalendarioResponse> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoEvento tipo,
            @RequestParam(required = false) Long cursoId
    ) {
        return service.listar(desde, hasta, tipo, cursoId);
    }

    /** Próximos eventos de la semana (o de la cantidad de días indicada). */
    @GetMapping("/proximos")
    public List<EventoCalendarioResponse> proximos(
            @RequestParam(defaultValue = "7") Integer dias
    ) {
        return service.listarProximos(dias);
    }

    /** Tipos disponibles con su etiqueta y color, para filtros y formularios. */
    @GetMapping("/tipos")
    public List<TipoEventoResponse> tipos() {
        return service.listarTipos();
    }

    @GetMapping("/{id}")
    public EventoCalendarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoCalendarioResponse crear(@RequestBody EventoCalendarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public EventoCalendarioResponse actualizar(
            @PathVariable Long id,
            @RequestBody EventoCalendarioRequest request
    ) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return Map.of("mensaje", "Evento eliminado correctamente.");
    }
}
