package com.instituto.gestion.dto;

import java.util.List;

/** Respuesta consolidada del panel de estadísticas y reportes. */
public class DashboardReporteResponse {

    private final long totalAlumnosActivos;
    private final long totalInscripcionesActivas;
    private final long totalCursos;
    private final long totalCursosCasiLlenos;
    private final long inscripcionesMesActual;
    private final int umbralCasiLleno;
    private final List<CursoReporte> cursos;
    private final List<CursoReporte> cursosCasiLlenos;
    private final List<EvolucionInscripciones> evolucionMensual;
    private final String generadoEn;

    public DashboardReporteResponse(long totalAlumnosActivos, long totalInscripcionesActivas,
                                    long totalCursos, long totalCursosCasiLlenos,
                                    long inscripcionesMesActual, int umbralCasiLleno,
                                    List<CursoReporte> cursos,
                                    List<CursoReporte> cursosCasiLlenos,
                                    List<EvolucionInscripciones> evolucionMensual,
                                    String generadoEn) {
        this.totalAlumnosActivos = totalAlumnosActivos;
        this.totalInscripcionesActivas = totalInscripcionesActivas;
        this.totalCursos = totalCursos;
        this.totalCursosCasiLlenos = totalCursosCasiLlenos;
        this.inscripcionesMesActual = inscripcionesMesActual;
        this.umbralCasiLleno = umbralCasiLleno;
        this.cursos = cursos;
        this.cursosCasiLlenos = cursosCasiLlenos;
        this.evolucionMensual = evolucionMensual;
        this.generadoEn = generadoEn;
    }

    public long getTotalAlumnosActivos() { return totalAlumnosActivos; }
    public long getTotalInscripcionesActivas() { return totalInscripcionesActivas; }
    public long getTotalCursos() { return totalCursos; }
    public long getTotalCursosCasiLlenos() { return totalCursosCasiLlenos; }
    public long getInscripcionesMesActual() { return inscripcionesMesActual; }
    public int getUmbralCasiLleno() { return umbralCasiLleno; }
    public List<CursoReporte> getCursos() { return cursos; }
    public List<CursoReporte> getCursosCasiLlenos() { return cursosCasiLlenos; }
    public List<EvolucionInscripciones> getEvolucionMensual() { return evolucionMensual; }
    public String getGeneradoEn() { return generadoEn; }
}
