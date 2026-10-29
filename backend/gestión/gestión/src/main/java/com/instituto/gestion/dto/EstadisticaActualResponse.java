package com.instituto.gestion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consolidado en tiempo real de las métricas del instituto. Los desgloses
 * también se serializan en {@code datosDetalladosJson} al guardar un cierre.
 */
public record EstadisticaActualResponse(
        String periodo,
        LocalDateTime fechaGeneracion,
        KpisGenerales kpis,
        List<CursoEstadistica> cursos,
        List<EvolucionInscripciones> evolucionInscripciones,
        FlujoCaja flujoCaja,
        List<GastoCategoria> gastosPorCategoria,
        double tasaAsistenciaPromedio,
        boolean asistenciaDisponible,
        String asistenciaNota,
        ResumenEventos eventos
) {

    public record KpisGenerales(
            int totalAlumnosActivos,
            int totalProfesores,
            int totalCursos,
            int totalInscripcionesActivas,
            BigDecimal totalIngresos,
            BigDecimal totalEgresos,
            BigDecimal balanceNeto,
            double tasaAsistenciaPromedio
    ) {}

    public record CursoEstadistica(
            Long cursoId,
            String nombre,
            String turno,
            String profesor,
            Integer cupo,
            int inscriptos,
            Double ocupacion,
            Integer cupoDisponible,
            boolean casiLleno
    ) {}

    public record FlujoCaja(
            BigDecimal ingresos,
            BigDecimal egresos,
            BigDecimal balance,
            List<FlujoMensual> mensual
    ) {}

    public record FlujoMensual(
            String periodo,
            String etiqueta,
            BigDecimal ingresos,
            BigDecimal egresos
    ) {}

    public record GastoCategoria(
            Long categoriaId,
            String nombre,
            BigDecimal total,
            long cantidad,
            double porcentaje
    ) {}

    public record ResumenEventos(
            int proximos30Dias,
            int examenes,
            int festejos,
            int feriados,
            int reuniones,
            int entregasTp,
            int otros
    ) {}
}
