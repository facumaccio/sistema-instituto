package com.instituto.gestion.controller;

import com.instituto.gestion.dto.CategoriaMovimientoResponse;
import com.instituto.gestion.dto.ResumenFinancieroResponse;
import com.instituto.gestion.model.TipoMovimiento;
import com.instituto.gestion.service.FinanzasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finanzas")
@CrossOrigin(origins = "*")
public class FinanzasController {

    @Autowired
    private FinanzasService service;

    /** Categorías disponibles, opcionalmente filtradas por tipo de movimiento. */
    @GetMapping("/categorias")
    public List<CategoriaMovimientoResponse> listarCategorias(
            @RequestParam(required = false) TipoMovimiento tipo
    ) {
        return service.listarCategorias(tipo);
    }

    /** Balance del período y desglose de egresos agrupados por categoría. */
    @GetMapping("/resumen")
    public ResumenFinancieroResponse obtenerResumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long categoriaId
    ) {
        return service.obtenerResumen(desde, hasta, tipo, categoriaId);
    }
}
