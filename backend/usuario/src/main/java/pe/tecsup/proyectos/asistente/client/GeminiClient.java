package pe.tecsup.proyectos.asistente.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pe.tecsup.proyectos.config.LlmProperties;
import pe.tecsup.proyectos.shared.exception.ApiException;

import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final ObjectMapper objectMapper;
    private final LlmProperties llm;
    private final RestClient restClient;

    public GeminiClient(ObjectMapper objectMapper, LlmProperties llm) {
        this.objectMapper = objectMapper;
        this.llm = llm;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Llama a la API de Gemini con reintentos automáticos.
     * Reintenta hasta 3 veces en errores transitorios: saturación (429),
     * errores de servidor (5xx) y problemas de red.
     * Backoff exponencial con jitter para no saturar más la API.
     */
    @Retryable(
            retryFor = {
                HttpServerErrorException.class,
                HttpClientErrorException.TooManyRequests.class,
                ResourceAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000, random = true)
    )
    public String llamar(Map<String, Object> body) {
        String url = llm.getApi().getUrl() + "/" + llm.getModel()
                + ":generateContent?key=" + llm.getApi().getKey();

        String responseJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Respuesta inesperada de Gemini: {}", responseJson);
            throw new RuntimeException("Respuesta inesperada del LLM", e);
        }
    }

    // Llamado automáticamente cuando se agotan todos los reintentos
    @Recover
    public String recuperar(Exception e, Map<String, Object> body) {
        log.error("Reintentos agotados — {}: {}", e.getClass().getSimpleName(), e.getMessage());
        throw new ApiException(
                "ERROR_LLM",
                "El asistente no está disponible en este momento. Intenta en unos segundos.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
