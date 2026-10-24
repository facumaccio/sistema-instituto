package com.instituto.gestion.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Movimiento de caja del instituto. Los ingresos y egresos comparten la misma
 * entidad y se diferencian por el tipo, lo que permite calcular el balance
 * neto y agrupar los gastos por categoría con una sola fuente de datos.
 */
@Entity
@Table(
    name = "movimiento_financiero",
    indexes = {
        @Index(name = "idx_movimiento_financiero_fecha", columnList = "fecha"),
        @Index(name = "idx_movimiento_financiero_tipo", columnList = "tipo"),
        @Index(name = "idx_movimiento_financiero_categoria", columnList = "categoria_id")
    }
)
public class MovimientoFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaMovimiento categoria;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    /**
     * Cuota que originó este movimiento de forma automática. Es null para los
     * movimientos cargados manualmente desde Finanzas.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id", unique = true)
    private Cuota cuota;

    public MovimientoFinanciero() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public CategoriaMovimiento getCategoria() { return categoria; }
    public void setCategoria(CategoriaMovimiento categoria) { this.categoria = categoria; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public Cuota getCuota() { return cuota; }
    public void setCuota(Cuota cuota) { this.cuota = cuota; }
}
