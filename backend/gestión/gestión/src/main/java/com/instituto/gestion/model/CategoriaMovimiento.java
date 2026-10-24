package com.instituto.gestion.model;

import jakarta.persistence.*;

/**
 * Categoría de un movimiento financiero. Cada categoría pertenece a un tipo
 * (INGRESO o EGRESO) para que el formulario pueda filtrarlas dinámicamente.
 */
@Entity
@Table(
    name = "categoria_movimiento",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_categoria_movimiento_nombre_tipo",
        columnNames = {"nombre", "tipo"}
    )
)
public class CategoriaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    public CategoriaMovimiento() {}

    public CategoriaMovimiento(String nombre, TipoMovimiento tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
}
