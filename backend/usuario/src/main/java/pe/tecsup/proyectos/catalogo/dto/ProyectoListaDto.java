package pe.tecsup.proyectos.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.tecsup.proyectos.shared.entity.Proyecto;

@Getter
@AllArgsConstructor
public class ProyectoListaDto {

    private final Long id;
    private final String origen;
    private final String nombre;
    private final String ciclo;
    private final String codigoSeccion;
    private final String descripcion;
    private final String estado; // null para origen=HISTORICO

    public static ProyectoListaDto from(Proyecto p) {
        String estado = p.getOrigen() == Proyecto.Origen.NUEVO && p.getEstado() != null
                ? p.getEstado().name()
                : null;
        return new ProyectoListaDto(
                p.getId(),
                p.getOrigen().name(),
                p.getNombre(),
                p.getCiclo(),
                p.getCodigoSeccion(),
                p.getDescripcion(),
                estado
        );
    }
}
