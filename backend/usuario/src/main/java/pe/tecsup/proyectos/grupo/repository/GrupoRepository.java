package pe.tecsup.proyectos.grupo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.tecsup.proyectos.shared.entity.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    boolean existsByCodigoGrupo(String codigoGrupo);
}
