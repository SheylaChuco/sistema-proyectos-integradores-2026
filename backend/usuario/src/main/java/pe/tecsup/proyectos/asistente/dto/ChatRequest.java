package pe.tecsup.proyectos.asistente.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {

    @NotBlank(message = "El mensaje no puede estar vacío.")
    private String mensaje;

    private List<MensajeDto> historial = List.of();
}
