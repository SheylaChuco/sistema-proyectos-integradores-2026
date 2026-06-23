package pe.tecsup.proyectos.propuesta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.tecsup.proyectos.shared.entity.PropuestaVersion;

public interface PropuestaVersionRepository extends JpaRepository<PropuestaVersion, Long> {
}
