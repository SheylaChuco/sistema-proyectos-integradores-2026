package pe.tecsup.proyectos.asistente.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.tecsup.proyectos.asistente.dto.ChatRequest;
import pe.tecsup.proyectos.asistente.dto.MensajeDto;
import pe.tecsup.proyectos.catalogo.repository.ProyectoRepository;
import pe.tecsup.proyectos.config.LlmProperties;
import pe.tecsup.proyectos.shared.entity.Proyecto;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AsistenteService {

    private final ProyectoRepository proyectoRepository;
    private final ObjectMapper objectMapper;
    private final LlmProperties llm;

    private static final int MAX_DESC_CHARS = 150;
    private static final String SYSTEM_PROMPT_BASE = """
            Eres el Asistente IA de Integratec, el sistema de gestión de proyectos integradores de TECSUP.
            Ayudas a estudiantes a consultar información sobre proyectos académicos: encontrar proyectos similares,
            explorar temas ya desarrollados y sugerir ideas para nuevos proyectos.

            REGLAS CRÍTICAS:
            - Los proyectos HISTORICOS solo tienen: nombre, descripción, ciclo y código de sección.
              NO tienen estado, integrantes ni URL. Nunca inventes ni menciones esos campos para históricos.
            - Los proyectos NUEVOS pueden tener todos los campos: nombre, descripción, ciclo,
              grupo, estado (EN_DESARROLLO / APROBADO / NO_APROBADO) y URL (si fue registrada).
            - Responde siempre en español, de forma clara y amigable.
            - Si no tienes información suficiente para responder, dilo honestamente.
            """;

    public String chat(ChatRequest request) {
        String contexto = construirContexto();
        String systemPrompt = SYSTEM_PROMPT_BASE + "\n\n" + contexto;

        List<Map<String, String>> mensajes = new ArrayList<>();
        for (MensajeDto m : request.getHistorial()) {
            mensajes.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }
        mensajes.add(Map.of("role", "user", "content", request.getMensaje()));

        Map<String, Object> body = Map.of(
                "model", llm.getModel(),
                "max_tokens", 1024,
                "system", systemPrompt,
                "messages", mensajes
        );

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(llm.getApi().getUrl())
                    .defaultHeader("x-api-key", llm.getApi().getKey())
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .build();

            String responseJson = client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            throw new ApiException(
                    "ERROR_LLM",
                    "El asistente no está disponible en este momento. Intenta nuevamente.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String construirContexto() {
        List<Proyecto> proyectos = proyectoRepository.findAll();

        StringBuilder historicos = new StringBuilder("PROYECTOS HISTÓRICOS:\n");
        StringBuilder nuevos = new StringBuilder("\nPROYECTOS NUEVOS:\n");
        boolean hayNuevos = false;

        for (Proyecto p : proyectos) {
            String desc = p.getDescripcion() != null && p.getDescripcion().length() > MAX_DESC_CHARS
                    ? p.getDescripcion().substring(0, MAX_DESC_CHARS) + "..."
                    : p.getDescripcion();

            if (p.getOrigen() == Proyecto.Origen.HISTORICO) {
                historicos.append(String.format("- [%s|%s] %s: %s%n",
                        p.getCiclo(),
                        p.getCodigoSeccion() != null ? p.getCodigoSeccion() : "—",
                        p.getNombre(),
                        desc));
            } else {
                hayNuevos = true;
                String estado = p.getEstado() != null ? p.getEstado().name() : "—";
                String url = p.getUrl() != null ? p.getUrl() : "no registrada";
                String grupo = p.getGrupo() != null ? p.getGrupo().getCodigoGrupo() : "—";
                nuevos.append(String.format("- [%s|%s] %s: %s | Estado: %s | URL: %s%n",
                        p.getCiclo(), grupo, p.getNombre(), desc, estado, url));
            }
        }

        return historicos + (hayNuevos ? nuevos.toString() : "");
    }
}
