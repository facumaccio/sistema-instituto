package com.instituto.gestion.repository;

import com.instituto.gestion.dto.CursoInscriptosProjection;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.EstadoAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByProfesor_Id(Long profesorId);

    /**
     * Devuelve todos los cursos, incluso los que no tienen inscriptos, con el
     * conteo de alumnos activos. Los alumnos sin regularidad definida cuentan
     * como activos, igual que en la generación de cuotas.
     */
    @Query("""
            SELECT c.id AS cursoId,
                   c.nombre AS nombre,
                   c.turno AS turno,
                   c.cupo AS cupo,
                   p.nya AS profesor,
                   COUNT(DISTINCT i.alumno.id) AS inscriptos
            FROM Curso c
            LEFT JOIN c.profesor p
            LEFT JOIN Inscripcion i ON i.curso = c
                 AND i.alumno.estado = :estado
                 AND (i.alumno.esRegular IS NULL OR i.alumno.esRegular = true)
            GROUP BY c.id, c.nombre, c.turno, c.cupo, p.nya
            ORDER BY c.nombre ASC
            """)
    List<CursoInscriptosProjection> contarInscriptosActivosPorCursoPorEstado(
            @Param("estado") EstadoAlumno estado);

    default List<CursoInscriptosProjection> contarInscriptosActivosPorCurso() {
        return contarInscriptosActivosPorCursoPorEstado(EstadoAlumno.ACTIVO);
    }
}
