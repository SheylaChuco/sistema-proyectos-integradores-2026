package pe.tecsup.proyectos.propuesta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.tecsup.proyectos.shared.entity.Propuesta;

import java.util.Optional;

public interface PropuestaRepository extends JpaRepository<Propuesta, Long> {

    @Query("SELECT p FROM Propuesta p JOIN FETCH p.grupo WHERE p.grupo.id = :grupoId")
    Optional<Propuesta> findByGrupoId(@Param("grupoId") Long grupoId);
}
