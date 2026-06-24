package pe.tecsup.proyectos.catalogo.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.catalogo.dto.ProyectoDetalleDto;
import pe.tecsup.proyectos.catalogo.dto.ProyectoListaDto;
import pe.tecsup.proyectos.catalogo.repository.ProyectoRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;
import pe.tecsup.proyectos.shared.entity.Proyecto;
import pe.tecsup.proyectos.shared.exception.ApiException;
import pe.tecsup.proyectos.shared.response.PaginaDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final int MAX_PAGINA = 50;

    private final ProyectoRepository proyectoRepository;
    private final EstudianteRepository estudianteRepository;
    private final GrupoIntegranteRepository grupoIntegranteRepository;

    public List<String> listarCiclos() {
        return proyectoRepository.findDistinctCiclos();
    }

    public PaginaDto<ProyectoListaDto> listarProyectos(String ciclo, String origen, String busqueda, int page, int size) {
        int tamanio = Math.min(Math.max(size, 1), MAX_PAGINA);
        Specification<Proyecto> spec = construirSpec(ciclo, origen, busqueda);
        Page<Proyecto> pageResult = proyectoRepository.findAll(spec, PageRequest.of(page, tamanio));
        List<ProyectoListaDto> content = pageResult.getContent().stream()
                .map(ProyectoListaDto::from)
                .toList();
        return new PaginaDto<>(content, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    public ProyectoDetalleDto obtenerDetalle(Long id) {
        Proyecto proyecto = proyectoRepository.findByIdWithIntegrantes(id)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Proyecto no encontrado.", HttpStatus.NOT_FOUND));
        return ProyectoDetalleDto.from(proyecto);
    }

    public ProyectoDetalleDto getMiProyecto(Long usuarioId) {
        Grupo grupo = resolverGrupoDelEstudiante(usuarioId);
        Proyecto proyecto = proyectoRepository.findByGrupoIdAndOrigenWithIntegrantes(grupo.getId(), Proyecto.Origen.NUEVO)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Tu grupo aún no tiene un proyecto aprobado.", HttpStatus.NOT_FOUND));
        return ProyectoDetalleDto.from(proyecto);
    }

    public ProyectoDetalleDto actualizarUrl(Long usuarioId, Long proyectoId, String url) {
        Grupo grupo = resolverGrupoDelEstudiante(usuarioId);
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Proyecto no encontrado.", HttpStatus.NOT_FOUND));

        if (!proyecto.getGrupo().getId().equals(grupo.getId())) {
            throw new ApiException("FORBIDDEN", "No tienes permisos sobre este proyecto.", HttpStatus.FORBIDDEN);
        }
        if (proyecto.getEstado() != Proyecto.Estado.EN_DESARROLLO) {
            throw new ApiException("ESTADO_INVALIDO",
                    "Solo puedes registrar la URL mientras el proyecto esté en desarrollo.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        proyecto.setUrl(url);
        return ProyectoDetalleDto.from(proyectoRepository.save(proyecto));
    }

    private Grupo resolverGrupoDelEstudiante(Long usuarioId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante no encontrado.", HttpStatus.NOT_FOUND));
        GrupoIntegrante gi = grupoIntegranteRepository
                .findFirstByEstudianteIdOrderByIdDesc(estudiante.getId())
                .orElseThrow(() -> new ApiException(
                        "SIN_GRUPO", "No perteneces a ningún grupo.", HttpStatus.FORBIDDEN));
        return gi.getGrupo();
    }

    private Specification<Proyecto> construirSpec(String ciclo, String origen, String busqueda) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (ciclo != null && !ciclo.isBlank()) {
                predicados.add(cb.equal(root.get("ciclo"), ciclo));
            }

            if (busqueda != null && !busqueda.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + busqueda.toLowerCase() + "%"));
            }

            if (origen != null && !origen.isBlank()) {
                try {
                    Proyecto.Origen origenEnum = Proyecto.Origen.valueOf(origen.toUpperCase());
                    predicados.add(cb.equal(root.get("origen"), origenEnum));
                } catch (IllegalArgumentException e) {
                    predicados.add(cb.disjunction());
                }
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
