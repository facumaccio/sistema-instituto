package com.instituto.gestion.repository;

import com.instituto.gestion.model.MovimientoFinanciero;
import com.instituto.gestion.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoFinancieroRepository extends JpaRepository<MovimientoFinanciero, Long> {

    Optional<MovimientoFinanciero> findByCuota_Id(Long cuotaId);

    boolean existsByCuota_Id(Long cuotaId);

    void deleteByCuota_Id(Long cuotaId);

    @Query("""
            SELECT m
            FROM MovimientoFinanciero m
            JOIN FETCH m.categoria c
            WHERE m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR c.id = :categoriaId)
            ORDER BY m.fecha DESC, m.id DESC
            """)
    List<MovimientoFinanciero> buscarTodos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );

    @Query("""
            SELECT m
            FROM MovimientoFinanciero m
            JOIN FETCH m.categoria c
            WHERE m.tipo = :tipo
              AND m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR c.id = :categoriaId)
            ORDER BY m.fecha DESC, m.id DESC
            """)
    List<MovimientoFinanciero> buscarPorTipo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );

    @Query("""
            SELECT COALESCE(SUM(m.monto), 0)
            FROM MovimientoFinanciero m
            WHERE m.tipo = :tipo
              AND m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR m.categoria.id = :categoriaId)
            """)
    BigDecimal sumarPorTipo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );

    @Query("""
            SELECT COUNT(m)
            FROM MovimientoFinanciero m
            WHERE m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR m.categoria.id = :categoriaId)
            """)
    long contarTodos(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );

    @Query("""
            SELECT COUNT(m)
            FROM MovimientoFinanciero m
            WHERE m.tipo = :tipo
              AND m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR m.categoria.id = :categoriaId)
            """)
    long contarPorTipo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );

    @Query("""
            SELECT c.id, c.nombre, c.tipo, COALESCE(SUM(m.monto), 0), COUNT(m.id)
            FROM MovimientoFinanciero m
            JOIN m.categoria c
            WHERE m.tipo = :tipo
              AND m.fecha >= :desde
              AND m.fecha <= :hasta
              AND (:categoriaId = 0 OR c.id = :categoriaId)
            GROUP BY c.id, c.nombre, c.tipo
            ORDER BY SUM(m.monto) DESC, c.nombre ASC
            """)
    List<Object[]> desglosePorTipo(
            @Param("tipo") TipoMovimiento tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("categoriaId") long categoriaId
    );
}
