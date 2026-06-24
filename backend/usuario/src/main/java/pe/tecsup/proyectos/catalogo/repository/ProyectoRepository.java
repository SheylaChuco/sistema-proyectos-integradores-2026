package pe.tecsup.proyectos.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.tecsup.proyectos.shared.entity.Proyecto;

import java.util.List;
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

    @Query("SELECT DISTINCT p FROM Proyecto p LEFT JOIN FETCH p.grupo")
    List<Proyecto> findAllWithGrupo();

    @Query("SELECT DISTINCT p.ciclo FROM Proyecto p ORDER BY p.ciclo DESC")
    List<String> findDistinctCiclos();

    @Query("SELECT DISTINCT p FROM Proyecto p " +
           "LEFT JOIN FETCH p.grupo g " +
           "LEFT JOIN FETCH g.integrantes gi " +
           "LEFT JOIN FETCH gi.estudiante e " +
           "LEFT JOIN FETCH e.usuario " +
           "WHERE g.id = :grupoId AND p.origen = :origen")
    Optional<Proyecto> findByGrupoIdAndOrigenWithIntegrantes(
            @Param("grupoId") Long grupoId,
            @Param("origen") Proyecto.Origen origen);
}
