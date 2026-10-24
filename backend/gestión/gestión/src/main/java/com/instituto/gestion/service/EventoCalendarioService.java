package com.instituto.gestion.service;

import com.instituto.gestion.dto.EventoCalendarioRequest;
import com.instituto.gestion.dto.EventoCalendarioResponse;
import com.instituto.gestion.dto.TipoEventoResponse;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.EventoCalendario;
import com.instituto.gestion.model.TipoEvento;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.EventoCalendarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class EventoCalendarioService {

    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);
    private static final int MAX_TITULO = 160;
    private static final int MAX_DESCRIPCION = 2000;
    private static final int MAX_DIAS_PROXIMOS = 365;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private CursoRepository cursoRepository;

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

    @Transactional
    public EventoCalendarioResponse crear(EventoCalendarioRequest request) {
        Curso curso = validarRequest(request);

        EventoCalendario evento = new EventoCalendario();
        aplicarDatos(evento, request, curso);

        return mapearEvento(eventoRepository.save(evento));
    }

    @Transactional
    public EventoCalendarioResponse actualizar(Long id, EventoCalendarioRequest request) {
        EventoCalendario evento = buscarEvento(id);
        Curso curso = validarRequest(request);

        aplicarDatos(evento, request, curso);

        return mapearEvento(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(Long id) {
        EventoCalendario evento = buscarEvento(id);
        eventoRepository.delete(evento);
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
