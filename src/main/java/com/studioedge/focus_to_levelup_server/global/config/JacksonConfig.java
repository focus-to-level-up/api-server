package com.studioedge.focus_to_levelup_server.global.config;

import com.studioedge.focus_to_levelup_server.global.serializer.KstDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // [핵심] LocalDateTime 타입으로 들어오는 모든 요청은 이 Deserializer를 거치게 설정
            builder.deserializerByType(LocalDateTime.class, new KstDeserializer());
        };
    }
}
