package com.studioedge.focus_to_levelup_server.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "서버 상태를 확인합니다. (ALB, ECS 헬스체크용)")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
