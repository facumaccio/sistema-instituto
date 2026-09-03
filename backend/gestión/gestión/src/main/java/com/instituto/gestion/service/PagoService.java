package com.instituto.gestion.service;

import com.instituto.gestion.model.Pago;
import com.instituto.gestion.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoRepository repository;

    public List<Pago> obtenerTodos() {
        return repository.findAll();
    }

    public Pago guardarPago(Pago pago) {
        return repository.save(pago);
    }
}