package pe.tecsup.proyectos.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.tecsup.proyectos.shared.entity.Estudiante;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario u " +
           "WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "AND u.rol = 'ESTUDIANTE' AND u.activo = true " +
           "ORDER BY u.nombre")
    List<Estudiante> buscarPorNombre(@Param("nombre") String nombre);
}
