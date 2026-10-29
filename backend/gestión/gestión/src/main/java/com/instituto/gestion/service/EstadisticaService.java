package com.instituto.gestion.service;

import com.instituto.gestion.dto.CursoInscriptosProjection;
import com.instituto.gestion.dto.EstadisticaActualResponse;
import com.instituto.gestion.dto.EstadisticaHistorialResponse;
import com.instituto.gestion.dto.EvolucionInscripciones;
import com.instituto.gestion.model.Estadistica;
import com.instituto.gestion.model.EventoCalendario;
import com.instituto.gestion.model.MovimientoFinanciero;
import com.instituto.gestion.model.TipoEvento;
import com.instituto.gestion.model.TipoMovimiento;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.EstadisticaRepository;
import com.instituto.gestion.repository.EventoCalendarioRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.instituto.gestion.repository.MovimientoFinancieroRepository;
import com.instituto.gestion.repository.ProfesorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticaService {

    private static final Logger log = LoggerFactory.getLogger(EstadisticaService.class);

    private static final int UMBRAL_CASI_LLENO = 80;
    private static final int MESES_FLUJO = 6;

    private static final String NOTA_ASISTENCIA =
            "El instituto todavía no registra asistencias. La métrica se activará cuando exista un módulo de asistencia.";

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private MovimientoFinancieroRepository movimientoRepository;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private EstadisticaRepository estadisticaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Calcula el consolidado completo en tiempo real. El período financiero
     * principal es el mes actual y el flujo comparativo abarca seis meses.
     */
    @Transactional(readOnly = true)
    public EstadisticaActualResponse obtenerActual() {
        LocalDate hoy = LocalDate.now();
        YearMonth mesActual = YearMonth.from(hoy);
        LocalDate desdeMes = mesActual.atDay(1);
        LocalDate hastaMes = mesActual.atEndOfMonth();

        int totalAlumnosActivos = (int) alumnoRepository.contarAlumnosActivos();
        int totalProfesores = (int) profesorRepository.count();
        int totalCursos = (int) cursoRepository.count();
        int totalInscripcionesActivas = (int) inscripcionRepository.contarInscripcionesActivas();

        List<EstadisticaActualResponse.CursoEstadistica> cursos = construirCursos();

        BigDecimal totalIngresos = valorSeguro(movimientoRepository.sumarPorTipo(
                TipoMovimiento.INGRESO, desdeMes, hastaMes, 0L));
        BigDecimal totalEgresos = valorSeguro(movimientoRepository.sumarPorTipo(
                TipoMovimiento.EGRESO, desdeMes, hastaMes, 0L));
        BigDecimal balanceNeto = totalIngresos.subtract(totalEgresos);

        List<EstadisticaActualResponse.GastoCategoria> gastos =
                construirGastosPorCategoria(totalEgresos, desdeMes, hastaMes);
        EstadisticaActualResponse.FlujoCaja flujoCaja = construirFlujoCaja(mesActual);
        List<EvolucionInscripciones> evolucion = construirEvolucion(
                inscripcionRepository.contarInscripcionesUltimosDoceMeses(), mesActual);
        EstadisticaActualResponse.ResumenEventos eventos = construirResumenEventos(hoy);

        EstadisticaActualResponse.KpisGenerales kpis = new EstadisticaActualResponse.KpisGenerales(
                totalAlumnosActivos,
                totalProfesores,
                totalCursos,
                totalInscripcionesActivas,
                totalIngresos,
                totalEgresos,
                balanceNeto,
                0.0
        );

        return new EstadisticaActualResponse(
                mesActual.toString(),
                LocalDateTime.now(),
                kpis,
                cursos,
                evolucion,
                flujoCaja,
                gastos,
                0.0,
                false,
                NOTA_ASISTENCIA,
                eventos
        );
    }

    /**
     * Guarda o actualiza el snapshot del período actual. Si ya existe un
     * cierre para el mismo mes, se actualiza en lugar de duplicarlo.
     */
    @Transactional
    public EstadisticaHistorialResponse guardarCierre() {
        EstadisticaActualResponse actual = obtenerActual();

        Estadistica estadistica = estadisticaRepository
                .findTopByPeriodoOrderByFechaGeneracionDesc(actual.periodo())
                .orElseGet(Estadistica::new);

        estadistica.setFechaGeneracion(LocalDateTime.now());
        estadistica.setPeriodo(actual.periodo());
        estadistica.setTotalAlumnosActivos(actual.kpis().totalAlumnosActivos());
        estadistica.setTotalProfesores(actual.kpis().totalProfesores());
        estadistica.setTotalCursos(actual.kpis().totalCursos());
        estadistica.setTasaAsistenciaPromedio(actual.tasaAsistenciaPromedio());
        estadistica.setTotalIngresos(actual.kpis().totalIngresos());
        estadistica.setTotalEgresos(actual.kpis().totalEgresos());
        estadistica.setBalanceNeto(actual.kpis().balanceNeto());
        estadistica.setDatosDetalladosJson(serializarDetalle(actual));

        return mapearHistorial(estadisticaRepository.save(estadistica));
    }

    @Transactional(readOnly = true)
    public List<EstadisticaHistorialResponse> listarHistorial() {
        return estadisticaRepository.findAllByOrderByFechaGeneracionDescIdDesc().stream()
                .map(this::mapearHistorial)
                .toList();
    }

    private List<EstadisticaActualResponse.CursoEstadistica> construirCursos() {
        return cursoRepository.contarInscriptosActivosPorCurso().stream()
                .map(this::mapearCurso)
                .toList();
    }

    private EstadisticaActualResponse.CursoEstadistica mapearCurso(CursoInscriptosProjection fila) {
        int inscriptos = fila.getInscriptos() != null ? fila.getInscriptos().intValue() : 0;
        Integer cupo = fila.getCupo();
        Double ocupacion = null;
        Integer cupoDisponible = null;
        boolean casiLleno = false;

        if (cupo != null && cupo > 0) {
            ocupacion = Math.round((inscriptos * 10000.0) / cupo) / 100.0;
            cupoDisponible = Math.max(0, cupo - inscriptos);
            casiLleno = inscriptos * 100 >= cupo * UMBRAL_CASI_LLENO;
        }

        return new EstadisticaActualResponse.CursoEstadistica(
                fila.getCursoId(),
                fila.getNombre(),
                fila.getTurno(),
                fila.getProfesor(),
                cupo,
                inscriptos,
                ocupacion,
                cupoDisponible,
                casiLleno
        );
    }

    private List<EstadisticaActualResponse.GastoCategoria> construirGastosPorCategoria(
            BigDecimal totalEgresos,
            LocalDate desde,
            LocalDate hasta
    ) {
        return movimientoRepository.desglosePorTipo(
                        TipoMovimiento.EGRESO, desde, hasta, 0L).stream()
                .map(fila -> {
                    Long categoriaId = fila[0] == null ? null : ((Number) fila[0]).longValue();
                    String nombre = (String) fila[1];
                    BigDecimal total = valorSeguro(aBigDecimal(fila[3]));
                    long cantidad = fila[4] == null ? 0L : ((Number) fila[4]).longValue();
                    double porcentaje = totalEgresos.compareTo(BigDecimal.ZERO) > 0
                            ? redondearPorcentaje(total.doubleValue() * 100.0 / totalEgresos.doubleValue())
                            : 0.0;

                    return new EstadisticaActualResponse.GastoCategoria(
                            categoriaId, nombre, total, cantidad, porcentaje);
                })
                .toList();
    }

    private EstadisticaActualResponse.FlujoCaja construirFlujoCaja(YearMonth mesActual) {
        YearMonth primerMes = mesActual.minusMonths(MESES_FLUJO - 1L);
        LocalDate desde = primerMes.atDay(1);
        LocalDate hasta = mesActual.atEndOfMonth();

        Map<YearMonth, BigDecimal[]> acumulado = new HashMap<>();
        for (int i = 0; i < MESES_FLUJO; i++) {
            acumulado.put(primerMes.plusMonths(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (MovimientoFinanciero movimiento : movimientoRepository.buscarTodos(desde, hasta, 0L)) {
            if (movimiento.getFecha() == null) {
                continue;
            }
            YearMonth periodo = YearMonth.from(movimiento.getFecha());
            BigDecimal[] valores = acumulado.get(periodo);
            if (valores == null) {
                continue;
            }
            if (movimiento.getTipo() == TipoMovimiento.INGRESO) {
                valores[0] = valores[0].add(valorSeguro(movimiento.getMonto()));
            } else if (movimiento.getTipo() == TipoMovimiento.EGRESO) {
                valores[1] = valores[1].add(valorSeguro(movimiento.getMonto()));
            }
        }

        List<EstadisticaActualResponse.FlujoMensual> mensual = new ArrayList<>();
        for (int i = 0; i < MESES_FLUJO; i++) {
            YearMonth periodo = primerMes.plusMonths(i);
            BigDecimal[] valores = acumulado.get(periodo);
            mensual.add(new EstadisticaActualResponse.FlujoMensual(
                    periodo.toString(),
                    String.format("%02d/%d", periodo.getMonthValue(), periodo.getYear()),
                    valorSeguro(valores[0]),
                    valorSeguro(valores[1])
            ));
        }

        BigDecimal ingresos = valorSeguro(acumulado.get(mesActual)[0]);
        BigDecimal egresos = valorSeguro(acumulado.get(mesActual)[1]);

        return new EstadisticaActualResponse.FlujoCaja(
                ingresos,
                egresos,
                ingresos.subtract(egresos),
                mensual
        );
    }

    private List<EvolucionInscripciones> construirEvolucion(List<Object[]> filas, YearMonth mesActual) {
        Map<String, Long> porPeriodo = new HashMap<>();
        for (Object[] fila : filas) {
            if (fila == null || fila.length < 2 || fila[0] == null || fila[1] == null) {
                continue;
            }
            porPeriodo.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }

        List<EvolucionInscripciones> resultado = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth mes = mesActual.minusMonths(i);
            String periodo = mes.toString();
            String etiqueta = String.format("%02d/%d", mes.getMonthValue(), mes.getYear());
            resultado.add(new EvolucionInscripciones(
                    periodo, etiqueta, porPeriodo.getOrDefault(periodo, 0L)));
        }
        return resultado;
    }

    private EstadisticaActualResponse.ResumenEventos construirResumenEventos(LocalDate hoy) {
        List<EventoCalendario> eventos = eventoRepository.buscarProximos(hoy, hoy.plusDays(30));

        int examenes = 0;
        int festejos = 0;
        int feriados = 0;
        int reuniones = 0;
        int entregasTp = 0;
        int otros = 0;

        for (EventoCalendario evento : eventos) {
            TipoEvento tipo = evento.getTipo();
            if (tipo == null) {
                otros++;
                continue;
            }
            switch (tipo) {
                case EXAMEN -> examenes++;
                case FESTEJO -> festejos++;
                case FERIADO -> feriados++;
                case REUNION -> reuniones++;
                case ENTREGA_TP -> entregasTp++;
                case OTRO -> otros++;
            }
        }

        return new EstadisticaActualResponse.ResumenEventos(
                eventos.size(), examenes, festejos, feriados, reuniones, entregasTp, otros);
    }

    private String serializarDetalle(EstadisticaActualResponse actual) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("cursos", actual.cursos());
        detalle.put("evolucionInscripciones", actual.evolucionInscripciones());
        detalle.put("flujoCaja", actual.flujoCaja());
        detalle.put("gastosPorCategoria", actual.gastosPorCategoria());
        detalle.put("eventos", actual.eventos());
        detalle.put("asistencia", Map.of(
                "disponible", actual.asistenciaDisponible(),
                "tasaPromedio", actual.tasaAsistenciaPromedio(),
                "nota", actual.asistenciaNota()
        ));

        try {
            return objectMapper.writeValueAsString(detalle);
        } catch (Exception error) {
            log.warn("No se pudo serializar el detalle estadístico del período {}", actual.periodo(), error);
            return "{}";
        }
    }

    private EstadisticaHistorialResponse mapearHistorial(Estadistica estadistica) {
        return new EstadisticaHistorialResponse(
                estadistica.getId(),
                estadistica.getFechaGeneracion(),
                estadistica.getPeriodo(),
                estadistica.getTotalAlumnosActivos(),
                estadistica.getTotalProfesores(),
                estadistica.getTotalCursos(),
                estadistica.getTasaAsistenciaPromedio(),
                estadistica.getTotalIngresos(),
                estadistica.getTotalEgresos(),
                estadistica.getBalanceNeto()
        );
    }

    private BigDecimal aBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        return new BigDecimal(valor.toString());
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        BigDecimal resultado = valor == null ? BigDecimal.ZERO : valor;
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }

    private double redondearPorcentaje(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
