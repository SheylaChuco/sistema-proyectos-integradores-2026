package pe.tecsup.proyectos.grupo.dto;

import pe.tecsup.proyectos.shared.entity.Estudiante;

public record BuscarEstudianteDto(Long id, String nombre, String codigoEstudiante) {

    public static BuscarEstudianteDto from(Estudiante e) {
        return new BuscarEstudianteDto(
                e.getId(),
                e.getUsuario().getNombre(),
                e.getCodigoEstudiante()
        );
    }
}
