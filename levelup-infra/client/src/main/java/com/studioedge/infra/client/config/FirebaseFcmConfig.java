package com.studioedge.infra.client.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseFcmConfig {

    @Value("${firebase.service-account-json}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))) {
                log.info("[FirebaseFcmConfig] FCM Firebase App already initialized");
                return;
            }

            log.info("[FirebaseFcmConfig] Initializing FCM Firebase App...");

            String processedJson = serviceAccountJson.replace("\\n", "\n");
            InputStream serviceAccountStream = new ByteArrayInputStream(processedJson.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FirebaseFcmConfig] FCM Firebase App initialized successfully");
        } catch (IOException e) {
            log.error("[FirebaseFcmConfig] Failed to initialize FCM Firebase App", e);
            throw new IllegalStateException("FCM Firebase 초기화 실패: " + e.getMessage(), e);
        }
    }
}
