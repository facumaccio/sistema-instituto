package com.instituto.gestion.service;

import com.instituto.gestion.dto.*;
import com.instituto.gestion.model.CategoriaMovimiento;
import com.instituto.gestion.model.MovimientoFinanciero;
import com.instituto.gestion.model.TipoMovimiento;
import com.instituto.gestion.repository.CategoriaMovimientoRepository;
import com.instituto.gestion.repository.MovimientoFinancieroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinanzasService {

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("999999999999.99");
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(9999, 12, 31);

    @Autowired
    private MovimientoFinancieroRepository movimientoRepository;

    @Autowired
    private CategoriaMovimientoRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaMovimientoResponse> listarCategorias(TipoMovimiento tipo) {
        List<CategoriaMovimiento> categorias = tipo == null
                ? categoriaRepository.findAllByOrderByTipoAscNombreAsc()
                : categoriaRepository.findByTipoOrderByNombreAsc(tipo);

        return categorias.stream()
                .map(categoria -> new CategoriaMovimientoResponse(
                        categoria.getId(), categoria.getNombre(), categoria.getTipo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoFinancieroResponse> listarMovimientos(
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipo,
            Long categoriaId
    ) {
        validarRango(desde, hasta);
        validarCategoriaFiltro(categoriaId, tipo);

        LocalDate desdeSeguro = desde != null ? desde : FECHA_MINIMA;
        LocalDate hastaSeguro = hasta != null ? hasta : FECHA_MAXIMA;
        long categoriaSegura = categoriaId != null ? categoriaId : 0L;

        List<MovimientoFinanciero> movimientos = tipo == null
                ? movimientoRepository.buscarTodos(desdeSeguro, hastaSeguro, categoriaSegura)
                : movimientoRepository.buscarPorTipo(tipo, desdeSeguro, hastaSeguro, categoriaSegura);

        return movimientos.stream()
                .map(this::mapearMovimiento)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenFinancieroResponse obtenerResumen(
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipo,
            Long categoriaId
    ) {
        validarRango(desde, hasta);
        validarCategoriaFiltro(categoriaId, tipo);

        LocalDate desdeSeguro = desde != null ? desde : FECHA_MINIMA;
        LocalDate hastaSeguro = hasta != null ? hasta : FECHA_MAXIMA;
        long categoriaSegura = categoriaId != null ? categoriaId : 0L;

        BigDecimal totalIngresos = tipo == null || tipo == TipoMovimiento.INGRESO
                ? valorSeguro(movimientoRepository.sumarPorTipo(
                        TipoMovimiento.INGRESO, desdeSeguro, hastaSeguro, categoriaSegura))
                : valorSeguro(BigDecimal.ZERO);

        BigDecimal totalEgresos = tipo == null || tipo == TipoMovimiento.EGRESO
                ? valorSeguro(movimientoRepository.sumarPorTipo(
                        TipoMovimiento.EGRESO, desdeSeguro, hastaSeguro, categoriaSegura))
                : valorSeguro(BigDecimal.ZERO);

        List<CategoriaResumen> gastosPorCategoria = tipo == null || tipo == TipoMovimiento.EGRESO
                ? movimientoRepository.desglosePorTipo(
                        TipoMovimiento.EGRESO, desdeSeguro, hastaSeguro, categoriaSegura).stream()
                        .map(this::mapearCategoriaResumen)
                        .toList()
                : List.of();

        long cantidadMovimientos = tipo == null
                ? movimientoRepository.contarTodos(desdeSeguro, hastaSeguro, categoriaSegura)
                : movimientoRepository.contarPorTipo(tipo, desdeSeguro, hastaSeguro, categoriaSegura);

        return new ResumenFinancieroResponse(
                totalIngresos,
                totalEgresos,
                totalIngresos.subtract(totalEgresos),
                cantidadMovimientos,
                gastosPorCategoria
        );
    }

    @Transactional
    public MovimientoFinancieroResponse crearMovimiento(MovimientoFinancieroRequest request) {
        CategoriaMovimiento categoria = validarRequest(request);

        MovimientoFinanciero movimiento = new MovimientoFinanciero();
        aplicarDatos(movimiento, request, categoria);

        return mapearMovimiento(movimientoRepository.save(movimiento));
    }

    @Transactional
    public MovimientoFinancieroResponse actualizarMovimiento(Long id, MovimientoFinancieroRequest request) {
        MovimientoFinanciero movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El movimiento financiero no existe."));

        CategoriaMovimiento categoria = validarRequest(request);
        aplicarDatos(movimiento, request, categoria);

        return mapearMovimiento(movimientoRepository.save(movimiento));
    }

    @Transactional
    public void eliminarMovimiento(Long id) {
        MovimientoFinanciero movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El movimiento financiero no existe."));

        movimientoRepository.delete(movimiento);
    }

    private CategoriaMovimiento validarRequest(MovimientoFinancieroRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del movimiento son obligatorios.");
        }
        if (request.getTipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná el tipo de movimiento.");
        }
        if (request.getCategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná una categoría.");
        }
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor que cero.");
        }
        if (request.getMonto().compareTo(MONTO_MAXIMO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto supera el límite permitido.");
        }
        if (request.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indicá la fecha del movimiento.");
        }
        if (request.getMetodoPago() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccioná el método de pago.");
        }
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá una descripción o concepto.");
        }
        if (request.getDescripcion().trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción no puede superar los 255 caracteres.");
        }

        CategoriaMovimiento categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La categoría seleccionada no existe."));

        if (categoria.getTipo() != request.getTipo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoría seleccionada no corresponde al tipo de movimiento.");
        }

        return categoria;
    }

    private void aplicarDatos(
            MovimientoFinanciero movimiento,
            MovimientoFinancieroRequest request,
            CategoriaMovimiento categoria
    ) {
        movimiento.setTipo(request.getTipo());
        movimiento.setCategoria(categoria);
        movimiento.setMonto(request.getMonto().setScale(2, RoundingMode.HALF_UP));
        movimiento.setFecha(request.getFecha());
        movimiento.setDescripcion(request.getDescripcion().trim());
        movimiento.setMetodoPago(request.getMetodoPago());
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }

    private void validarCategoriaFiltro(Long categoriaId, TipoMovimiento tipo) {
        if (categoriaId == null) {
            return;
        }

        CategoriaMovimiento categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La categoría indicada no existe."));

        if (tipo != null && categoria.getTipo() != tipo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categoría indicada no corresponde al tipo de movimiento filtrado.");
        }
    }

    private MovimientoFinancieroResponse mapearMovimiento(MovimientoFinanciero movimiento) {
        CategoriaMovimiento categoria = movimiento.getCategoria();
        return new MovimientoFinancieroResponse(
                movimiento.getId(),
                movimiento.getTipo(),
                categoria.getId(),
                categoria.getNombre(),
                movimiento.getMonto(),
                movimiento.getFecha(),
                movimiento.getDescripcion(),
                movimiento.getMetodoPago()
        );
    }

    private CategoriaResumen mapearCategoriaResumen(Object[] fila) {
        Long categoriaId = fila[0] == null ? null : ((Number) fila[0]).longValue();
        String nombre = (String) fila[1];
        TipoMovimiento tipo = (TipoMovimiento) fila[2];
        BigDecimal total = valorSeguro(aBigDecimal(fila[3]));
        long cantidad = fila[4] == null ? 0L : ((Number) fila[4]).longValue();

        return new CategoriaResumen(categoriaId, nombre, tipo, total, cantidad);
    }

    private BigDecimal aBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }
        if (valor instanceof Number numero) {
            return BigDecimal.valueOf(numero.doubleValue());
        }
        return new BigDecimal(valor.toString());
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        BigDecimal resultado = valor == null ? BigDecimal.ZERO : valor;
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }
}
