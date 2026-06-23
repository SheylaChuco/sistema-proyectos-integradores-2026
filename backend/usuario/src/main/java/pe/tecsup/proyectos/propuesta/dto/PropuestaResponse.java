package pe.tecsup.proyectos.propuesta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.tecsup.proyectos.shared.entity.Propuesta;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class PropuestaResponse {

    private final Long id;
    private final Long grupoId;
    private final String codigoGrupo;
    private final String nombre;
    private final String descripcion;
    private final String estado;
    private final String comentarioObservacion;
    private final OffsetDateTime fechaEnvio;

    public static PropuestaResponse from(Propuesta p) {
        return new PropuestaResponse(
                p.getId(),
                p.getGrupo() != null ? p.getGrupo().getId() : null,
                p.getGrupo() != null ? p.getGrupo().getCodigoGrupo() : null,
                p.getNombre(),
                p.getDescripcion(),
                p.getEstado().name(),
                p.getComentarioObservacion(),
                p.getFechaEnvio()
        );
    }
}
