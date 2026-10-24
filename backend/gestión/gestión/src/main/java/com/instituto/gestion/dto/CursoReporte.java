package com.instituto.gestion.dto;

/** Fila de curso utilizada por el tablero y por los reportes descargables. */
public class CursoReporte {

    private final Long cursoId;
    private final String nombre;
    private final String turno;
    private final String profesor;
    private final Integer cupo;
    private final long inscriptos;
    private final Double ocupacion;
    private final Integer cupoDisponible;
    private final boolean casiLleno;

    public CursoReporte(Long cursoId, String nombre, String turno, String profesor,
                        Integer cupo, long inscriptos, Double ocupacion,
                        Integer cupoDisponible, boolean casiLleno) {
        this.cursoId = cursoId;
        this.nombre = nombre;
        this.turno = turno;
        this.profesor = profesor;
        this.cupo = cupo;
        this.inscriptos = inscriptos;
        this.ocupacion = ocupacion;
        this.cupoDisponible = cupoDisponible;
        this.casiLleno = casiLleno;
    }

    public Long getCursoId() { return cursoId; }
    public String getNombre() { return nombre; }
    public String getTurno() { return turno; }
    public String getProfesor() { return profesor; }
    public Integer getCupo() { return cupo; }
    public long getInscriptos() { return inscriptos; }
    public Double getOcupacion() { return ocupacion; }
    public Integer getCupoDisponible() { return cupoDisponible; }
    public boolean isCasiLleno() { return casiLleno; }
}
