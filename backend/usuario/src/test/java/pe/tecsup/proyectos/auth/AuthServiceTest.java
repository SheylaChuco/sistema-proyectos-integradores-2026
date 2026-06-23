package pe.tecsup.proyectos.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.tecsup.proyectos.auth.dto.LoginRequest;
import pe.tecsup.proyectos.auth.dto.RegistroRequest;
import pe.tecsup.proyectos.auth.repository.EstudianteRepository;
import pe.tecsup.proyectos.auth.repository.UsuarioRepository;
import pe.tecsup.proyectos.auth.service.AuthService;
import pe.tecsup.proyectos.config.JwtService;
import pe.tecsup.proyectos.shared.entity.Usuario;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegistroRequest buildRegistroRequest(String correo) {
        RegistroRequest req = new RegistroRequest();
        req.setNombre("Test User");
        req.setCorreo(correo);
        req.setPassword("password123");
        req.setCodigoEstudiante("2024001");
        return req;
    }

    @Test
    void registrar_correoInstitucional_creaUsuarioYEstudiante() {
        RegistroRequest req = buildRegistroRequest("test@tecsup.edu.pe");
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        Usuario saved = new Usuario();
        saved.setId(1L);
        saved.setNombre("Test User");
        saved.setCorreo("test@tecsup.edu.pe");
        saved.setRol(Usuario.Rol.ESTUDIANTE);
        when(usuarioRepository.save(any())).thenReturn(saved);

        var response = authService.registrar(req);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(estudianteRepository).save(any());
    }

    @Test
    void registrar_correoNoDominioTecsup_lanzaCorreoInvalido() {
        RegistroRequest req = buildRegistroRequest("test@gmail.com");

        ApiException ex = assertThrows(ApiException.class, () -> authService.registrar(req));
        assertEquals("CORREO_INVALIDO", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void registrar_correoDuplicado_lanzaCorreoDuplicado() {
        RegistroRequest req = buildRegistroRequest("duplicado@tecsup.edu.pe");
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> authService.registrar(req));
        assertEquals("CORREO_DUPLICADO", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void login_credencialesCorrectas_retornaToken() {
        LoginRequest req = new LoginRequest();
        req.setCorreo("test@tecsup.edu.pe");
        req.setPassword("password123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test");
        usuario.setCorreo("test@tecsup.edu.pe");
        usuario.setPassword("hash");
        usuario.setRol(Usuario.Rol.ESTUDIANTE);
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreoAndRolAndActivo(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generarToken(anyLong(), anyString(), anyString())).thenReturn("jwt-token");

        var response = authService.login(req);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("ESTUDIANTE", response.getUsuario().getRol());
    }

    @Test
    void login_usuarioNoExiste_lanzaCredencialesIncorrectas() {
        LoginRequest req = new LoginRequest();
        req.setCorreo("noexiste@tecsup.edu.pe");
        req.setPassword("password123");
        when(usuarioRepository.findByCorreoAndRolAndActivo(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(req));
        assertEquals("CREDENCIALES_INCORRECTAS", ex.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void login_passwordIncorrecto_lanzaCredencialesIncorrectas() {
        LoginRequest req = new LoginRequest();
        req.setCorreo("test@tecsup.edu.pe");
        req.setPassword("wrongpass");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPassword("hash");
        usuario.setRol(Usuario.Rol.ESTUDIANTE);
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreoAndRolAndActivo(anyString(), any(), anyBoolean()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(req));
        assertEquals("CREDENCIALES_INCORRECTAS", ex.getCode());
    }
}
