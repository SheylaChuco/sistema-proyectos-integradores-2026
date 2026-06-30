package pe.tecsup.proyectos.propuesta;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PropuestaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenEstudiante;

    @BeforeEach
    void configurar() throws Exception {
        // Registrar estudiante de prueba (si ya existe, el login igual funciona)
        String registro = objectMapper.writeValueAsString(Map.of(
                "nombre", "Propuesta Test",
                "correo", "propuesta.test@tecsup.edu.pe",
                "password", "Password123",
                "codigo_estudiante", "2024201"
        ));
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro));

        // Login para obtener token
        String login = objectMapper.writeValueAsString(Map.of(
                "correo", "propuesta.test@tecsup.edu.pe",
                "password", "Password123"
        ));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andReturn();

        String respuesta = result.getResponse().getContentAsString();
        tokenEstudiante = objectMapper.readTree(respuesta)
                .path("data").path("access_token").asText();
    }

    @Test
    void miPropuesta_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/propuestas/mi-propuesta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPropuesta_sinToken_retorna401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "App de prueba",
                "descripcion", "Descripcion de prueba."
        ));

        mockMvc.perform(post("/api/propuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPropuesta_conToken_sinGrupo_retorna403SinGrupo() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "App de prueba",
                "descripcion", "Descripcion de la propuesta de prueba."
        ));

        mockMvc.perform(post("/api/propuestas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenEstudiante)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SIN_GRUPO"));
    }

    @Test
    void miPropuesta_conToken_sinGrupo_retorna403SinGrupo() throws Exception {
        mockMvc.perform(get("/api/propuestas/mi-propuesta")
                        .header("Authorization", "Bearer " + tokenEstudiante))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SIN_GRUPO"));
    }
}
