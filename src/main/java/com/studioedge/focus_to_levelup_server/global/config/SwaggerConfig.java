package com.studioedge.focus_to_levelup_server.global.config;

import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    @Profile("local")
    public Server localServer() {
        return null; // 로컬 환경에서는 기본 서버 사용
    }

    @Bean
    @Profile("dev")
    public Server devServer() {
        Server devServer = new Server();
        devServer.setDescription("Development Server");
        devServer.setUrl("https://dev.api.studio-edge.app"); // TODO: dev 환경 URL로 수정
        return devServer;
    }

    @Bean
    @Profile("prod")
    public Server prodServer() {
        Server prodServer = new Server();
        prodServer.setDescription("Production Server");
        prodServer.setUrl("https://prod.api.studio-edge.app"); // TODO: prod 환경 URL로 수정
        return prodServer;
    }

    @Bean
    public OpenAPI openAPI(@Autowired(required = false) @Nullable Server server) {
        // Security 설정: JWT 기반 인증
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Arrays.asList(securityRequirement))
                .info(apiInfo())
                .openapi("3.0.0");

        if (server != null) {
            openAPI.servers(List.of(server));
        }

        return openAPI;
    }

    private Info apiInfo() {
        return new Info()
                .title("Focus to Level Up API")
                .description("""
                        ## Swagger 이용방법

                        - 해당 페이지는 Focus to Level Up API 서버의 swagger 페이지입니다.
                        - 에러나 문제가 발생한다면 곧바로 이슈를 등록해주세요.
                        - URI는 꼭 `/api`로 시작하며, RESTful API를 지향합니다.
                        - `/api/auth/**` 외에는 모두 인증/인가를 수행합니다.

                        ### 인증인가 방법
                        - Swagger에서는 오른쪽에 보이는 Authorize에 토큰값만 넣어주면 됩니다.
                        - 실제 토큰검증은 요청 헤더에 `Authorization: Bearer {token}` 형식으로 요청해주시면 됩니다.
                        """)
                .version("1.0.0");
    }
}
