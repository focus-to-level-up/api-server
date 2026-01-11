package com.studioedge.focus_to_levelup_server.global.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class KstDeserializer extends StdDeserializer<LocalDateTime> {
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public KstDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 먼저 Offset 정보(Z, +09:00 등)가 있는 형태로 파싱 시도
            // 이렇게 하면 'Z' 뿐만 아니라 '+00:00', '+09:00' 등도 모두 처리 가능
            return java.time.OffsetDateTime.parse(dateString)
                    .atZoneSameInstant(KST_ZONE_ID) // 타임존이 뭐든 KST로 변환
                    .toLocalDateTime(); // LocalDateTime으로 추출

        } catch (java.time.format.DateTimeParseException e1) {
            try {
                // 2. 오프셋 정보가 없는 단순 날짜 문자열인 경우 (예: "2026-01-01T12:00:00")
                // 이 경우 "이미 KST 로컬 시간"이라고 가정하고 그대로 파싱
                return LocalDateTime.parse(dateString);
            } catch (Exception e2) {
                log.error(">> [TimeConv] 날짜 파싱 실패: {}", dateString, e2);
                throw ctxt.weirdStringException(dateString, LocalDateTime.class, "날짜 형식이 올바르지 않습니다.");
            }
        }
    }
}
