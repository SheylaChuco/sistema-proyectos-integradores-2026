package pe.tecsup.proyectos.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistroResponse {
    private Long id;
    private String nombre;
    private String correo;
    private String codigoEstudiante;
}
