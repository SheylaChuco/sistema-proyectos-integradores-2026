package pe.tecsup.proyectos.catalogo.dto;

import pe.tecsup.proyectos.shared.entity.GrupoIntegrante;

public record IntegranteDto(String nombre, String codigoEstudiante) {

    public static IntegranteDto from(GrupoIntegrante gi) {
        return new IntegranteDto(
                gi.getEstudiante().getUsuario().getNombre(),
                gi.getEstudiante().getCodigoEstudiante()
        );
    }
}
