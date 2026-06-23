package pe.tecsup.proyectos.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.tecsup.proyectos.shared.entity.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    boolean existsByCodigoEstudiante(String codigoEstudiante);
}
