package com.instituto.gestion.service;

import com.instituto.gestion.dto.EventoCalendarioRequest;
import com.instituto.gestion.dto.EventoCalendarioResponse;
import com.instituto.gestion.dto.TipoEventoResponse;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.EstadoAlumno;
import com.instituto.gestion.model.EventoCalendario;
import com.instituto.gestion.model.TipoEvento;
import com.instituto.gestion.repository.CalificacionRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.EventoCalendarioRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class EventoCalendarioService {

    private static final Logger log = LoggerFactory.getLogger(EventoCalendarioService.class);
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);
    private static final int MAX_TITULO = 160;
    private static final int MAX_DESCRIPCION = 2000;
    private static final int MAX_DIAS_PROXIMOS = 365;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EmailService emailService;

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponse> listar(
            LocalDate desde,
            LocalDate hasta,
            TipoEvento tipo,
            Long cursoId
    ) {
        validarRango(desde, hasta);

        LocalDate desdeSeguro = desde != null ? desde : FECHA_MINIMA;
        LocalDate hastaSeguro = hasta != null ? hasta : FECHA_MAXIMA;
        long cursoSeguro = cursoId != null ? cursoId : 0L;

        List<EventoCalendario> eventos = tipo == null
                ? eventoRepository.buscarEnRango(desdeSeguro, hastaSeguro, cursoSeguro)
                : eventoRepository.buscarEnRangoPorTipo(tipo, desdeSeguro, hastaSeguro, cursoSeguro);

        return eventos.stream().map(this::mapearEvento).toList();
    }

    @Transactional(readOnly = true)
    public EventoCalendarioResponse obtener(Long id) {
        return mapearEvento(buscarEvento(id));
    }

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponse> listarProximos(Integer dias) {
        int diasSeguro = dias != null ? dias : 7;

        if (diasSeguro < 1 || diasSeguro > MAX_DIAS_PROXIMOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad de días debe estar entre 1 y " + MAX_DIAS_PROXIMOS + ".");
        }

        LocalDate hoy = LocalDate.now();
        return eventoRepository.buscarProximos(hoy, hoy.plusDays(diasSeguro)).stream()
                .map(this::mapearEvento)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TipoEventoResponse> listarTipos() {
        return Arrays.stream(TipoEvento.values())
                .map(tipo -> new TipoEventoResponse(tipo, tipo.getEtiqueta(), tipo.getColor()))
                .toList();
    }

    /**
     * Exámenes agendados para un curso, ordenados cronológicamente. Son los
     * eventos del calendario que se usan como instancias de evaluación.
     */
    @Transactional(readOnly = true)
    public List<EventoCalendarioResponse> listarExamenesPorCurso(Long cursoId) {
        if (cursoId == null || !cursoRepository.existsById(cursoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró el curso con ID " + cursoId + ".");
        }

        return eventoRepository
                .findByCurso_IdAndTipoOrderByFechaInicioAscHoraAsc(cursoId, TipoEvento.EXAMEN)
                .stream()
                .map(this::mapearEvento)
                .toList();
    }

    @Transactional
    public EventoCalendarioResponse crear(EventoCalendarioRequest request) {
        Curso curso = validarRequest(request);

        EventoCalendario evento = new EventoCalendario();
        aplicarDatos(evento, request, curso);

        EventoCalendario eventoGuardado = eventoRepository.save(evento);
        notificarNuevoExamen(eventoGuardado);

        return mapearEvento(eventoGuardado);
    }

    /**
     * Dispara la notificación por correo cuando se crea un examen asociado a
     * un curso. La consulta de destinatarios se resuelve dentro de la
     * transacción; el envío real queda a cargo del método @Async.
     */
    private void notificarNuevoExamen(EventoCalendario evento) {
        if (evento.getTipo() != TipoEvento.EXAMEN || evento.getCurso() == null) {
            return;
        }

        try {
            List<String> destinatarios = inscripcionRepository
                    .findEmailsAlumnosPorCursoYEstado(
                            evento.getCurso().getId(),
                            EstadoAlumno.ACTIVO);

            emailService.notificarNuevoExamen(
                    destinatarios,
                    evento.getCurso().getNombre(),
                    evento.getTitulo(),
                    evento.getFechaInicio(),
                    evento.getHora(),
                    evento.getDescripcion());
        } catch (Exception error) {
            // Un problema al preparar o encolar la notificación nunca debe
            // impedir que el examen quede guardado.
            log.error("No se pudo preparar la notificación del examen '{}': {}",
                    evento.getTitulo(), error.getMessage(), error);
        }
    }

    @Transactional
    public EventoCalendarioResponse actualizar(Long id, EventoCalendarioRequest request) {
        EventoCalendario evento = buscarEvento(id);
        Curso curso = validarRequest(request);

        validarCambioConNotas(evento, request, curso);

        aplicarDatos(evento, request, curso);

        return mapearEvento(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(Long id) {
        EventoCalendario evento = buscarEvento(id);

        if (calificacionRepository.existsByEventoExamen_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el examen porque tiene notas cargadas. "
                            + "Primero quitá las notas desde el detalle del curso.");
        }

        eventoRepository.delete(evento);
    }

    /**
     * Un examen con notas no puede cambiar de curso ni dejar de ser EXAMEN:
     * las calificaciones quedarían vinculadas a un evento inconsistente.
     */
    private void validarCambioConNotas(
            EventoCalendario evento,
            EventoCalendarioRequest request,
            Curso cursoNuevo
    ) {
        if (!calificacionRepository.existsByEventoExamen_Id(evento.getId())) {
            return;
        }

        Long cursoActualId = evento.getCurso() != null ? evento.getCurso().getId() : null;
        Long cursoNuevoId = cursoNuevo != null ? cursoNuevo.getId() : null;

        if (request.getTipo() != TipoEvento.EXAMEN
                || !Objects.equals(cursoActualId, cursoNuevoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar el tipo ni el curso de un examen que ya tiene notas cargadas.");
        }
    }

    private EventoCalendario buscarEvento(Long id) {
        return eventoRepository.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El evento del calendario no existe."));
    }

    private Curso validarRequest(EventoCalendarioRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del evento son obligatorios.");
        }

        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ingresá un título para el evento.");
        }

        if (request.getTitulo().trim().length() > MAX_TITULO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El título no puede superar los " + MAX_TITULO + " caracteres.");
        }

        if (request.getTipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seleccioná el tipo de evento.");
        }

        if (request.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indicá la fecha de inicio del evento.");
        }

        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (request.getDescripcion() != null
                && request.getDescripcion().trim().length() > MAX_DESCRIPCION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción no puede superar los " + MAX_DESCRIPCION + " caracteres.");
        }

        if (request.getCursoId() == null) {
            return null;
        }

        return cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El curso seleccionado no existe."));
    }

    private void aplicarDatos(
            EventoCalendario evento,
            EventoCalendarioRequest request,
            Curso curso
    ) {
        evento.setTitulo(request.getTitulo().trim());
        evento.setDescripcion(normalizarDescripcion(request.getDescripcion()));
        evento.setFechaInicio(request.getFechaInicio());
        evento.setFechaFin(request.getFechaFin());
        evento.setHora(request.getHora());
        evento.setTipo(request.getTipo());
        evento.setColor(request.getTipo().getColor());
        evento.setCurso(curso);
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        return descripcion.trim();
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }

    private EventoCalendarioResponse mapearEvento(EventoCalendario evento) {
        Curso curso = evento.getCurso();
        TipoEvento tipo = evento.getTipo();
        String color = evento.getColor() != null && !evento.getColor().isBlank()
                ? evento.getColor()
                : (tipo != null ? tipo.getColor() : null);

        return new EventoCalendarioResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescripcion(),
                evento.getFechaInicio(),
                evento.getFechaFin(),
                evento.getHora(),
                tipo,
                tipo != null ? tipo.getEtiqueta() : null,
                color,
                curso != null ? curso.getId() : null,
                curso != null ? curso.getNombre() : null,
                curso != null ? curso.getTurno() : null
        );
    }
}
