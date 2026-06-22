package com.studioedge.infra.client.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebasePreRegistrationConfig {

    public static final String PRE_REGISTRATION_APP_NAME = "pre-registration";

    @Value("${firebase.pre-registration.service-account-json:#{null}}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() {
        if (serviceAccountJson == null || serviceAccountJson.isEmpty()) {
            log.warn("[FirebasePreRegistrationConfig] Pre-registration Firebase config not found, skipping...");
            return;
        }

        try {
            if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(PRE_REGISTRATION_APP_NAME))) {
                log.info("[FirebasePreRegistrationConfig] Pre-registration Firebase App already initialized");
                return;
            }

            log.info("[FirebasePreRegistrationConfig] Initializing Pre-registration Firebase App...");

            String processedJson = serviceAccountJson.replace("\\n", "\n");
            InputStream serviceAccountStream = new ByteArrayInputStream(processedJson.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options, PRE_REGISTRATION_APP_NAME);
            log.info("[FirebasePreRegistrationConfig] Pre-registration Firebase App initialized successfully");
        } catch (IOException e) {
            log.error("[FirebasePreRegistrationConfig] Failed to initialize Pre-registration Firebase App", e);
            log.warn("[FirebasePreRegistrationConfig] Pre-registration feature will be disabled");
        }
    }
}
