package pe.tecsup.proyectos.grupo.dto;

import pe.tecsup.proyectos.shared.entity.Estudiante;

public record IntegranteGrupoDto(Long id, String nombre, String codigoEstudiante, boolean esLider) {

    public static IntegranteGrupoDto from(Estudiante e, boolean esLider) {
        return new IntegranteGrupoDto(
                e.getId(),
                e.getUsuario().getNombre(),
                e.getCodigoEstudiante(),
                esLider
        );
    }
}
