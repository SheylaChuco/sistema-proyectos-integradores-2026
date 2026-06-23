package pe.tecsup.proyectos.grupo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;

import java.util.Optional;

public interface GrupoIntegranteRepository extends JpaRepository<GrupoIntegrante, Long> {

    // Verifica si un estudiante ya pertenece a algun grupo en ese periodo
    boolean existsByEstudianteIdAndGrupoPeriodo(Long estudianteId, String periodo);

    // Obtiene el grupo mas reciente del estudiante para vincular su propuesta
    Optional<GrupoIntegrante> findFirstByEstudianteIdOrderByIdDesc(Long estudianteId);
}
