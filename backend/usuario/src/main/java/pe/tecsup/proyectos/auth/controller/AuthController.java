package pe.tecsup.proyectos.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.tecsup.proyectos.auth.dto.AuthResponse;
import pe.tecsup.proyectos.auth.dto.LoginRequest;
import pe.tecsup.proyectos.auth.dto.RegistroRequest;
import pe.tecsup.proyectos.auth.dto.RegistroResponse;
import pe.tecsup.proyectos.auth.service.AuthService;
import pe.tecsup.proyectos.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<RegistroResponse>> registro(
            @Valid @RequestBody RegistroRequest request) {
        RegistroResponse data = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
