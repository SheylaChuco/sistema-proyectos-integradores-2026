package pe.tecsup.proyectos.grupo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearGrupoRequest {

    @NotBlank(message = "El código de grupo es obligatorio.")
    private String codigoGrupo;

    @NotBlank(message = "El periodo es obligatorio.")
    private String periodo;

    @NotNull(message = "La lista de compañeros es obligatoria.")
    private List<Long> companeroIds;
}
