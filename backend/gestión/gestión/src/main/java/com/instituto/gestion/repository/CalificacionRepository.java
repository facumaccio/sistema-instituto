package com.instituto.gestion.repository;

import com.instituto.gestion.model.Calificacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    @EntityGraph(attributePaths = {"alumno", "curso", "eventoExamen"})
    List<Calificacion> findByEventoExamen_Id(Long eventoId);

    @EntityGraph(attributePaths = {"alumno", "curso", "eventoExamen"})
    List<Calificacion> findByCurso_Id(Long cursoId);

    boolean existsByEventoExamen_Id(Long eventoId);

    boolean existsByCurso_Id(Long cursoId);
}
