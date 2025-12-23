package com.studioedge.focus_to_levelup_server.global.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
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
        String dateString = p.getText(); // 프론트에서 보낸 시간 문자열

        // 데이터가 없거나 비어있는 경우 처리
        if (dateString == null || dateString.trim().isEmpty()) {
            return null; // 혹은 LocalDateTime.now() 등 정책에 따라 변경
        }

        try {
            // Case 1: UTC 포맷 ('Z'로 끝나는 경우) -> KST로 변환
            if (dateString.endsWith("Z")) {
                LocalDateTime kstTime = Instant.parse(dateString)
                        .atZone(KST_ZONE_ID)
                        .toLocalDateTime();

                log.debug(">> [TimeConv] UTC({}) -> KST 변환됨: {}", dateString, kstTime);
                return kstTime;
            }

            // Case 2: KST 포맷 ('Z'가 없는 경우) -> 그대로 파싱
            // 예: "2025-12-23T13:00:00"
            return LocalDateTime.parse(dateString);

        } catch (Exception e) {
            log.error(">> [TimeConv] 날짜 파싱 실패: {}", dateString, e);
            // Jackson이 알 수 있는 예외로 감싸서 던짐
            throw ctxt.weirdStringException(dateString, LocalDateTime.class, "날짜 형식이 올바르지 않습니다.");
        }
    }
}
