package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de cierre estadístico para la tabla de historial. */
public record EstadisticaHistorialResponse(
        Long id,
        LocalDateTime fechaGeneracion,
        String periodo,
        Integer totalAlumnosActivos,
        Integer totalProfesores,
        Integer totalCursos,
        Double tasaAsistenciaPromedio,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal balanceNeto
) {}
