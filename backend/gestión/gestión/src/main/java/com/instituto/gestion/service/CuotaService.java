package com.instituto.gestion.service;

import com.instituto.gestion.dto.*;
import com.instituto.gestion.model.*;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CategoriaMovimientoRepository;
import com.instituto.gestion.repository.CuotaRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.instituto.gestion.repository.MovimientoFinancieroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CuotaService {

    private static final String CATEGORIA_CUOTAS = "Cuotas";

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private MovimientoFinancieroRepository movimientoRepository;

    @Autowired
    private CategoriaMovimientoRepository categoriaRepository;

    // ------------------------------------------------------------------
    // Generación masiva de cuotas
    // ------------------------------------------------------------------

    /**
     * Genera la cuota de un mes/año para todos los alumnos activos del curso.
     * Se consideran alumnos activos a los inscriptos en el curso cuyo atributo
     * esRegular no sea false (null se toma como activo, ya que el alta de
     * alumnos del sistema no siempre completa ese dato).
     * Si un alumno ya tiene la cuota de ese período, se omite.
     */
    @Transactional
    public GenerarCuotasResponse generarCuotas(GenerarCuotasRequest request) {
        if (request == null || request.getCursoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el curso.");
        }
        validarPeriodo(request.getMes(), request.getAnio());
        if (request.getMonto() == null || request.getMonto() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a cero.");
        }
        if (request.getFechaVencimiento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar la fecha de vencimiento.");
        }

        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el curso indicado."));

        // Alumnos activos: inscriptos en el curso, sin duplicados, excluyendo los marcados como no regulares.
        Map<Long, Alumno> activos = new LinkedHashMap<>();
        for (Inscripcion inscripcion : inscripcionRepository.findByCurso_Id(curso.getId())) {
            Alumno alumno = inscripcion.getAlumno();
            if (alumno == null || alumno.getId() == null) {
                continue;
            }
            if (alumno.getEstado() != EstadoAlumno.ACTIVO) {
                continue;
            }
            if (Boolean.FALSE.equals(alumno.getEsRegular())) {
                continue;
            }
            activos.putIfAbsent(alumno.getId(), alumno);
        }

        if (activos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El curso no tiene alumnos activos inscriptos.");
        }

        LocalDate hoy = LocalDate.now();
        EstadoCuota estadoInicial = request.getFechaVencimiento().isBefore(hoy)
                ? EstadoCuota.VENCIDO
                : EstadoCuota.PENDIENTE;

        List<Cuota> nuevas = new ArrayList<>();
        int omitidas = 0;

        for (Alumno alumno : activos.values()) {
            boolean yaExiste = cuotaRepository.existsByAlumno_IdAndCurso_IdAndPeriodoMesAndPeriodoAnio(
                    alumno.getId(), curso.getId(), request.getMes(), request.getAnio());
            if (yaExiste) {
                omitidas++;
                continue;
            }

            Cuota cuota = new Cuota();
            cuota.setAlumno(alumno);
            cuota.setCurso(curso);
            cuota.setPeriodoMes(request.getMes());
            cuota.setPeriodoAnio(request.getAnio());
            cuota.setMonto(request.getMonto());
            cuota.setFechaVencimiento(request.getFechaVencimiento());
            cuota.setEstado(estadoInicial);
            nuevas.add(cuota);
        }

        cuotaRepository.saveAll(nuevas);

        String mensaje = String.format(
                "Se generaron %d cuota(s) para %s del período %02d/%d. %d cuota(s) ya existían.",
                nuevas.size(), curso.getNombre(), request.getMes(), request.getAnio(), omitidas);

        return new GenerarCuotasResponse(
                curso.getId(), curso.getNombre(), request.getMes(), request.getAnio(),
                request.getMonto(), request.getFechaVencimiento(), activos.size(),
                nuevas.size(), omitidas, mensaje);
    }

    // ------------------------------------------------------------------
    // Registro de pagos
    // ------------------------------------------------------------------

    @Transactional
    public Cuota registrarPago(Long cuotaId, LocalDate fechaPago, MetodoPago metodoPago) {
        Cuota cuota = obtenerPorId(cuotaId);

        if (cuota.getEstado() == EstadoCuota.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuota ya figura como pagada.");
        }

        LocalDate fecha = fechaPago != null ? fechaPago : LocalDate.now();
        if (fecha.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de pago no puede ser futura.");
        }

        cuota.setFechaPago(fecha);
        cuota.setEstado(EstadoCuota.PAGADO);
        registrarIngresoCuota(cuota, fecha, metodoPago);
        Cuota guardada = cuotaRepository.save(cuota);

        if (guardada.getAlumno() != null && guardada.getAlumno().getId() != null) {
            alumnoService.registrarActividad(guardada.getAlumno().getId());
        }

        return guardada;
    }

    /**
     * Genera el ingreso correspondiente a una cuota pagada. La operación es
     * idempotente: si la cuota ya tiene su ingreso registrado o existe un
     * movimiento vinculado, no se crea otro.
     */
    private boolean registrarIngresoCuota(Cuota cuota, LocalDate fecha, MetodoPago metodoPago) {
        if (Boolean.TRUE.equals(cuota.getIngresoRegistrado())) {
            return false;
        }

        if (cuota.getId() != null && movimientoRepository.existsByCuota_Id(cuota.getId())) {
            cuota.setIngresoRegistrado(true);
            return true;
        }

        if (cuota.getMonto() == null || cuota.getMonto() <= 0) {
            return false;
        }

        MovimientoFinanciero movimiento = new MovimientoFinanciero();
        movimiento.setTipo(TipoMovimiento.INGRESO);
        movimiento.setCategoria(obtenerCategoriaCuotas());
        movimiento.setMonto(BigDecimal.valueOf(cuota.getMonto()).setScale(2, RoundingMode.HALF_UP));
        movimiento.setFecha(fecha != null ? fecha : LocalDate.now());
        movimiento.setDescripcion(descripcionCuota(cuota));
        movimiento.setMetodoPago(metodoPago != null ? metodoPago : MetodoPago.EFECTIVO);
        movimiento.setCuota(cuota);
        movimientoRepository.save(movimiento);

        cuota.setIngresoRegistrado(true);
        return true;
    }

    /**
     * Sincroniza las cuotas que ya estaban pagadas antes de esta integración.
     * Solo actúa sobre cuotas sin ingreso registrado, por lo que puede
     * ejecutarse en cada arranque sin duplicar movimientos.
     */
    @Transactional
    public int sincronizarCuotasPagadas() {
        int sincronizadas = 0;
        for (Cuota cuota : cuotaRepository.findByEstado(EstadoCuota.PAGADO)) {
            if (cuota.getFechaPago() == null || Boolean.TRUE.equals(cuota.getIngresoRegistrado())) {
                continue;
            }
            if (registrarIngresoCuota(cuota, cuota.getFechaPago(), MetodoPago.EFECTIVO)) {
                cuotaRepository.save(cuota);
                sincronizadas++;
            }
        }
        return sincronizadas;
    }

    private CategoriaMovimiento obtenerCategoriaCuotas() {
        return categoriaRepository
                .findByNombreIgnoreCaseAndTipo(CATEGORIA_CUOTAS, TipoMovimiento.INGRESO)
                .orElseGet(() -> categoriaRepository.save(
                        new CategoriaMovimiento(CATEGORIA_CUOTAS, TipoMovimiento.INGRESO)));
    }

    private String descripcionCuota(Cuota cuota) {
        String periodo = cuota.getPeriodo() != null ? cuota.getPeriodo() : "sin período";
        String alumno = cuota.getAlumno() != null && cuota.getAlumno().getNya() != null
                ? cuota.getAlumno().getNya()
                : "Alumno";
        String curso = cuota.getCurso() != null && cuota.getCurso().getNombre() != null
                ? cuota.getCurso().getNombre()
                : "Curso";
        String descripcion = String.format("Cuota %s - %s - %s", periodo, alumno, curso);
        return descripcion.length() > 255 ? descripcion.substring(0, 255) : descripcion;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Actualiza a VENCIDO las cuotas pendientes cuya fecha de vencimiento ya pasó. */
    @Transactional
    public int actualizarVencidas() {
        return marcarVencidas();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void actualizarVencidasProgramado() {
        marcarVencidas();
    }

    private int marcarVencidas() {
        List<Cuota> pendientes = cuotaRepository.findByEstadoAndFechaVencimientoBefore(
                EstadoCuota.PENDIENTE, LocalDate.now());
        if (pendientes.isEmpty()) {
            return 0;
        }
        for (Cuota cuota : pendientes) {
            cuota.setEstado(EstadoCuota.VENCIDO);
        }
        cuotaRepository.saveAll(pendientes);
        return pendientes.size();
    }

    @Transactional
    public List<Cuota> listar(Long cursoId, Long alumnoId, EstadoCuota estado, Integer mes, Integer anio,
                              String busqueda) {
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mes debe estar entre 1 y 12.");
        }
        // Antes de listar, nos aseguramos de que los estados estén al día.
        marcarVencidas();
        return cuotaRepository.buscarConFiltros(
                cursoId, alumnoId, estado, mes, anio, busquedaParaQuery(busqueda));
    }

    @Transactional(readOnly = true)
    public Cuota obtenerPorId(Long id) {
        return cuotaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la cuota con ID " + id + "."));
    }

    @Transactional
    public List<MorosoResponse> listarMorosos(String busqueda) {
        marcarVencidas();
        LocalDate hoy = LocalDate.now();

        List<Cuota> vencidas = cuotaRepository.buscarVencidas(
                EstadoCuota.VENCIDO, EstadoCuota.PENDIENTE, hoy);

        Map<Long, List<Cuota>> agrupadas = new LinkedHashMap<>();
        for (Cuota cuota : vencidas) {
            Alumno alumno = cuota.getAlumno();
            if (alumno == null || alumno.getId() == null) {
                continue;
            }
            agrupadas.computeIfAbsent(alumno.getId(), k -> new ArrayList<>()).add(cuota);
        }

        List<MorosoResponse> resultado = new ArrayList<>();
        for (List<Cuota> cuotasAlumno : agrupadas.values()) {
            Alumno alumno = cuotasAlumno.get(0).getAlumno();
            double totalAdeudado = 0;
            long diasMoraMaximo = 0;
            List<CuotaResumen> detalles = new ArrayList<>();

            for (Cuota cuota : cuotasAlumno) {
                long diasMora = Math.max(0,
                        ChronoUnit.DAYS.between(cuota.getFechaVencimiento(), hoy));
                double monto = cuota.getMonto() != null ? cuota.getMonto() : 0;

                totalAdeudado += monto;
                diasMoraMaximo = Math.max(diasMoraMaximo, diasMora);
                detalles.add(new CuotaResumen(
                        cuota.getId(),
                        cuota.getCurso() != null ? cuota.getCurso().getId() : null,
                        cuota.getCurso() != null ? cuota.getCurso().getNombre() : null,
                        cuota.getPeriodoMes(),
                        cuota.getPeriodoAnio(),
                        cuota.getPeriodo(),
                        monto,
                        cuota.getFechaVencimiento(),
                        cuota.getEstado(),
                        diasMora));
            }

            detalles.sort(Comparator.comparing(CuotaResumen::getFechaVencimiento));
            resultado.add(new MorosoResponse(
                    alumno.getId(), alumno.getNya(), alumno.getDni(), alumno.getLegajo(),
                    detalles.size(), totalAdeudado, diasMoraMaximo, detalles));
        }

        resultado.sort(Comparator.comparingDouble(MorosoResponse::getTotalAdeudado).reversed());

        String filtro = normalizarBusqueda(busqueda);
        if (filtro != null) {
            String f = filtro.toLowerCase();
            resultado.removeIf(m -> !(contiene(m.getNya(), f)
                    || contiene(m.getDni(), f)
                    || contiene(m.getLegajo(), f)));
        }
        return resultado;
    }

    /**
     * Estado de cuenta consolidado por alumno y curso: cuotas, pagos, deuda y situación.
     */
    @Transactional
    public List<EstadoCuentaResponse> listarEstadoCuenta(String busqueda) {
        marcarVencidas();

        List<Cuota> cuotas = cuotaRepository.buscarConFiltros(
                null, null, null, null, null, busquedaParaQuery(busqueda));

        Map<String, List<Cuota>> agrupadas = new LinkedHashMap<>();
        for (Cuota cuota : cuotas) {
            if (cuota.getAlumno() == null || cuota.getCurso() == null) {
                continue;
            }
            String clave = cuota.getAlumno().getId() + "-" + cuota.getCurso().getId();
            agrupadas.computeIfAbsent(clave, k -> new ArrayList<>()).add(cuota);
        }

        List<EstadoCuentaResponse> resultado = new ArrayList<>();
        for (List<Cuota> cuotasCuenta : agrupadas.values()) {
            Alumno alumno = cuotasCuenta.get(0).getAlumno();
            Curso curso = cuotasCuenta.get(0).getCurso();

            int pagadas = 0;
            int pendientes = 0;
            int vencidas = 0;
            double totalFacturado = 0;
            double totalPagado = 0;
            double saldo = 0;

            for (Cuota cuota : cuotasCuenta) {
                double monto = cuota.getMonto() != null ? cuota.getMonto() : 0;
                totalFacturado += monto;

                switch (cuota.getEstado()) {
                    case PAGADO -> {
                        pagadas++;
                        totalPagado += monto;
                    }
                    case VENCIDO -> {
                        vencidas++;
                        saldo += monto;
                    }
                    case PENDIENTE -> {
                        pendientes++;
                        saldo += monto;
                    }
                }
            }

            String estado = vencidas > 0 ? "MOROSO" : (saldo > 0 ? "CON_DEUDA" : "AL_DIA");

            resultado.add(new EstadoCuentaResponse(
                    alumno.getId(), alumno.getNya(), alumno.getDni(), alumno.getLegajo(),
                    curso.getId(), curso.getNombre(), cuotasCuenta.size(),
                    pagadas, pendientes, vencidas, totalFacturado, totalPagado, saldo, estado));
        }

        resultado.sort(Comparator
                .comparingInt((EstadoCuentaResponse e) -> prioridadEstado(e.getEstado()))
                .thenComparing(Comparator.comparingDouble(EstadoCuentaResponse::getSaldo).reversed())
                .thenComparing(EstadoCuentaResponse::getNya, String.CASE_INSENSITIVE_ORDER));

        return resultado;
    }

    @Transactional
    public ResumenCuotasResponse obtenerResumen() {
        marcarVencidas();
        return new ResumenCuotasResponse(
                cuotaRepository.countByEstado(EstadoCuota.PENDIENTE),
                cuotaRepository.countByEstado(EstadoCuota.PAGADO),
                cuotaRepository.countByEstado(EstadoCuota.VENCIDO),
                valorSeguro(cuotaRepository.sumMontoPorEstado(EstadoCuota.PENDIENTE)),
                valorSeguro(cuotaRepository.sumMontoPorEstado(EstadoCuota.PAGADO)),
                valorSeguro(cuotaRepository.sumMontoPorEstado(EstadoCuota.VENCIDO)),
                cuotaRepository.countAlumnosByEstado(EstadoCuota.VENCIDO));
    }

    // ------------------------------------------------------------------
    // Alta manual / baja
    // ------------------------------------------------------------------

    @Transactional
    public Cuota crearCuota(Cuota cuota) {
        if (cuota == null || cuota.getAlumno() == null || cuota.getAlumno().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el alumno.");
        }
        if (cuota.getCurso() == null || cuota.getCurso().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el curso.");
        }
        validarPeriodo(cuota.getPeriodoMes(), cuota.getPeriodoAnio());
        if (cuota.getMonto() == null || cuota.getMonto() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a cero.");
        }
        if (cuota.getFechaVencimiento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar la fecha de vencimiento.");
        }

        Alumno alumno = alumnoRepository.findById(cuota.getAlumno().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el alumno indicado."));
        Curso curso = cursoRepository.findById(cuota.getCurso().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el curso indicado."));

        boolean yaExiste = cuotaRepository.existsByAlumno_IdAndCurso_IdAndPeriodoMesAndPeriodoAnio(
                alumno.getId(), curso.getId(), cuota.getPeriodoMes(), cuota.getPeriodoAnio());
        if (yaExiste) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una cuota para ese alumno, curso y período.");
        }

        cuota.setId(null);
        cuota.setAlumno(alumno);
        cuota.setCurso(curso);
        cuota.setIngresoRegistrado(false);

        if (cuota.getFechaPago() != null) {
            cuota.setEstado(EstadoCuota.PAGADO);
        } else if (cuota.getFechaVencimiento().isBefore(LocalDate.now())) {
            cuota.setEstado(EstadoCuota.VENCIDO);
        } else {
            cuota.setEstado(EstadoCuota.PENDIENTE);
        }

        Cuota guardada = cuotaRepository.save(cuota);
        if (guardada.getEstado() == EstadoCuota.PAGADO) {
            registrarIngresoCuota(guardada, guardada.getFechaPago(), MetodoPago.EFECTIVO);
            guardada = cuotaRepository.save(guardada);

            if (guardada.getAlumno() != null && guardada.getAlumno().getId() != null) {
                alumnoService.registrarActividad(guardada.getAlumno().getId());
            }
        }
        return guardada;
    }

    @Transactional
    public void eliminarCuota(Long id) {
        Cuota cuota = cuotaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la cuota con ID " + id + "."));

        // Si la cuota tenía un ingreso automático, se elimina junto con ella.
        movimientoRepository.deleteByCuota_Id(id);
        cuotaRepository.delete(cuota);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private void validarPeriodo(Integer mes, Integer anio) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mes debe estar entre 1 y 12.");
        }
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El año indicado no es válido.");
        }
    }

    private double valorSeguro(Double valor) {
        return valor != null ? valor : 0d;
    }

    private String normalizarBusqueda(String busqueda) {
        if (busqueda == null) {
            return null;
        }
        String limpio = busqueda.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    /** La consulta JPQL recibe siempre una cadena (nunca null) para evitar ambigüedad de tipos en PostgreSQL. */
    private String busquedaParaQuery(String busqueda) {
        String limpio = normalizarBusqueda(busqueda);
        return limpio != null ? limpio : "";
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private int prioridadEstado(String estado) {
        return switch (estado) {
            case "MOROSO" -> 0;
            case "CON_DEUDA" -> 1;
            default -> 2;
        };
    }
}
