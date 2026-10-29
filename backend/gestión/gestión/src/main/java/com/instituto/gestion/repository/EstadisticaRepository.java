package com.instituto.gestion.repository;

import com.instituto.gestion.model.Estadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadisticaRepository extends JpaRepository<Estadistica, Long> {

    /**
     * Último cierre guardado para un período. Se usa para actualizar el
     * snapshot del mes en lugar de duplicarlo cada vez que se guarda.
     */
    Optional<Estadistica> findTopByPeriodoOrderByFechaGeneracionDesc(String periodo);

    List<Estadistica> findAllByOrderByFechaGeneracionDescIdDesc();
}
