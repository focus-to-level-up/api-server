package com.studioedge.focus_to_levelup_server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
@Profile("local")
public class LocalTestTimeConfiguration {
    @Value("${server.mock-date:}") // 설정 파일에 값이 없으면 비어있는 상태("")로 둠
    private String mockDateString;

    @Bean
    public Clock clock() {
        if (mockDateString != null && !mockDateString.isBlank()) {
            System.out.println("⚠️ [TEST MODE] 서버 시간이 강제로 고정되었습니다: " + mockDateString);

            // [수정] 입력받은 문자열을 LocalDateTime으로 먼저 파싱한 후,
            // 서울 시간대(Asia/Seoul) 정보를 입혀서 Instant로 변환합니다.
            LocalDateTime localDateTime = LocalDateTime.parse(mockDateString);
            ZoneId seoulZone = ZoneId.of("Asia/Seoul");
            Instant fixedInstant = localDateTime.atZone(seoulZone).toInstant();

            return Clock.fixed(fixedInstant, seoulZone);
        } else {
            return Clock.system(ZoneId.of("Asia/Seoul"));
        }
    }
}
