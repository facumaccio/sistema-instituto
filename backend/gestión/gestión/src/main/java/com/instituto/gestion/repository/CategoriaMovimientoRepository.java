package com.instituto.gestion.repository;

import com.instituto.gestion.model.CategoriaMovimiento;
import com.instituto.gestion.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaMovimientoRepository extends JpaRepository<CategoriaMovimiento, Long> {

    List<CategoriaMovimiento> findByTipoOrderByNombreAsc(TipoMovimiento tipo);

    List<CategoriaMovimiento> findAllByOrderByTipoAscNombreAsc();

    boolean existsByNombreIgnoreCaseAndTipo(String nombre, TipoMovimiento tipo);

    Optional<CategoriaMovimiento> findByNombreIgnoreCaseAndTipo(String nombre, TipoMovimiento tipo);
}
