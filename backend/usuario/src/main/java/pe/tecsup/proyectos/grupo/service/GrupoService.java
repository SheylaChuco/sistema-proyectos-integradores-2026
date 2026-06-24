package pe.tecsup.proyectos.grupo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.grupo.dto.BuscarEstudianteDto;
import pe.tecsup.proyectos.grupo.dto.CrearGrupoRequest;
import pe.tecsup.proyectos.grupo.dto.GrupoResponse;
import pe.tecsup.proyectos.grupo.dto.IntegranteGrupoDto;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoRepository;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoIntegranteRepository grupoIntegranteRepository;

    @Transactional(readOnly = true)
    public List<BuscarEstudianteDto> buscarEstudiantes(String nombre) {
        String periodo = detectarPeriodoActual();
        return estudianteRepository.buscarPorNombre(nombre).stream()
                .map(e -> {
                    boolean tieneGrupo = grupoIntegranteRepository
                            .existsByEstudianteIdAndGrupoPeriodo(e.getId(), periodo);
                    return BuscarEstudianteDto.from(e, tieneGrupo);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponse getMiGrupo(Long usuarioId) {
        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante no encontrado.", HttpStatus.NOT_FOUND));

        GrupoIntegrante miIntegrante = grupoIntegranteRepository
                .findFirstByEstudianteIdOrderByIdDesc(estudiante.getId())
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "No perteneces a ningún grupo.", HttpStatus.NOT_FOUND));

        Grupo grupo = miIntegrante.getGrupo();

        List<IntegranteGrupoDto> integrantes = grupo.getIntegrantes().stream()
                .map(gi -> IntegranteGrupoDto.from(
                        gi.getEstudiante(),
                        gi.getEstudiante().getId().equals(estudiante.getId())))
                .toList();

        return GrupoResponse.from(grupo, integrantes);
    }

    @Transactional
    public GrupoResponse crearGrupo(Long usuarioId, CrearGrupoRequest request) {
        Estudiante creador = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante autenticado no encontrado.", HttpStatus.NOT_FOUND));

        String codigoGrupo = generarCodigoGrupo();
        String periodo = detectarPeriodoActual();

        List<Estudiante> integrantes = new ArrayList<>();
        integrantes.add(creador);

        for (Long integranteId : request.getIntegranteIds()) {
            if (integranteId.equals(creador.getId())) continue;
            Estudiante companero = estudianteRepository.findById(integranteId)
                    .orElseThrow(() -> new ApiException(
                            "NO_ENCONTRADO",
                            "Estudiante con id " + integranteId + " no encontrado.",
                            HttpStatus.NOT_FOUND));
            integrantes.add(companero);
        }

        for (Estudiante e : integrantes) {
            if (grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(
                    e.getId(), periodo)) {
                throw new ApiException(
                        "ESTUDIANTE_YA_TIENE_GRUPO",
                        "El estudiante " + e.getUsuario().getNombre()
                                + " ya pertenece a un grupo en el periodo " + periodo + ".",
                        HttpStatus.CONFLICT);
            }
        }

        Grupo grupo = new Grupo();
        grupo.setCodigoGrupo(codigoGrupo);
        grupo.setPeriodo(periodo);
        Grupo grupoGuardado = grupoRepository.save(grupo);

        List<GrupoIntegrante> registros = integrantes.stream().map(e -> {
            GrupoIntegrante gi = new GrupoIntegrante();
            gi.setGrupo(grupoGuardado);
            gi.setEstudiante(e);
            return gi;
        }).toList();
        grupoIntegranteRepository.saveAll(registros);

        List<IntegranteGrupoDto> integranteDtos = integrantes.stream()
                .map(e -> IntegranteGrupoDto.from(e, e.getId().equals(creador.getId())))
                .toList();

        return GrupoResponse.from(grupoGuardado, integranteDtos);
    }

    private String detectarPeriodoActual() {
        LocalDate hoy = LocalDate.now();
        String semestre = hoy.getMonthValue() <= 6 ? "I" : "II";
        return hoy.getYear() + "-" + semestre;
    }

    private String generarCodigoGrupo() {
        return "GRP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
