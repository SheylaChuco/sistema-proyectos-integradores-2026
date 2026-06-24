package pe.tecsup.proyectos.asistente.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.tecsup.proyectos.asistente.client.GeminiClient;
import pe.tecsup.proyectos.asistente.dto.ChatRequest;
import pe.tecsup.proyectos.asistente.dto.MensajeDto;
import pe.tecsup.proyectos.catalogo.repository.ProyectoRepository;
import pe.tecsup.proyectos.shared.entity.Proyecto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AsistenteService {

    private final ProyectoRepository proyectoRepository;
    private final GeminiClient geminiClient;

    private static final int MAX_DESC_CHARS = 150;
    private static final String SYSTEM_PROMPT_BASE = """
            Eres el Asistente IA de Integratec, el sistema de gestión de proyectos integradores de TECSUP.
            Ayudas a estudiantes a consultar proyectos académicos: encontrar proyectos similares,
            explorar temas ya desarrollados y sugerir ideas para nuevos proyectos.

            REGLAS DE CONTENIDO:
            - Los proyectos HISTORICOS solo tienen: nombre, descripción, ciclo y código de sección.
              NO tienen estado, integrantes ni URL. Nunca inventes ni menciones esos campos para históricos.
            - Los proyectos NUEVOS pueden tener todos los campos: nombre, descripción, ciclo,
              grupo, estado (EN_DESARROLLO / APROBADO / NO_APROBADO) y URL (si fue registrada).
            - Responde siempre en español.
            - Si no tienes información suficiente para responder, dilo honestamente.

            REGLAS DE FORMATO (obligatorio):
            - Sé conciso. Máximo 5 proyectos por respuesta salvo que el usuario pida más.
            - Usa listas con guion (-) cuando enumeres proyectos o puntos.
            - Usa **negrita** solo para el nombre del proyecto.
            - No uses encabezados (#) a menos que la respuesta tenga secciones claramente distintas.
            - No repitas la pregunta del usuario ni hagas introducciones largas.
            - Termina con una línea corta de cierre o pregunta de seguimiento cuando tenga sentido.
            """;

    public String chat(ChatRequest request) {
        String systemPrompt = SYSTEM_PROMPT_BASE + "\n\n" + construirContexto();

        // Gemini usa "model" donde Anthropic usaba "assistant"
        List<Map<String, Object>> contents = new ArrayList<>();
        for (MensajeDto m : request.getHistorial()) {
            String role = "assistant".equals(m.getRole()) ? "model" : m.getRole();
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", m.getContent()))
            ));
        }
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", request.getMensaje()))
        ));

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents
        );

        return geminiClient.llamar(body);
    }

    private String construirContexto() {
        List<Proyecto> proyectos = proyectoRepository.findAllWithGrupo();

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
