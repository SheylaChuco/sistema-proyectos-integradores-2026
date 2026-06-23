package pe.tecsup.proyectos.propuesta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.tecsup.proyectos.propuesta.dto.CrearPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.EditarPropuestaRequest;
import pe.tecsup.proyectos.propuesta.dto.PropuestaResponse;
import pe.tecsup.proyectos.propuesta.service.PropuestaService;
import pe.tecsup.proyectos.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/propuestas")
@RequiredArgsConstructor
public class PropuestaController {

    private final PropuestaService propuestaService;

    @PostMapping
    public ResponseEntity<ApiResponse<PropuestaResponse>> crear(
            @AuthenticationPrincipal String subject,
            @Valid @RequestBody CrearPropuestaRequest request) {
        Long usuarioId = Long.parseLong(subject);
        PropuestaResponse data = propuestaService.crearPropuesta(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PropuestaResponse>> editar(
            @AuthenticationPrincipal String subject,
            @PathVariable Long id,
            @Valid @RequestBody EditarPropuestaRequest request) {
        Long usuarioId = Long.parseLong(subject);
        PropuestaResponse data = propuestaService.editarPropuesta(usuarioId, id, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/mia")
    public ResponseEntity<ApiResponse<PropuestaResponse>> mia(
            @AuthenticationPrincipal String subject) {
        Long usuarioId = Long.parseLong(subject);
        PropuestaResponse data = propuestaService.getMiPropuesta(usuarioId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
