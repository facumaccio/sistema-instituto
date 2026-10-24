package com.instituto.gestion.service;

import com.instituto.gestion.dto.*;
import com.instituto.gestion.model.Alumno;
import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.Inscripcion;
import com.instituto.gestion.repository.AlumnoRepository;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.InscripcionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReporteService {

    private static final int UMBRAL_CASI_LLENO = 80;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.BASIC_ISO_DATE;

    private static final Color COLOR_PRIMARIO = new Color(31, 51, 80);
    private static final Color COLOR_BORDE = new Color(217, 224, 232);
    private static final Color COLOR_TEXTO = new Color(31, 41, 51);
    private static final Color COLOR_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_FILA_ALTERNA = new Color(246, 249, 252);

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    // ------------------------------------------------------------------
    // Tablero
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DashboardReporteResponse obtenerDashboard() {
        List<CursoReporte> cursos = cursoRepository.contarInscriptosActivosPorCurso().stream()
                .map(this::mapearCurso)
                .toList();

        List<CursoReporte> casiLlenos = cursos.stream()
                .filter(CursoReporte::isCasiLleno)
                .sorted(Comparator.comparingDouble(CursoReporte::getOcupacion).reversed()
                        .thenComparing(CursoReporte::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<EvolucionInscripciones> evolucion = construirEvolucion(
                inscripcionRepository.contarInscripcionesUltimosDoceMeses());

        long inscripcionesMesActual = evolucion.isEmpty() ? 0 : evolucion.get(evolucion.size() - 1).getTotal();

        return new DashboardReporteResponse(
                alumnoRepository.contarAlumnosActivos(),
                inscripcionRepository.contarInscripcionesActivas(),
                cursos.size(),
                casiLlenos.size(),
                inscripcionesMesActual,
                UMBRAL_CASI_LLENO,
                cursos,
                casiLlenos,
                evolucion,
                LocalDateTime.now().format(FORMATO_FECHA_HORA));
    }

    private CursoReporte mapearCurso(CursoInscriptosProjection fila) {
        long inscriptos = fila.getInscriptos() != null ? fila.getInscriptos() : 0L;
        Integer cupo = fila.getCupo();
        Double ocupacion = null;
        Integer disponible = null;
        boolean casiLleno = false;

        if (cupo != null && cupo > 0) {
            ocupacion = Math.round((inscriptos * 10000.0) / cupo) / 100.0;
            disponible = (int) Math.max(0, cupo - inscriptos);
            casiLleno = inscriptos * 100 >= cupo * UMBRAL_CASI_LLENO;
        }

        return new CursoReporte(
                fila.getCursoId(),
                fila.getNombre(),
                fila.getTurno(),
                fila.getProfesor(),
                cupo,
                inscriptos,
                ocupacion,
                disponible,
                casiLleno);
    }

    private List<EvolucionInscripciones> construirEvolucion(List<Object[]> filas) {
        Map<String, Long> porPeriodo = new HashMap<>();
        for (Object[] fila : filas) {
            if (fila == null || fila.length < 2 || fila[0] == null || fila[1] == null) {
                continue;
            }
            porPeriodo.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }

        YearMonth mesActual = YearMonth.now();
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

    // ------------------------------------------------------------------
    // Descarga de alumnos por curso
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReporteDescarga generarReporteAlumnos(Long cursoId, String formato) {
        String formatoNormalizado = formato == null ? "csv" : formato.trim().toLowerCase(Locale.ROOT);
        if (!formatoNormalizado.equals("csv") && !formatoNormalizado.equals("pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El formato debe ser csv o pdf.");
        }

        ReporteCursoData datos = obtenerReporteCurso(cursoId);
        return formatoNormalizado.equals("pdf") ? generarPdf(datos) : generarCsv(datos);
    }

    private ReporteCursoData obtenerReporteCurso(Long cursoId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el curso con ID " + cursoId + "."));

        Map<Long, AlumnoCursoFila> alumnosUnicos = new LinkedHashMap<>();
        for (Inscripcion inscripcion : inscripcionRepository.findByCurso_Id(cursoId)) {
            Alumno alumno = inscripcion.getAlumno();
            if (alumno == null || alumno.getId() == null) {
                continue;
            }
            alumnosUnicos.putIfAbsent(alumno.getId(), new AlumnoCursoFila(
                    alumno.getNya(),
                    alumno.getDni(),
                    alumno.getLegajo(),
                    alumno.getEdad(),
                    alumno.getEsRegular(),
                    inscripcion.getFechaInscripcion()));
        }

        List<AlumnoCursoFila> alumnos = new ArrayList<>(alumnosUnicos.values());
        alumnos.sort(Comparator.comparing(fila -> normalizar(fila.nya())));

        return new ReporteCursoData(curso, alumnos);
    }

    private ReporteDescarga generarCsv(ReporteCursoData datos) {
        Curso curso = datos.curso();
        StringBuilder csv = new StringBuilder("\uFEFF");

        csv.append(filaCsv("Listado de alumnos por curso")).append("\r\n");
        csv.append(filaCsv("Curso", texto(curso.getNombre()))).append("\r\n");
        csv.append(filaCsv("Turno", texto(curso.getTurno()))).append("\r\n");
        csv.append(filaCsv("Profesor", curso.getProfesor() != null
                ? texto(curso.getProfesor().getNya()) : "Sin asignar")).append("\r\n");
        csv.append(filaCsv("Generado", LocalDateTime.now().format(FORMATO_FECHA_HORA))).append("\r\n");
        csv.append(filaCsv("Total de alumnos", String.valueOf(datos.alumnos().size()))).append("\r\n");
        csv.append("\r\n");
        csv.append(filaCsv("Alumno", "DNI", "Legajo", "Edad", "Regularidad", "Fecha de inscripción"))
                .append("\r\n");

        if (datos.alumnos().isEmpty()) {
            csv.append(filaCsv("Sin alumnos inscriptos", "", "", "", "", "")).append("\r\n");
        } else {
            for (AlumnoCursoFila alumno : datos.alumnos()) {
                csv.append(filaCsv(
                        texto(alumno.nya()),
                        texto(alumno.dni()),
                        texto(alumno.legajo()),
                        alumno.edad() != null ? String.valueOf(alumno.edad()) : "",
                        regularidad(alumno.esRegular()),
                        formatearFecha(alumno.fechaInscripcion())))
                        .append("\r\n");
            }
        }

        String nombreArchivo = "alumnos-" + slug(curso.getNombre()) + "-"
                + LocalDate.now().format(FORMATO_ARCHIVO) + ".csv";

        return new ReporteDescarga(
                csv.toString().getBytes(StandardCharsets.UTF_8),
                nombreArchivo,
                "text/csv; charset=UTF-8");
    }

    private byte[] generarPdfBytes(ReporteCursoData datos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 46);

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PieDePagina());
            documento.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, COLOR_PRIMARIO);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_MUTED);
            Font info = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXTO);
            Font infoFuerte = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARIO);

            documento.add(new Paragraph("Listado de alumnos por curso", titulo));
            documento.add(new Paragraph("Instituto — Gestión Académica", subtitulo));
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Curso: " + texto(datos.curso().getNombre()), infoFuerte));
            documento.add(new Paragraph(
                    "Turno: " + texto(datos.curso().getTurno())
                            + "    Profesor: " + (datos.curso().getProfesor() != null
                            ? texto(datos.curso().getProfesor().getNya()) : "Sin asignar"), info));
            documento.add(new Paragraph(
                    "Generado: " + LocalDateTime.now().format(FORMATO_FECHA_HORA)
                            + "    Total de alumnos: " + datos.alumnos().size(), info));
            documento.add(new Paragraph(" "));

            if (datos.alumnos().isEmpty()) {
                documento.add(new Paragraph("El curso no tiene alumnos inscriptos.", info));
            } else {
                PdfPTable tabla = new PdfPTable(6);
                tabla.setWidthPercentage(100);
                tabla.setWidths(new float[]{3.4f, 1.6f, 1.4f, 0.8f, 1.5f, 1.7f});
                tabla.setHeaderRows(1);

                Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);
                Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO);

                agregarCelda(tabla, "Alumno", encabezado, Element.ALIGN_LEFT, true, false);
                agregarCelda(tabla, "DNI", encabezado, Element.ALIGN_LEFT, true, false);
                agregarCelda(tabla, "Legajo", encabezado, Element.ALIGN_LEFT, true, false);
                agregarCelda(tabla, "Edad", encabezado, Element.ALIGN_RIGHT, true, false);
                agregarCelda(tabla, "Regularidad", encabezado, Element.ALIGN_LEFT, true, false);
                agregarCelda(tabla, "Inscripción", encabezado, Element.ALIGN_LEFT, true, false);

                int indice = 0;
                for (AlumnoCursoFila alumno : datos.alumnos()) {
                    boolean alterna = indice % 2 == 1;
                    agregarCelda(tabla, texto(alumno.nya()), cuerpo, Element.ALIGN_LEFT, false, alterna);
                    agregarCelda(tabla, texto(alumno.dni()), cuerpo, Element.ALIGN_LEFT, false, alterna);
                    agregarCelda(tabla, texto(alumno.legajo()), cuerpo, Element.ALIGN_LEFT, false, alterna);
                    agregarCelda(tabla, alumno.edad() != null ? String.valueOf(alumno.edad()) : "—",
                            cuerpo, Element.ALIGN_RIGHT, false, alterna);
                    agregarCelda(tabla, regularidad(alumno.esRegular()), cuerpo,
                            Element.ALIGN_LEFT, false, alterna);
                    agregarCelda(tabla, formatearFecha(alumno.fechaInscripcion()), cuerpo,
                            Element.ALIGN_LEFT, false, alterna);
                    indice++;
                }

                documento.add(tabla);
            }
        } catch (DocumentException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el PDF del curso.");
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }

        return salida.toByteArray();
    }

    private ReporteDescarga generarPdf(ReporteCursoData datos) {
        byte[] contenido = generarPdfBytes(datos);
        String nombreArchivo = "alumnos-" + slug(datos.curso().getNombre()) + "-"
                + LocalDate.now().format(FORMATO_ARCHIVO) + ".pdf";
        return new ReporteDescarga(contenido, nombreArchivo, "application/pdf");
    }

    private void agregarCelda(PdfPTable tabla, String valor, Font fuente, int alineacion,
                              boolean encabezado, boolean alterna) {
        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setPadding(6.5f);
        celda.setHorizontalAlignment(alineacion);
        celda.setBorderColor(COLOR_BORDE);
        if (encabezado) {
            celda.setBackgroundColor(COLOR_PRIMARIO);
        } else if (alterna) {
            celda.setBackgroundColor(COLOR_FILA_ALTERNA);
        }
        tabla.addCell(celda);
    }

    private String filaCsv(String... valores) {
        StringBuilder fila = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                fila.append(';');
            }
            String valor = valores[i] == null ? "" : valores[i];
            fila.append('"').append(valor.replace("\"", "\"\"")).append('"');
        }
        return fila.toString();
    }

    private String regularidad(Boolean esRegular) {
        if (esRegular == null) {
            return "Sin definir";
        }
        return esRegular ? "Regular" : "No regular";
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "—";
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor.trim();
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String slug(String valor) {
        String base = normalizar(valor)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return base.isBlank() ? "curso" : base;
    }

    private record ReporteCursoData(Curso curso, List<AlumnoCursoFila> alumnos) {
    }

    private record AlumnoCursoFila(String nya, String dni, String legajo, Integer edad,
                                   Boolean esRegular, LocalDate fechaInscripcion) {
    }

    private static final class PieDePagina extends PdfPageEventHelper {
        private final Font fuente = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            float y = document.bottom() - 15;
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("Instituto — Gestión Académica", fuente),
                    document.left(), y, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), fuente),
                    document.right(), y, 0);
        }
    }
}
