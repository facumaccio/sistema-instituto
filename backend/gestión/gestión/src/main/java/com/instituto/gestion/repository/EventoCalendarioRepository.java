package com.instituto.gestion.repository;

import com.instituto.gestion.model.EventoCalendario;
import com.instituto.gestion.model.TipoEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Long> {

    /**
     * Eventos que se solapan con el rango indicado. Un evento de varios días
     * aparece si al menos uno de sus días cae dentro del rango.
     */
    @Query("""
            SELECT e
            FROM EventoCalendario e
            LEFT JOIN FETCH e.curso
            WHERE e.fechaInicio <= :hasta
              AND (
                    (e.fechaFin IS NULL AND e.fechaInicio >= :desde)
                    OR (e.fechaFin IS NOT NULL AND e.fechaFin >= :desde)
                  )
              AND (:cursoId = 0 OR e.curso.id = :cursoId)
            ORDER BY e.fechaInicio ASC, e.hora ASC, e.id ASC
            """)
    List<EventoCalendario> buscarEnRango(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("cursoId") long cursoId
    );

    /** Igual que el anterior, pero restringido a un tipo de evento. */
    @Query("""
            SELECT e
            FROM EventoCalendario e
            LEFT JOIN FETCH e.curso
            WHERE e.tipo = :tipo
              AND e.fechaInicio <= :hasta
              AND (
                    (e.fechaFin IS NULL AND e.fechaInicio >= :desde)
                    OR (e.fechaFin IS NOT NULL AND e.fechaFin >= :desde)
                  )
              AND (:cursoId = 0 OR e.curso.id = :cursoId)
            ORDER BY e.fechaInicio ASC, e.hora ASC, e.id ASC
            """)
    List<EventoCalendario> buscarEnRangoPorTipo(
            @Param("tipo") TipoEvento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("cursoId") long cursoId
    );

    /** Próximos eventos que se solapan con el rango, ordenados cronológicamente. */
    @Query("""
            SELECT e
            FROM EventoCalendario e
            LEFT JOIN FETCH e.curso
            WHERE e.fechaInicio <= :hasta
              AND (
                    (e.fechaFin IS NULL AND e.fechaInicio >= :desde)
                    OR (e.fechaFin IS NOT NULL AND e.fechaFin >= :desde)
                  )
            ORDER BY e.fechaInicio ASC, e.hora ASC, e.id ASC
            """)
    List<EventoCalendario> buscarProximos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    @Query("""
            SELECT e
            FROM EventoCalendario e
            LEFT JOIN FETCH e.curso
            WHERE e.id = :id
            """)
    Optional<EventoCalendario> buscarPorId(@Param("id") Long id);

    @EntityGraph(attributePaths = "curso")
    List<EventoCalendario> findByTipoOrderByFechaInicioAscHoraAsc(TipoEvento tipo);

    @EntityGraph(attributePaths = "curso")
    List<EventoCalendario> findByCurso_IdOrderByFechaInicioAscHoraAsc(Long cursoId);
}
