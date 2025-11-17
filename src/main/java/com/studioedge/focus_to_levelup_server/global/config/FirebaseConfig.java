package com.studioedge.focus_to_levelup_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK 설정
 * 사전예약 보상 시스템을 위한 Firebase Firestore 연동
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-json}")
    private String serviceAccountJson;

    /**
     * Firebase 초기화
     * 환경변수에서 Service Account JSON 로드
     */
    @PostConstruct
    public void initialize() {
        try {
            // 이미 초기화된 경우 스킵
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("[FirebaseConfig] Firebase already initialized");
                return;
            }

            // 디버그: JSON 길이 확인
            log.info("[FirebaseConfig] Service Account JSON length: {} characters", serviceAccountJson.length());
            log.info("[FirebaseConfig] JSON starts with: {}", serviceAccountJson.substring(0, Math.min(100, serviceAccountJson.length())));

            // JSON 문자열에서 \n을 실제 개행 문자로 변환
            String processedJson = serviceAccountJson.replace("\\n", "\n");
            log.info("[FirebaseConfig] After replacing \\n, JSON starts with: {}", processedJson.substring(0, Math.min(100, processedJson.length())));

            InputStream serviceAccountStream = new ByteArrayInputStream(processedJson.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FirebaseConfig] Firebase initialized successfully (Firestore mode)");

        } catch (IOException e) {
            log.error("[FirebaseConfig] Failed to initialize Firebase", e);
            throw new IllegalStateException("Firebase 초기화 실패: " + e.getMessage(), e);
        }
    }
}
