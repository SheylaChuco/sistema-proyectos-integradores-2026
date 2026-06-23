package pe.tecsup.proyectos.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.tecsup.proyectos.auth.dto.AuthResponse;
import pe.tecsup.proyectos.auth.dto.LoginRequest;
import pe.tecsup.proyectos.auth.dto.RegistroRequest;
import pe.tecsup.proyectos.auth.dto.RegistroResponse;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.auth.repository.UsuarioRepository;
import pe.tecsup.proyectos.config.JwtService;
import pe.tecsup.proyectos.shared.entity.Estudiante;
import pe.tecsup.proyectos.shared.entity.Usuario;
import pe.tecsup.proyectos.shared.exception.ApiException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegistroResponse registrar(RegistroRequest request) {
        if (!request.getCorreo().endsWith("@tecsup.edu.pe")) {
            throw new ApiException("CORREO_INVALIDO",
                    "El correo debe pertenecer al dominio @tecsup.edu.pe.",
                    HttpStatus.BAD_REQUEST);
        }

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new ApiException("CORREO_DUPLICADO",
                    "Ya existe una cuenta con este correo.",
                    HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Usuario.Rol.ESTUDIANTE);
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiante(request.getCodigoEstudiante());
        estudianteRepository.save(estudiante);

        return new RegistroResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                estudiante.getCodigoEstudiante()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByCorreoAndRolAndActivo(request.getCorreo(), Usuario.Rol.ESTUDIANTE, true)
                .orElseThrow(() -> new ApiException("CREDENCIALES_INCORRECTAS",
                        "Correo o contrasena incorrectos.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ApiException("CREDENCIALES_INCORRECTAS",
                    "Correo o contrasena incorrectos.", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generarToken(
                usuario.getId(), usuario.getCorreo(), usuario.getNombre());

        return new AuthResponse(
                token,
                new AuthResponse.UsuarioInfo(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getCorreo(),
                        usuario.getRol().name()
                )
        );
    }
}
