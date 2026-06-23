package pe.tecsup.proyectos;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ProyectosApplication {

    public static void main(String[] args) {
        cargarEnv();
        SpringApplication app = new SpringApplication(ProyectosApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }

    private static void cargarEnv() {
        // Busca .env en el directorio del proyecto raiz
        String[] candidatos = {"../../.env", "../.env", ".env"};
        for (String candidato : candidatos) {
            Path ruta = Paths.get(candidato).toAbsolutePath().normalize();
            if (ruta.toFile().exists()) {
                Dotenv.configure()
                        .directory(ruta.getParent().toString())
                        .filename(".env")
                        .ignoreIfMissing()
                        .load()
                        .entries()
                        .forEach(e -> System.setProperty(e.getKey(), e.getValue()));
                return;
            }
        }
    }
}
