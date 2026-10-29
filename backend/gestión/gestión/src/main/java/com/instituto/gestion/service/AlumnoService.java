package com.instituto.gestion.service;

import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.model.EstadoAlumno;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CuotaRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.instituto.gestion.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AlumnoService {

    private static final String MOTIVO_ARCHIVADO_AUTOMATICO =
            "Archivado automático por más de 3 meses sin actividad ni pagos.";
    private static final int MAX_EMAIL = 160;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private AlumnoRepository repository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    /**
     * Listado operativo: solo alumnos ACTIVO. Antes de consultar se ejecuta la
     * verificación de inactividad para que los mayores a tres meses queden
     * archivados automáticamente.
     */
    @Transactional
    public List<Alumno> obtenerActivos() {
        archivarInactivosInterno();
        return repository.findByEstadoOrderByNyaAsc(EstadoAlumno.ACTIVO);
    }

    /**
     * Listado de archivo: alumnos desafiliados manualmente o archivados por
     * inactividad. Los registros se conservan completos.
     */
    @Transactional
    public List<Alumno> obtenerArchivados() {
        archivarInactivosInterno();
        return repository.findByEstadoInOrderByNyaAsc(
                List.of(EstadoAlumno.DESAFILIADO, EstadoAlumno.INACTIVO_OCULTO));
    }

    @Transactional
    public Alumno guardarAlumno(Alumno alumno) {
        validarYNormalizar(alumno);

        // El alta siempre crea un registro nuevo; el ID se ignora aunque venga
        // en el cuerpo de la petición.
        alumno.setId(null);

        if (repository.existsByDni(alumno.getDni())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un alumno (activo o ex-alumno) con el DNI " + alumno.getDni()
                            + ". Si está archivado, reactivalo desde la vista de ex-alumnos.");
        }

        // El alta siempre comienza activa y sin datos de baja.
        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setFechaUltimaActividad(LocalDate.now());
        alumno.setFechaBaja(null);
        alumno.setMotivoBaja(null);

        return repository.save(alumno);
    }

    @Transactional
    public Alumno actualizarAlumno(Long id, Alumno alumnoActualizado) {
        Alumno alumno = buscarAlumno(id);

        validarYNormalizar(alumnoActualizado);

        if (repository.existsByDniAndIdNot(alumnoActualizado.getDni(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro alumno con el DNI " + alumnoActualizado.getDni() + ".");
        }

        // Los datos administrativos (estado, fechas y motivo de baja) no se
        // modifican desde la edición común del legajo.
        alumno.setNya(alumnoActualizado.getNya());
        alumno.setDni(alumnoActualizado.getDni());
        alumno.setEdad(alumnoActualizado.getEdad());
        alumno.setLegajo(alumnoActualizado.getLegajo());
        alumno.setEmail(alumnoActualizado.getEmail());
        alumno.setEsRegular(alumnoActualizado.getEsRegular());
        return repository.save(alumno);
    }

    /**
     * Baja lógica manual. No elimina relaciones ni historial.
     */
    @Transactional
    public Alumno desafiliarAlumno(Long id, String motivo) {
        Alumno alumno = buscarAlumno(id);

        alumno.setEstado(EstadoAlumno.DESAFILIADO);
        alumno.setFechaBaja(LocalDate.now());
        alumno.setMotivoBaja(normalizarMotivo(motivo));

        return repository.save(alumno);
    }

    /**
     * Reincorpora al alumno conservando todas sus inscripciones, cuotas y
     * pagos. La fecha de última actividad vuelve a ser hoy.
     */
    @Transactional
    public Alumno reactivarAlumno(Long id) {
        Alumno alumno = buscarAlumno(id);

        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setFechaUltimaActividad(LocalDate.now());
        alumno.setFechaBaja(null);
        alumno.setMotivoBaja(null);

        return repository.save(alumno);
    }

    /**
     * Registra actividad del alumno (pago, asistencia o inscripción).
     *
     * <p>Si el alumno estaba oculto por inactividad, vuelve a ACTIVO: la
     * actividad reciente ya no cumple la regla de tres meses. Una baja manual
     * (DESAFILIADO) se conserva y solo se actualiza la fecha.</p>
     */
    @Transactional
    public Alumno registrarActividad(Long alumnoId, LocalDate fecha) {
        if (alumnoId == null) {
            return null;
        }

        Alumno alumno = repository.findById(alumnoId).orElse(null);
        if (alumno == null) {
            return null;
        }

        LocalDate actividad = fecha != null ? fecha : LocalDate.now();
        if (alumno.getFechaUltimaActividad() == null
                || actividad.isAfter(alumno.getFechaUltimaActividad())) {
            alumno.setFechaUltimaActividad(actividad);
        }

        if (alumno.getEstado() == EstadoAlumno.INACTIVO_OCULTO) {
            alumno.setEstado(EstadoAlumno.ACTIVO);
            alumno.setFechaBaja(null);
            alumno.setMotivoBaja(null);
        }

        return repository.save(alumno);
    }

    @Transactional
    public Alumno registrarActividad(Long alumnoId) {
        return registrarActividad(alumnoId, LocalDate.now());
    }

    /**
     * Verificación diaria de respaldo. El listado también la ejecuta para que
     * el archivado sea inmediato cuando se consulta la pantalla.
     */
    @Scheduled(cron = "0 10 3 * * *")
    @Transactional
    public void archivarInactivosProgramado() {
        archivarInactivosInterno();
    }

    private void archivarInactivosInterno() {
        // Filas heredadas sin estado: se consideran activas hasta evaluarlas.
        repository.marcarEstadoInicial(EstadoAlumno.ACTIVO);

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.minusMonths(3);
        List<Alumno> activos = repository.findByEstadoOrderByNyaAsc(EstadoAlumno.ACTIVO);
        List<Alumno> cambios = new ArrayList<>();

        for (Alumno alumno : activos) {
            LocalDate ultima = alumno.getFechaUltimaActividad();

            // Las filas heredadas no tienen fecha. Se reconstruye una sola vez
            // con el último pago o inscripción; luego los flujos de pago,
            // inscripción y asistencia mantienen el dato actualizado.
            if (ultima == null) {
                ultima = calcularUltimaActividad(alumno.getId());
                if (ultima == null) {
                    // Sin actividad histórica conocida: se inicializa hoy para
                    // no archivarlo por falta de datos.
                    ultima = hoy;
                }
                alumno.setFechaUltimaActividad(ultima);
                cambios.add(alumno);
            }

            if (ultima.isBefore(limite)) {
                alumno.setEstado(EstadoAlumno.INACTIVO_OCULTO);
                alumno.setFechaBaja(hoy);
                alumno.setMotivoBaja(MOTIVO_ARCHIVADO_AUTOMATICO);
                if (!cambios.contains(alumno)) {
                    cambios.add(alumno);
                }
            }
        }

        if (!cambios.isEmpty()) {
            repository.saveAll(cambios);
        }
    }

    private LocalDate calcularUltimaActividad(Long alumnoId) {
        LocalDate ultima = null;
        ultima = maxFecha(ultima, cuotaRepository.findUltimaFechaPagoByAlumnoId(alumnoId));
        ultima = maxFecha(ultima, pagoRepository.findUltimaFechaPagoByAlumnoId(alumnoId));
        ultima = maxFecha(ultima, inscripcionRepository.findUltimaFechaInscripcionByAlumnoId(alumnoId));
        return ultima;
    }

    private LocalDate maxFecha(LocalDate actual, LocalDate candidata) {
        if (candidata == null) {
            return actual;
        }
        return actual == null || candidata.isAfter(actual) ? candidata : actual;
    }

    private Alumno buscarAlumno(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el alumno con ID " + id + "."));
    }

    private void validarYNormalizar(Alumno alumno) {
        if (alumno == null || alumno.getNya() == null || alumno.getNya().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y apellido es obligatorio.");
        }

        if (alumno.getDni() == null || alumno.getDni().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI es obligatorio.");
        }

        String dni = alumno.getDni().replaceAll("[.\\s]", "").trim();
        if (!dni.matches("\\d{7,8}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI debe ser un número válido de 7 u 8 cifras.");
        }

        if (alumno.getEdad() != null && (alumno.getEdad() < 1 || alumno.getEdad() > 120)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La edad debe estar entre 1 y 120 años.");
        }

        alumno.setNya(alumno.getNya().trim());
        alumno.setDni(dni);
        alumno.setLegajo(normalizarLegajo(alumno.getLegajo()));
        alumno.setEmail(normalizarEmail(alumno.getEmail()));
        if (alumno.getEsRegular() == null) {
            alumno.setEsRegular(true);
        }
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String limpio = email.trim();
        if (limpio.length() > MAX_EMAIL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico no puede superar los " + MAX_EMAIL + " caracteres.");
        }

        if (!EMAIL_PATTERN.matcher(limpio).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico no tiene un formato válido.");
        }

        return limpio;
    }

    private String normalizarLegajo(String legajo) {
        return legajo == null || legajo.isBlank() ? null : legajo.trim();
    }

    private String normalizarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        String limpio = motivo.trim();
        return limpio.length() > 255 ? limpio.substring(0, 255) : limpio;
    }
}
