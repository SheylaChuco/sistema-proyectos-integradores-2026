package pe.tecsup.proyectos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
@Getter
@Setter
public class LlmProperties {

    private Api api = new Api();
    private String model;

    @Getter
    @Setter
    public static class Api {
        private String key;
        private String url;
    }
}
