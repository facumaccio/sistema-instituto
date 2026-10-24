package com.instituto.gestion.repository;

import com.instituto.gestion.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    @Query("SELECT COUNT(a) > 0 FROM Alumno a WHERE a.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);

    @Query("SELECT COUNT(a) > 0 FROM Alumno a WHERE a.dni = :dni AND a.id <> :id")
    boolean existsByDniAndIdNot(@Param("dni") String dni, @Param("id") Long id);

    /**
     * Alumno activo para reportes: tiene al menos una inscripción y no está
     * marcado como no regular. La regularidad nula se considera activa.
     */
    @Query("""
            SELECT COUNT(DISTINCT a.id)
            FROM Alumno a
            WHERE (a.esRegular IS NULL OR a.esRegular = true)
              AND EXISTS (SELECT i.id FROM Inscripcion i WHERE i.alumno = a)
            """)
    long contarAlumnosActivos();
}
