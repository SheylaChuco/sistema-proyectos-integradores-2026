package pe.tecsup.proyectos.grupo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.grupo.dto.BuscarEstudianteDto;
import pe.tecsup.proyectos.grupo.dto.CrearGrupoRequest;
import pe.tecsup.proyectos.grupo.dto.GrupoResponse;
import pe.tecsup.proyectos.grupo.repository.GrupoIntegranteRepository;
import pe.tecsup.proyectos.grupo.repository.GrupoRepository;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Grupo;
import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoIntegranteRepository grupoIntegranteRepository;

    @Transactional(readOnly = true)
    public List<BuscarEstudianteDto> buscarEstudiantes(String nombre) {
        return estudianteRepository.buscarPorNombre(nombre).stream()
                .map(BuscarEstudianteDto::from)
                .toList();
    }

    @Transactional
    public GrupoResponse crearGrupo(Long usuarioId, CrearGrupoRequest request) {
        // Obtener el estudiante autenticado (creador del grupo)
        Estudiante creador = estudianteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ApiException(
                        "NO_ENCONTRADO", "Estudiante autenticado no encontrado.", HttpStatus.NOT_FOUND));

        // Validar unicidad del codigo_grupo
        if (grupoRepository.existsByCodigoGrupo(request.getCodigoGrupo())) {
            throw new ApiException(
                    "CODIGO_GRUPO_DUPLICADO", "El código de grupo ya está en uso.", HttpStatus.CONFLICT);
        }

        // Construir lista de integrantes (creador primero, sin duplicados)
        List<Estudiante> integrantes = new ArrayList<>();
        integrantes.add(creador);

        for (Long companeroId : request.getCompaneroIds()) {
            if (companeroId.equals(creador.getId())) continue; // ya está incluido
            Estudiante companero = estudianteRepository.findById(companeroId)
                    .orElseThrow(() -> new ApiException(
                            "NO_ENCONTRADO",
                            "Estudiante con id " + companeroId + " no encontrado.",
                            HttpStatus.NOT_FOUND));
            integrantes.add(companero);
        }

        // Validar que ningún integrante ya tenga grupo en el mismo periodo
        for (Estudiante e : integrantes) {
            if (grupoIntegranteRepository.existsByEstudianteIdAndGrupoPeriodo(
                    e.getId(), request.getPeriodo())) {
                throw new ApiException(
                        "ESTUDIANTE_YA_TIENE_GRUPO",
                        "El estudiante " + e.getUsuario().getNombre()
                                + " ya pertenece a un grupo en el periodo " + request.getPeriodo() + ".",
                        HttpStatus.CONFLICT);
            }
        }

        // Crear el grupo
        Grupo grupo = new Grupo();
        grupo.setCodigoGrupo(request.getCodigoGrupo());
        grupo.setPeriodo(request.getPeriodo());
        Grupo grupoGuardado = grupoRepository.save(grupo);

        // Crear los registros de integrantes
        List<GrupoIntegrante> registros = integrantes.stream().map(e -> {
            GrupoIntegrante gi = new GrupoIntegrante();
            gi.setGrupo(grupoGuardado);
            gi.setEstudiante(e);
            return gi;
        }).toList();
        grupoIntegranteRepository.saveAll(registros);

        return GrupoResponse.from(grupoGuardado, integrantes);
    }
}
