package pe.tecsup.proyectos.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.tecsup.proyectos.shared.entity.Proyecto;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class ProyectoDetalleDto {

    private final Long id;
    private final String origen;
    private final String nombre;
    private final String descripcion;
    private final String ciclo;
    private final String codigoSeccion;
    // Solo presentes para origen=NUEVO:
    private final String codigoGrupo;
    private final List<IntegranteDto> integrantes;
    private final String estado;
    private final String url;
    private final String comentarioEvaluacion;

    public static ProyectoDetalleDto from(Proyecto p) {
        boolean esNuevo = p.getOrigen() == Proyecto.Origen.NUEVO;
        return new ProyectoDetalleDto(
                p.getId(),
                p.getOrigen().name(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCiclo(),
                p.getCodigoSeccion(),
                esNuevo && p.getGrupo() != null ? p.getGrupo().getCodigoGrupo() : null,
                esNuevo && p.getGrupo() != null
                        ? p.getGrupo().getIntegrantes().stream()
                                .map(IntegranteDto::from)
                                .collect(Collectors.toList())
                        : null,
                esNuevo && p.getEstado() != null ? p.getEstado().name() : null,
                esNuevo ? p.getUrl() : null,
                esNuevo ? p.getComentarioEvaluacion() : null
        );
    }
}
