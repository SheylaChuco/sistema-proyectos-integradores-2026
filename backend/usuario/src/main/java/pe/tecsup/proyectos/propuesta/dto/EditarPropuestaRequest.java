package pe.tecsup.proyectos.propuesta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarPropuestaRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 255)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria.")
    private String descripcion;
}
