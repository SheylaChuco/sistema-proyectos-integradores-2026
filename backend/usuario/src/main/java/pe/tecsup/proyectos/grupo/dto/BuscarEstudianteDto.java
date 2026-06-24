package pe.tecsup.proyectos.grupo.dto;

import pe.tecsup.proyectos.shared.entity.Estudiante;

public record BuscarEstudianteDto(Long id, String nombre, String codigoEstudiante, boolean tieneGrupo) {

    public static BuscarEstudianteDto from(Estudiante e, boolean tieneGrupo) {
        return new BuscarEstudianteDto(
                e.getId(),
                e.getUsuario().getNombre(),
                e.getCodigoEstudiante(),
                tieneGrupo
        );
    }
}
