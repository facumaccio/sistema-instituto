package com.instituto.gestion.service;

import com.instituto.gestion.model.Curso;
import com.instituto.gestion.model.MaterialCurso;
import com.instituto.gestion.repository.CursoRepository;
import com.instituto.gestion.repository.MaterialCursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class MaterialCursoService {

    private static final int MAX_TITULO = 160;
    private static final int MAX_DESCRIPCION = 1000;
    private static final int MAX_UNIDAD = 120;
    private static final int MAX_URL = 1000;

    @Autowired
    private MaterialCursoRepository repository;

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public List<MaterialCurso> listarPorCurso(Long cursoId) {
        validarCurso(cursoId);
        return repository.findByCurso_IdOrderByUnidadOClaseAscFechaPublicacionDesc(cursoId);
    }

    @Transactional(readOnly = true)
    public List<MaterialCurso> listarTodos() {
        return repository.findAllByOrderByFechaPublicacionDesc();
    }

    @Transactional(readOnly = true)
    public MaterialCurso obtener(Long id) {
        return buscarMaterial(id);
    }

    @Transactional
    public MaterialCurso crear(Long cursoId, MaterialCurso material) {
        Curso curso = validarCurso(cursoId);

        validarYNormalizar(material);
        material.setId(null);
        material.setCurso(curso);

        if (material.getFechaPublicacion() == null) {
            material.setFechaPublicacion(LocalDate.now());
        }

        return repository.save(material);
    }

    @Transactional
    public MaterialCurso actualizar(Long id, MaterialCurso materialActualizado) {
        MaterialCurso material = buscarMaterial(id);

        validarYNormalizar(materialActualizado);

        material.setTitulo(materialActualizado.getTitulo());
        material.setDescripcion(materialActualizado.getDescripcion());
        material.setUnidadOClase(materialActualizado.getUnidadOClase());
        material.setTipo(materialActualizado.getTipo());
        material.setUrl(materialActualizado.getUrl());

        if (materialActualizado.getFechaPublicacion() != null) {
            material.setFechaPublicacion(materialActualizado.getFechaPublicacion());
        }

        return repository.save(material);
    }

    @Transactional
    public void eliminar(Long id) {
        MaterialCurso material = buscarMaterial(id);
        repository.delete(material);
    }

    private Curso validarCurso(Long cursoId) {
        if (cursoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El curso es obligatorio para asociar el material.");
        }

        return cursoRepository.findById(cursoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el curso con ID " + cursoId + "."));
    }

    private MaterialCurso buscarMaterial(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el material con ID " + id + "."));
    }

    private void validarYNormalizar(MaterialCurso material) {
        if (material == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del material son obligatorios.");
        }

        if (material.getTitulo() == null || material.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El título del material es obligatorio.");
        }

        String titulo = material.getTitulo().trim();
        if (titulo.length() > MAX_TITULO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El título del material no puede superar los " + MAX_TITULO + " caracteres.");
        }
        material.setTitulo(titulo);

        if (material.getTipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seleccioná un tipo de material.");
        }

        if (material.getUrl() == null || material.getUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La URL del material es obligatoria.");
        }

        String url = material.getUrl().trim();
        if (url.length() > MAX_URL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La URL no puede superar los " + MAX_URL + " caracteres.");
        }

        String urlMinuscula = url.toLowerCase(Locale.ROOT);
        if (!urlMinuscula.startsWith("http://") && !urlMinuscula.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La URL debe comenzar con http:// o https://.");
        }
        material.setUrl(url);

        material.setDescripcion(normalizarTexto(material.getDescripcion(), MAX_DESCRIPCION,
                "La descripción del material no puede superar los " + MAX_DESCRIPCION + " caracteres."));

        material.setUnidadOClase(normalizarTexto(material.getUnidadOClase(), MAX_UNIDAD,
                "La unidad o clase no puede superar los " + MAX_UNIDAD + " caracteres."));
    }

    private String normalizarTexto(String valor, int maximo, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeError);
        }

        return limpio;
    }
}
