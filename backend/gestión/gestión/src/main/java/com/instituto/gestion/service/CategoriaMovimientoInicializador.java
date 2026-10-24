package com.instituto.gestion.service;

import com.instituto.gestion.model.CategoriaMovimiento;
import com.instituto.gestion.model.TipoMovimiento;
import com.instituto.gestion.repository.CategoriaMovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga las categorías base la primera vez que arranca la aplicación.
 * Si una categoría ya existe (sin importar mayúsculas), no se duplica.
 */
@Component
@Order(1)
public class CategoriaMovimientoInicializador implements CommandLineRunner {

    private record CategoriaBase(String nombre, TipoMovimiento tipo) {}

    private static final List<CategoriaBase> CATEGORIAS_BASE = List.of(
            new CategoriaBase("Cuotas", TipoMovimiento.INGRESO),
            new CategoriaBase("Matrículas", TipoMovimiento.INGRESO),
            new CategoriaBase("Inscripciones", TipoMovimiento.INGRESO),
            new CategoriaBase("Donaciones", TipoMovimiento.INGRESO),
            new CategoriaBase("Otros ingresos", TipoMovimiento.INGRESO),
            new CategoriaBase("Sueldos", TipoMovimiento.EGRESO),
            new CategoriaBase("Servicios", TipoMovimiento.EGRESO),
            new CategoriaBase("Alquiler", TipoMovimiento.EGRESO),
            new CategoriaBase("Insumos", TipoMovimiento.EGRESO),
            new CategoriaBase("Mantenimiento", TipoMovimiento.EGRESO),
            new CategoriaBase("Impuestos", TipoMovimiento.EGRESO),
            new CategoriaBase("Otros egresos", TipoMovimiento.EGRESO)
    );

    @Autowired
    private CategoriaMovimientoRepository categoriaRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (CategoriaBase base : CATEGORIAS_BASE) {
            if (!categoriaRepository.existsByNombreIgnoreCaseAndTipo(base.nombre(), base.tipo())) {
                categoriaRepository.save(new CategoriaMovimiento(base.nombre(), base.tipo()));
            }
        }
    }
}
