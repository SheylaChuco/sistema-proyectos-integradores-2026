package pe.tecsup.proyectos.propuesta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearPropuestaRequest {

    @NotBlank(message = "El nombre del proyecto es obligatorio.")
    private String nombre;

    @NotBlank(message = "La descripción del proyecto es obligatoria.")
    private String descripcion;
}
