package com.instituto.gestion.controller;

import com.instituto.gestion.dto.*;
import com.instituto.gestion.model.Cuota;
import com.instituto.gestion.model.EstadoCuota;
import com.instituto.gestion.service.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuotas")
@CrossOrigin(origins = "*")
public class CuotaController {

    @Autowired
    private CuotaService service;

    /** Listado de cuotas con filtros opcionales por curso, alumno, estado, mes, año y búsqueda. */
    @GetMapping
    public List<Cuota> listar(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Long alumnoId,
            @RequestParam(required = false) EstadoCuota estado,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String busqueda) {
        return service.listar(cursoId, alumnoId, estado, mes, anio, busqueda);
    }

    @GetMapping("/{id}")
    public Cuota obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    /** Genera las cuotas de un período para todos los alumnos activos del curso. */
    @PostMapping("/generar")
    public GenerarCuotasResponse generar(@RequestBody GenerarCuotasRequest request) {
        return service.generarCuotas(request);
    }

    /**
     * Registra el pago de una cuota. La fecha y el método son opcionales; el
     * pago genera automáticamente un ingreso con categoría Cuotas en Finanzas.
     */
    @PutMapping("/{id}/pagar")
    public Cuota pagar(@PathVariable Long id,
                       @RequestBody(required = false) RegistrarPagoRequest request) {
        return service.registrarPago(
                id,
                request != null ? request.getFechaPago() : null,
                request != null ? request.getMetodoPago() : null);
    }

    @GetMapping("/morosos")
    public List<MorosoResponse> morosos(@RequestParam(required = false) String busqueda) {
        return service.listarMorosos(busqueda);
    }

    /** Estado de cuenta consolidado por alumno y curso. */
    @GetMapping("/estado-cuenta")
    public List<EstadoCuentaResponse> estadoCuenta(@RequestParam(required = false) String busqueda) {
        return service.listarEstadoCuenta(busqueda);
    }

    @GetMapping("/resumen")
    public ResumenCuotasResponse resumen() {
        return service.obtenerResumen();
    }

    /** Alta manual de una cuota puntual. */
    @PostMapping
    public Cuota crear(@RequestBody Cuota cuota) {
        return service.crearCuota(cuota);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminarCuota(id);
        return "Cuota con ID " + id + " eliminada correctamente.";
    }
}
