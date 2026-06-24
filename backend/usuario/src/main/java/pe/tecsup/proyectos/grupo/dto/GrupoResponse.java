package pe.tecsup.proyectos.grupo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.tecsup.proyectos.shared.entity.Grupo;

import java.util.List;

@Getter
@AllArgsConstructor
public class GrupoResponse {

    private final Long id;
    private final String codigoGrupo;
    private final String periodo;
    private final List<IntegranteGrupoDto> integrantes;

    public static GrupoResponse from(Grupo grupo, List<IntegranteGrupoDto> integrantes) {
        return new GrupoResponse(
                grupo.getId(),
                grupo.getCodigoGrupo(),
                grupo.getPeriodo(),
                integrantes
        );
    }
}
