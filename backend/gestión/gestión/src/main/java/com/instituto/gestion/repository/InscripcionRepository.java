package com.instituto.gestion.repository;

import com.instituto.gestion.model.EstadoAlumno;
import com.instituto.gestion.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByCurso_Id(Long cursoId);

    List<Inscripcion> findByAlumno_EstadoOrderByFechaInscripcionDesc(EstadoAlumno estado);

    List<Inscripcion> findByCurso_IdAndAlumno_EstadoOrderByFechaInscripcionDesc(
            Long cursoId, EstadoAlumno estado);

    @Query("""
            SELECT DISTINCT a.email
            FROM Inscripcion i
            JOIN i.alumno a
            WHERE i.curso.id = :cursoId
              AND a.estado = :estado
              AND a.email IS NOT NULL
              AND TRIM(a.email) <> ''
            """)
    List<String> findEmailsAlumnosPorCursoYEstado(
            @Param("cursoId") Long cursoId,
            @Param("estado") EstadoAlumno estado);

    boolean existsByAlumno_Id(Long alumnoId);

    boolean existsByCurso_Id(Long cursoId);

    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.alumno.id = :alumnoId AND i.curso.id = :cursoId")
    boolean existsByAlumnoAndCursoIds(@Param("alumnoId") Long alumnoId, @Param("cursoId") Long cursoId);

    boolean existsByAlumno_IdAndCurso_IdAndIdNot(Long alumnoId, Long cursoId, Long id);

    @Query("""
            SELECT MAX(i.fechaInscripcion)
            FROM Inscripcion i
            WHERE i.alumno.id = :alumnoId
              AND i.fechaInscripcion IS NOT NULL
            """)
    LocalDate findUltimaFechaInscripcionByAlumnoId(@Param("alumnoId") Long alumnoId);

    @Query("""
            SELECT COUNT(i)
            FROM Inscripcion i
            WHERE i.alumno.estado = :estado
              AND (i.alumno.esRegular IS NULL OR i.alumno.esRegular = true)
            """)
    long contarInscripcionesActivasPorEstado(@Param("estado") EstadoAlumno estado);

    default long contarInscripcionesActivas() {
        return contarInscripcionesActivasPorEstado(EstadoAlumno.ACTIVO);
    }

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
