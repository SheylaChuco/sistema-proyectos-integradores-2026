package pe.tecsup.proyectos.grupo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.tecsup.proyectos.grupo.dto.BuscarEstudianteDto;
import pe.tecsup.proyectos.grupo.dto.CrearGrupoRequest;
import pe.tecsup.proyectos.grupo.dto.GrupoResponse;
import pe.tecsup.proyectos.grupo.service.GrupoService;
import pe.tecsup.proyectos.shared.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping("/api/estudiantes/buscar")
    public ResponseEntity<ApiResponse<List<BuscarEstudianteDto>>> buscar(
            @RequestParam @NotBlank @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres.")
            String nombre) {
        List<BuscarEstudianteDto> data = grupoService.buscarEstudiantes(nombre);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/api/grupos")
    public ResponseEntity<ApiResponse<GrupoResponse>> crear(
            @AuthenticationPrincipal String subject,
            @Valid @RequestBody CrearGrupoRequest request) {
        Long usuarioId = Long.parseLong(subject);
        GrupoResponse data = grupoService.crearGrupo(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }
}
