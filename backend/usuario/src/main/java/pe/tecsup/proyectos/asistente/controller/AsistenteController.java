package pe.tecsup.proyectos.asistente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.tecsup.proyectos.asistente.dto.ChatRequest;
import pe.tecsup.proyectos.asistente.dto.ChatResponse;
import pe.tecsup.proyectos.asistente.service.AsistenteService;
import pe.tecsup.proyectos.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        String respuesta = asistenteService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(respuesta)));
    }
}
