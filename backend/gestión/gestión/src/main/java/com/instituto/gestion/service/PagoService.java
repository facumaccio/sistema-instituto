package com.instituto.gestion.service;

import com.instituto.gestion.model.Pago;
import com.instituto.gestion.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoRepository repository;

    @Autowired
    private AlumnoService alumnoService;

    public List<Pago> obtenerTodos() {
        return repository.findAll();
    }

    @Transactional
    public Pago guardarPago(Pago pago) {
        Pago guardado = repository.save(pago);

        if (guardado.getAlumno() != null && guardado.getAlumno().getId() != null) {
            alumnoService.registrarActividad(guardado.getAlumno().getId());
        }

        return guardado;
    }
}
