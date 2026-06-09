package com.studioedge;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class FocusToLevelupAdminApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory(findDotenvDirectory())
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(FocusToLevelupAdminApplication.class, args);
    }

    private static String findDotenvDirectory() {
        Path current = Path.of("").toAbsolutePath();

        while (current != null) {
            if (Files.exists(current.resolve(".env"))) {
                return current.toString();
            }
            current = current.getParent();
        }

        return Path.of("").toAbsolutePath().toString();
    }
}
