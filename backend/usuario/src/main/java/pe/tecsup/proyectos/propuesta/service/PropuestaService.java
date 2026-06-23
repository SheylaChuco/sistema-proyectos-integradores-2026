package pe.tecsup.proyectos.propuesta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.propuesta.dto.CrearPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.EditarPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.PropuestaResponse;
import pe.tecsup.proyectos.propuesta.repository.PropuestaRepository;
import pe.tecsup.proyectos.propuesta.repository.PropuestaVersionRepository;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;
import pe.tecsup.proyectos.shared.entity.Propuesta;
import pe.tecsup.proyectos.shared.entity.PropuestaVersion;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PropuestaService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoIntegranteRepository grupoIntegranteRepository;
    private final PropuestaRepository propuestaRepository;
    private final PropuestaVersionRepository propuestaVersionRepository;

    @Transactional
    public PropuestaResponse crearPropuesta(Long usuarioId, CrearPropuestaRequest request) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante no encontrado.", HttpStatus.NOT_FOUND));

        GrupoIntegrante gi = grupoIntegranteRepository
                .findFirstByEstudianteIdOrderByIdDesc(estudiante.getId())
                .orElseThrow(() -> new ApiException(
                        "SIN_GRUPO",
                        "No perteneces a ningún grupo. Registra tu grupo antes de proponer un proyecto.",
                        HttpStatus.FORBIDDEN));

        Grupo grupo = gi.getGrupo();

        propuestaRepository.findByGrupoId(grupo.getId()).ifPresent(p -> {
            if (p.getEstado() == Propuesta.Estado.PENDIENTE
                    || p.getEstado() == Propuesta.Estado.APROBADO) {
                throw new ApiException(
                        "PROPUESTA_ACTIVA",
                        "El grupo ya tiene una propuesta pendiente o aprobada.",
                        HttpStatus.CONFLICT);
            }
        });

        Propuesta propuesta = new Propuesta();
        propuesta.setGrupo(grupo);
        propuesta.setNombre(request.getNombre());
        propuesta.setDescripcion(request.getDescripcion());
        propuesta.setEstado(Propuesta.Estado.PENDIENTE);
        propuesta.setFechaEnvio(OffsetDateTime.now());
        propuesta = propuestaRepository.save(propuesta);

        return PropuestaResponse.from(propuesta);
    }

    @Transactional
    public PropuestaResponse editarPropuesta(Long usuarioId, Long propuestaId, EditarPropuestaRequest request) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante no encontrado.", HttpStatus.NOT_FOUND));

        GrupoIntegrante gi = grupoIntegranteRepository
                .findFirstByEstudianteIdOrderByIdDesc(estudiante.getId())
                .orElseThrow(() -> new ApiException(
                        "SIN_GRUPO", "No perteneces a ningún grupo.", HttpStatus.FORBIDDEN));

        Propuesta propuesta = propuestaRepository.findByGrupoId(gi.getGrupo().getId())
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "No tienes una propuesta registrada.", HttpStatus.NOT_FOUND));

        if (!propuesta.getId().equals(propuestaId)) {
            throw new ApiException("NO_ENCONTRADO", "Propuesta no encontrada.", HttpStatus.NOT_FOUND);
        }

        if (propuesta.getEstado() != Propuesta.Estado.OBSERVADO) {
            throw new ApiException(
                    "ESTADO_INVALIDO",
                    "Solo puedes editar una propuesta que está en estado OBSERVADO.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        PropuestaVersion version = new PropuestaVersion();
        version.setPropuesta(propuesta);
        version.setNombre(propuesta.getNombre());
        version.setDescripcion(propuesta.getDescripcion());
        version.setVersionFecha(OffsetDateTime.now());
        propuestaVersionRepository.save(version);

        propuesta.setNombre(request.getNombre());
        propuesta.setDescripcion(request.getDescripcion());
        propuesta.setEstado(Propuesta.Estado.PENDIENTE);
        propuesta = propuestaRepository.save(propuesta);

        return PropuestaResponse.from(propuesta);
    }

    @Transactional(readOnly = true)
    public PropuestaResponse getMiPropuesta(Long usuarioId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante no encontrado.", HttpStatus.NOT_FOUND));

        GrupoIntegrante gi = grupoIntegranteRepository
                .findFirstByEstudianteIdOrderByIdDesc(estudiante.getId())
                .orElseThrow(() -> new ApiException(
                        "SIN_GRUPO", "No perteneces a ningún grupo.", HttpStatus.FORBIDDEN));

        Propuesta propuesta = propuestaRepository.findByGrupoId(gi.getGrupo().getId())
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "No tienes una propuesta registrada.", HttpStatus.NOT_FOUND));

        return PropuestaResponse.from(propuesta);
    }
}
