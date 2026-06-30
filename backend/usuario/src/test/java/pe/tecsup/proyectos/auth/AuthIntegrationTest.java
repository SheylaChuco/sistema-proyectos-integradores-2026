package pe.tecsup.proyectos.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registro_exitoso_retorna201ConDatos() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Ana Torres",
                "correo", "ana.torres.int@tecsup.edu.pe",
                "password", "Password123",
                "codigo_estudiante", "2024101"
        ));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.correo").value("ana.torres.int@tecsup.edu.pe"))
                .andExpect(jsonPath("$.data.nombre").value("Ana Torres"));
    }

    @Test
    void registro_correoNoInstitucional_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Test Gmail",
                "correo", "test@gmail.com",
                "password", "Password123",
                "codigo_estudiante", "2024102"
        ));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CORREO_INVALIDO"));
    }

    @Test
    void registro_correoRepetido_retorna409() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Maria Lopez",
                "correo", "maria.duplicada@tecsup.edu.pe",
                "password", "Password123",
                "codigo_estudiante", "2024103"
        ));

        // Primer registro — exitoso
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Segundo registro con el mismo correo — conflicto
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CORREO_DUPLICADO"));
    }

    @Test
    void login_exitoso_retornaTokenYDatosDeUsuario() throws Exception {
        // Registrar primero
        String registro = objectMapper.writeValueAsString(Map.of(
                "nombre", "Carlos Ruiz",
                "correo", "carlos.ruiz.int@tecsup.edu.pe",
                "password", "Password123",
                "codigo_estudiante", "2024104"
        ));
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated());

        // Luego hacer login
        String login = objectMapper.writeValueAsString(Map.of(
                "correo", "carlos.ruiz.int@tecsup.edu.pe",
                "password", "Password123"
        ));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.usuario.correo").value("carlos.ruiz.int@tecsup.edu.pe"))
                .andExpect(jsonPath("$.data.usuario.rol").value("ESTUDIANTE"));
    }

    @Test
    void login_credencialesIncorrectas_retorna401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "correo", "noexiste@tecsup.edu.pe",
                "password", "WrongPassword1"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CREDENCIALES_INCORRECTAS"));
    }
}
