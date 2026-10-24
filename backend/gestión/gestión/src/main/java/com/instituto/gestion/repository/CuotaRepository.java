package com.instituto.gestion.repository;

import com.instituto.gestion.model.Cuota;
import com.instituto.gestion.model.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    boolean existsByAlumno_IdAndCurso_IdAndPeriodoMesAndPeriodoAnio(
            Long alumnoId, Long cursoId, Integer periodoMes, Integer periodoAnio);

    boolean existsByAlumno_Id(Long alumnoId);

    boolean existsByCurso_Id(Long cursoId);

    List<Cuota> findByEstadoAndFechaVencimientoBefore(EstadoCuota estado, LocalDate fecha);

    List<Cuota> findByEstado(EstadoCuota estado);

    List<Cuota> findByAlumno_IdOrderByPeriodoAnioDescPeriodoMesDesc(Long alumnoId);

    List<Cuota> findByCurso_IdOrderByPeriodoAnioDescPeriodoMesDesc(Long cursoId);

    long countByEstado(EstadoCuota estado);

    @Query("SELECT COUNT(DISTINCT c.alumno.id) FROM Cuota c WHERE c.estado = :estado")
    long countAlumnosByEstado(@Param("estado") EstadoCuota estado);

    @Query("""
            SELECT c FROM Cuota c
            WHERE (:cursoId IS NULL OR c.curso.id = :cursoId)
              AND (:alumnoId IS NULL OR c.alumno.id = :alumnoId)
              AND (:estado IS NULL OR c.estado = :estado)
              AND (:mes IS NULL OR c.periodoMes = :mes)
              AND (:anio IS NULL OR c.periodoAnio = :anio)
              AND (:busqueda = ''
                   OR LOWER(c.alumno.nya) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR c.alumno.dni LIKE CONCAT('%', :busqueda, '%')
                   OR LOWER(c.alumno.legajo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(c.curso.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            ORDER BY c.periodoAnio DESC, c.periodoMes DESC, c.alumno.nya ASC
            """)
    List<Cuota> buscarConFiltros(
            @Param("cursoId") Long cursoId,
            @Param("alumnoId") Long alumnoId,
            @Param("estado") EstadoCuota estado,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio,
            @Param("busqueda") String busqueda);

    /**
     * Cuotas que deben considerarse vencidas: las ya marcadas como VENCIDO y
     * las PENDIENTES cuya fecha de vencimiento ya pasó.
     */
    @Query("""
            SELECT c FROM Cuota c
            WHERE c.estado = :vencido
               OR (c.estado = :pendiente AND c.fechaVencimiento < :hoy)
            ORDER BY c.alumno.nya ASC, c.fechaVencimiento ASC
            """)
    List<Cuota> buscarVencidas(
            @Param("vencido") EstadoCuota vencido,
            @Param("pendiente") EstadoCuota pendiente,
            @Param("hoy") LocalDate hoy);

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Cuota c WHERE c.estado = :estado")
    Double sumMontoPorEstado(@Param("estado") EstadoCuota estado);
}
