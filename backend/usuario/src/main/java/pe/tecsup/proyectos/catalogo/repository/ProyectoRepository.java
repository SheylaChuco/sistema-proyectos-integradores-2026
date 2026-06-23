package pe.tecsup.proyectos.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.tecsup.proyectos.shared.entity.Proyecto;

import java.util.Optional;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {

    // Carga el proyecto con grupo + integrantes en una sola consulta para evitar N+1
    @Query("SELECT DISTINCT p FROM Proyecto p " +
           "LEFT JOIN FETCH p.grupo g " +
           "LEFT JOIN FETCH g.integrantes gi " +
           "LEFT JOIN FETCH gi.estudiante e " +
           "LEFT JOIN FETCH e.usuario " +
           "WHERE p.id = :id")
    Optional<Proyecto> findByIdWithIntegrantes(@Param("id") Long id);
}
