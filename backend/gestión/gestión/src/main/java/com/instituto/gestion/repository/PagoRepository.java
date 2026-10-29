package com.instituto.gestion.repository;

import com.instituto.gestion.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByAlumno_Id(Long alumnoId);

    @Query("""
            SELECT MAX(p.fechaPago)
            FROM Pago p
            WHERE p.alumno.id = :alumnoId
              AND p.fechaPago IS NOT NULL
            """)
    LocalDate findUltimaFechaPagoByAlumnoId(@Param("alumnoId") Long alumnoId);
}
