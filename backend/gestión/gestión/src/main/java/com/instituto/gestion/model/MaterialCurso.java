package com.instituto.gestion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Recurso de estudio asociado a un curso. El contenido pesado (PDFs, videos,
 * carpetas de Drive, etc.) vive fuera del sistema; acá solo se guarda el
 * enlace y los datos descriptivos.
 */
@Entity
@Table(
    name = "material_curso",
    indexes = {
        @Index(name = "idx_material_curso_curso", columnList = "curso_id"),
        @Index(name = "idx_material_curso_unidad", columnList = "unidad_o_clase"),
        @Index(name = "idx_material_curso_fecha", columnList = "fecha_publicacion")
    }
)
public class MaterialCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "unidad_o_clase", length = 120)
    private String unidadOClase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMaterial tipo;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDate fechaPublicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    @JsonIgnore
    private Curso curso;

    public MaterialCurso() {}

    @PrePersist
    private void completarFechaPublicacion() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDate.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUnidadOClase() { return unidadOClase; }
    public void setUnidadOClase(String unidadOClase) { this.unidadOClase = unidadOClase; }

    public TipoMaterial getTipo() { return tipo; }
    public void setTipo(TipoMaterial tipo) { this.tipo = tipo; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
}
