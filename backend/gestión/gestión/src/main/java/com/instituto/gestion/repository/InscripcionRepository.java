package com.instituto.gestion.repository;

import com.instituto.gestion.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    
    List<Inscripcion> findByCurso_Id(Long cursoId);

    boolean existsByAlumno_Id(Long alumnoId);

    boolean existsByCurso_Id(Long cursoId);

    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.alumno.id = :alumnoId AND i.curso.id = :cursoId")
    boolean existsByAlumnoAndCursoIds(@Param("alumnoId") Long alumnoId, @Param("cursoId") Long cursoId);

    boolean existsByAlumno_IdAndCurso_IdAndIdNot(Long alumnoId, Long cursoId, Long id);

    @Query("""
            SELECT COUNT(i)
            FROM Inscripcion i
            WHERE (i.alumno.esRegular IS NULL OR i.alumno.esRegular = true)
            """)
    long contarInscripcionesActivas();

    /**
     * Inscripciones agrupadas por mes de los últimos doce meses. Se resuelve
     * en PostgreSQL para no traer el historial completo al servidor.
     */
    @Query(value = """
            SELECT to_char(i.fecha_inscripcion, 'YYYY-MM') AS periodo,
                   COUNT(*) AS total
            FROM inscripcion i
            WHERE i.fecha_inscripcion IS NOT NULL
              AND i.fecha_inscripcion >= CAST(
                    date_trunc('month', CURRENT_DATE) - INTERVAL '11 months' AS date)
            GROUP BY to_char(i.fecha_inscripcion, 'YYYY-MM')
            ORDER BY periodo
            """, nativeQuery = true)
    List<Object[]> contarInscripcionesUltimosDoceMeses();
}