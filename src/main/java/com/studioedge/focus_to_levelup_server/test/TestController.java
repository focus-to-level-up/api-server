package com.studioedge.focus_to_levelup_server.test;

import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "테스트 API")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "헬스 체크", description = "서버 상태를 확인합니다")
    @GetMapping("/health")
    public CommonResponse<String> healthCheck() {
        return CommonResponse.ok("Server is running!");
    }

    @Operation(summary = "Swagger 테스트", description = "Swagger 설정이 정상 작동하는지 확인합니다")
    @GetMapping("/swagger")
    public CommonResponse<TestResponse> swaggerTest() {
        return CommonResponse.ok(new TestResponse("Swagger is working!"));
    }

    public record TestResponse(String message) {
    }
}
