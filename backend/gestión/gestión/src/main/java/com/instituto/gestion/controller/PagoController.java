package com.instituto.gestion.controller;

import com.instituto.gestion.model.Pago;
import com.instituto.gestion.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired
    private PagoService service;

    @GetMapping
    public List<Pago> listar() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Pago crear(@RequestBody Pago pago) {
        return service.guardarPago(pago);
    }
}