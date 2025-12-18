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
 * 두 개의 Firebase 프로젝트 관리:
 * 1. FCM용 (concenludev/concenluprod) - 기본 앱
 * 2. 사전예약 DB용 (concenlu-936e4) - "pre-registration" 이름의 앱
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    public static final String PRE_REGISTRATION_APP_NAME = "pre-registration";

    @Value("${firebase.service-account-json}")
    private String fcmServiceAccountJson;

    @Value("${firebase.pre-registration.service-account-json:#{null}}")
    private String preRegistrationServiceAccountJson;

    /**
     * Firebase 초기화
     * 환경변수에서 Service Account JSON 로드
     */
    @PostConstruct
    public void initialize() {
        // 1. FCM용 Firebase 초기화 (기본 앱)
        initializeFcmApp();

        // 2. 사전예약 DB용 Firebase 초기화 (별도 앱)
        initializePreRegistrationApp();
    }

    /**
     * FCM용 Firebase 앱 초기화 (기본 앱)
     */
    private void initializeFcmApp() {
        try {
            // 이미 기본 앱이 초기화된 경우 스킵
            if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))) {
                log.info("[FirebaseConfig] FCM Firebase App already initialized");
                return;
            }

            log.info("[FirebaseConfig] Initializing FCM Firebase App...");

            String processedJson = fcmServiceAccountJson.replace("\\n", "\n");
            InputStream serviceAccountStream = new ByteArrayInputStream(processedJson.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FirebaseConfig] FCM Firebase App initialized successfully (default app)");

        } catch (IOException e) {
            log.error("[FirebaseConfig] Failed to initialize FCM Firebase App", e);
            throw new IllegalStateException("FCM Firebase 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사전예약 DB용 Firebase 앱 초기화 (별도 앱)
     */
    private void initializePreRegistrationApp() {
        // 사전예약용 설정이 없으면 스킵
        if (preRegistrationServiceAccountJson == null || preRegistrationServiceAccountJson.isEmpty()) {
            log.warn("[FirebaseConfig] Pre-registration Firebase config not found, skipping...");
            return;
        }

        try {
            // 이미 초기화된 경우 스킵
            if (FirebaseApp.getApps().stream().anyMatch(app -> app.getName().equals(PRE_REGISTRATION_APP_NAME))) {
                log.info("[FirebaseConfig] Pre-registration Firebase App already initialized");
                return;
            }

            log.info("[FirebaseConfig] Initializing Pre-registration Firebase App...");

            String processedJson = preRegistrationServiceAccountJson.replace("\\n", "\n");
            InputStream serviceAccountStream = new ByteArrayInputStream(processedJson.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options, PRE_REGISTRATION_APP_NAME);
            log.info("[FirebaseConfig] Pre-registration Firebase App initialized successfully (name: {})", PRE_REGISTRATION_APP_NAME);

        } catch (IOException e) {
            log.error("[FirebaseConfig] Failed to initialize Pre-registration Firebase App", e);
            // 사전예약 기능은 선택적이므로 예외를 던지지 않고 경고만 출력
            log.warn("[FirebaseConfig] Pre-registration feature will be disabled");
        }
    }
}
