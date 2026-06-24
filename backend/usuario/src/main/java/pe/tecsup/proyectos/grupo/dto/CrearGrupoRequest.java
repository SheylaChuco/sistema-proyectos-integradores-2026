package pe.tecsup.proyectos.grupo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearGrupoRequest {

    @NotEmpty(message = "Debes agregar al menos un compañero.")
    private List<Long> integranteIds;
}
