package com.instituto.gestion.repository;

import com.instituto.gestion.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

    @Query("SELECT COUNT(p) > 0 FROM Profesor p WHERE p.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);

    @Query("SELECT COUNT(p) > 0 FROM Profesor p WHERE p.dni = :dni AND p.id <> :id")
    boolean existsByDniAndIdNot(@Param("dni") String dni, @Param("id") Long id);

    @Query("SELECT p FROM Profesor p WHERE p.dni = :dni")
    Optional<Profesor> findByDni(@Param("dni") String dni);
}
