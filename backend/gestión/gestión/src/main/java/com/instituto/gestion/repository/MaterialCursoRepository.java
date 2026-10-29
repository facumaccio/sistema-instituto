package com.instituto.gestion.repository;

import com.instituto.gestion.model.MaterialCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialCursoRepository extends JpaRepository<MaterialCurso, Long> {

    List<MaterialCurso> findByCurso_IdOrderByUnidadOClaseAscFechaPublicacionDesc(Long cursoId);

    List<MaterialCurso> findByCurso_IdOrderByFechaPublicacionDesc(Long cursoId);

    List<MaterialCurso> findAllByOrderByFechaPublicacionDesc();

    boolean existsByCurso_Id(Long cursoId);
}
