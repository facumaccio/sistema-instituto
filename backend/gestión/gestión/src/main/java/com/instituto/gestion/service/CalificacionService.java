package com.instituto.gestion.service;

import com.instituto.gestion.dto.CalificacionItemRequest;
import com.instituto.gestion.dto.CalificacionLoteRequest;
import com.instituto.gestion.dto.CalificacionResponse;
import com.instituto.gestion.dto.HistorialAlumnoResponse;
import com.instituto.gestion.dto.NotaExamenAlumnoResponse;
import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.model.Calificacion;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.EstadoAlumno;
import com.instituto.gestion.model.EventoCalendario;
import com.instituto.gestion.model.Inscripcion;
import com.instituto.gestion.model.TipoEvento;
import com.instituto.gestion.repository.CalificacionRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.EventoCalendarioRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalificacionService {

    private static final BigDecimal NOTA_MINIMA = BigDecimal.ONE;
    private static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;
    private static final BigDecimal NOTA_APROBATORIA = new BigDecimal("6.0");
    private static final int MAX_OBSERVACIONES = 500;

    private static final Comparator<Calificacion> ORDEN_EXAMEN =
            Comparator.comparing(
                    (Calificacion calificacion) -> calificacion.getEventoExamen().getFechaInicio(),
                    Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                    calificacion -> calificacion.getEventoExamen().getHora(),
                    Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                    Calificacion::getId,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    /**
     * Devuelve una fila por cada alumno activo del curso, tenga o no una nota
     * cargada para el examen indicado.
     */
    @Transactional(readOnly = true)
    public List<NotaExamenAlumnoResponse> listarNotasPorExamen(Long eventoId) {
        EventoCalendario evento = buscarExamen(eventoId);
        Long cursoId = evento.getCurso().getId();

        List<Inscripcion> inscripciones = inscripcionRepository
                .findByCurso_IdAndAlumno_EstadoOrderByFechaInscripcionDesc(
                        cursoId, EstadoAlumno.ACTIVO);

        Map<Long, Calificacion> notas = calificacionRepository
                .findByEventoExamen_Id(eventoId)
                .stream()
                .collect(Collectors.toMap(
                        calificacion -> calificacion.getAlumno().getId(),
                        Function.identity(),
                        (existente, duplicada) -> existente));

        return inscripciones.stream()
                .map(inscripcion -> {
                    Alumno alumno = inscripcion.getAlumno();
                    Calificacion calificacion = notas.get(alumno.getId());

                    return new NotaExamenAlumnoResponse(
                            alumno.getId(),
                            alumno.getNya(),
                            alumno.getDni(),
                            alumno.getLegajo(),
                            calificacion != null ? calificacion.getId() : null,
                            calificacion != null ? calificacion.getNota() : null,
                            calificacion != null ? calificacion.getObservaciones() : null,
                            calificacion != null ? calificacion.getFechaCarga() : null,
                            calificacion != null ? estaAprobada(calificacion.getNota()) : null);
                })
                .sorted(Comparator.comparing(
                        NotaExamenAlumnoResponse::getAlumnoNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    /**
     * Guarda o actualiza todas las notas de un examen. Si una fila llega con
     * nota nula, se elimina la nota existente de ese alumno (permite limpiar
     * una carga sin borrar el examen).
     */
    @Transactional
    public List<CalificacionResponse> guardarLote(CalificacionLoteRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos de las calificaciones son obligatorios.");
        }

        EventoCalendario evento = buscarExamen(request.getEventoId());
        List<CalificacionItemRequest> items = request.getCalificaciones();

        if (items == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enviá la lista de calificaciones a guardar.");
        }

        Curso curso = evento.getCurso();
        List<Inscripcion> inscripciones = inscripcionRepository
                .findByCurso_IdAndAlumno_EstadoOrderByFechaInscripcionDesc(
                        curso.getId(), EstadoAlumno.ACTIVO);

        Map<Long, Alumno> alumnosActivos = inscripciones.stream()
                .collect(Collectors.toMap(
                        inscripcion -> inscripcion.getAlumno().getId(),
                        Inscripcion::getAlumno,
                        (existente, duplicado) -> existente));

        Map<Long, Calificacion> existentes = calificacionRepository
                .findByEventoExamen_Id(evento.getId())
                .stream()
                .collect(Collectors.toMap(
                        calificacion -> calificacion.getAlumno().getId(),
                        Function.identity(),
                        (existente, duplicada) -> existente));

        Set<Long> procesados = new HashSet<>();
        List<Calificacion> guardadas = new ArrayList<>();

        for (CalificacionItemRequest item : items) {
            if (item == null || item.getAlumnoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada fila de calificación debe indicar el alumno.");
            }

            Long alumnoId = item.getAlumnoId();

            if (!procesados.add(alumnoId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El alumno con ID " + alumnoId + " está repetido en el lote.");
            }

            Alumno alumno = alumnosActivos.get(alumnoId);

            if (alumno == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El alumno con ID " + alumnoId
                                + " no está inscripto activamente en este curso.");
            }

            BigDecimal nota = validarNota(item.getNota());
            Calificacion calificacion = existentes.get(alumnoId);

            if (nota == null) {
                if (calificacion != null) {
                    calificacionRepository.delete(calificacion);
                }
                continue;
            }

            if (calificacion == null) {
                calificacion = new Calificacion();
                calificacion.setAlumno(alumno);
                calificacion.setCurso(curso);
                calificacion.setEventoExamen(evento);
            }

            calificacion.setNota(nota);
            calificacion.setObservaciones(normalizarObservaciones(item.getObservaciones()));
            calificacion.setFechaCarga(LocalDate.now());

            guardadas.add(calificacionRepository.save(calificacion));
        }

        return guardadas.stream().map(this::mapearCalificacion).toList();
    }

    /**
     * Historial de notas por alumno activo del curso, ordenado por nombre, con
     * el promedio calculado sobre todas las notas cargadas en ese curso.
     */
    @Transactional(readOnly = true)
    public List<HistorialAlumnoResponse> historialPorCurso(Long cursoId) {
        cursoRepository.findById(cursoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el curso con ID " + cursoId + "."));

        List<Inscripcion> inscripciones = inscripcionRepository
                .findByCurso_IdAndAlumno_EstadoOrderByFechaInscripcionDesc(
                        cursoId, EstadoAlumno.ACTIVO);

        List<Calificacion> calificaciones = calificacionRepository.findByCurso_Id(cursoId);

        Map<Long, List<Calificacion>> porAlumno = calificaciones.stream()
                .collect(Collectors.groupingBy(
                        calificacion -> calificacion.getAlumno().getId()));

        return inscripciones.stream()
                .map(inscripcion -> {
                    Alumno alumno = inscripcion.getAlumno();
                    List<Calificacion> notas = porAlumno
                            .getOrDefault(alumno.getId(), List.of())
                            .stream()
                            .sorted(ORDEN_EXAMEN)
                            .toList();

                    return new HistorialAlumnoResponse(
                            alumno.getId(),
                            alumno.getNya(),
                            alumno.getDni(),
                            alumno.getLegajo(),
                            calcularPromedio(notas),
                            notas.size(),
                            notas.stream().map(this::mapearCalificacion).toList());
                })
                .sorted(Comparator.comparing(
                        HistorialAlumnoResponse::getAlumnoNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private EventoCalendario buscarExamen(Long eventoId) {
        if (eventoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seleccioná un examen.");
        }

        EventoCalendario evento = eventoRepository.buscarPorId(eventoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El examen indicado no existe."));

        if (evento.getTipo() != TipoEvento.EXAMEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El evento seleccionado no es un examen.");
        }

        if (evento.getCurso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El examen no está asociado a un curso.");
        }

        return evento;
    }

    private BigDecimal validarNota(BigDecimal nota) {
        if (nota == null) {
            return null;
        }

        if (nota.compareTo(NOTA_MINIMA) < 0 || nota.compareTo(NOTA_MAXIMA) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nota debe estar entre 1 y 10.");
        }

        BigDecimal doble = nota.multiply(BigDecimal.valueOf(2));
        if (doble.stripTrailingZeros().scale() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nota debe cargarse en pasos de 0.5 (por ejemplo 6, 6.5 o 7).");
        }

        return nota.setScale(1, RoundingMode.HALF_UP);
    }

    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }

        String limpio = observaciones.trim();

        if (limpio.length() > MAX_OBSERVACIONES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La observación no puede superar los " + MAX_OBSERVACIONES + " caracteres.");
        }

        return limpio;
    }

    private BigDecimal calcularPromedio(List<Calificacion> notas) {
        List<BigDecimal> valores = notas.stream()
                .map(Calificacion::getNota)
                .filter(Objects::nonNull)
                .toList();

        if (valores.isEmpty()) {
            return null;
        }

        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }

    private Boolean estaAprobada(BigDecimal nota) {
        return nota == null ? null : nota.compareTo(NOTA_APROBATORIA) >= 0;
    }

    private CalificacionResponse mapearCalificacion(Calificacion calificacion) {
        Alumno alumno = calificacion.getAlumno();
        EventoCalendario evento = calificacion.getEventoExamen();

        return new CalificacionResponse(
                calificacion.getId(),
                alumno != null ? alumno.getId() : null,
                alumno != null ? alumno.getNya() : null,
                alumno != null ? alumno.getDni() : null,
                alumno != null ? alumno.getLegajo() : null,
                calificacion.getCurso() != null ? calificacion.getCurso().getId() : null,
                evento != null ? evento.getId() : null,
                evento != null ? evento.getTitulo() : null,
                evento != null ? evento.getFechaInicio() : null,
                evento != null ? evento.getHora() : null,
                calificacion.getNota(),
                calificacion.getObservaciones(),
                calificacion.getFechaCarga(),
                estaAprobada(calificacion.getNota()));
    }
}
