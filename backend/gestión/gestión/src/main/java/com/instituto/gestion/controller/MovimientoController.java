package com.instituto.gestion.controller;

import com.instituto.gestion.dto.MovimientoFinancieroRequest;
import com.instituto.gestion.dto.MovimientoFinancieroResponse;
import com.instituto.gestion.model.TipoMovimiento;
import com.instituto.gestion.service.FinanzasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    @Autowired
    private FinanzasService service;

    /** Historial de movimientos con filtros opcionales por fecha, tipo y categoría. */
    @GetMapping
    public List<MovimientoFinancieroResponse> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long categoriaId
    ) {
        return service.listarMovimientos(desde, hasta, tipo, categoriaId);
    }

    @PostMapping
    public MovimientoFinancieroResponse crear(@RequestBody MovimientoFinancieroRequest request) {
        return service.crearMovimiento(request);
    }

    @PutMapping("/{id}")
    public MovimientoFinancieroResponse actualizar(
            @PathVariable Long id,
            @RequestBody MovimientoFinancieroRequest request
    ) {
        return service.actualizarMovimiento(id, request);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminarMovimiento(id);
        return "Movimiento con ID " + id + " eliminado correctamente.";
    }
}
