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

/**
 * Firebase Cloud Messaging 설정
 * - Firebase Admin SDK 초기화
 * - FCM 메시지 발송을 위한 기본 설정
 */
@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.service-account-json}")
    private String serviceAccountJson;

    /**
     * Firebase Admin SDK 초기화
     * - application.yml의 firebase.service-account-json 값을 사용
     * - JSON 문자열을 GoogleCredentials로 변환
     */
    @PostConstruct
    public void initialize() {
        try {
            // 이미 초기화되어 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new ByteArrayInputStream(serviceAccountJson.getBytes()));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}