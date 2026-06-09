package com.studioedge.admin.controller;

import com.studioedge.admin.dto.request.AdminUpdateGuildDescriptionRequest;
import com.studioedge.admin.dto.request.AdminUpdateGuildNameRequest;
import com.studioedge.admin.dto.response.AdminGuildResponse;
import com.studioedge.admin.service.AdminGuildService;
import com.studioedge.response.CommonResponse;
import com.studioedge.admin.support.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Guild", description = "관리자 길드 관리 API")
@RestController
@RequestMapping("/api/v1/admin/guilds")
@RequiredArgsConstructor
public class AdminGuildController {
    private final AdminGuildService adminGuildService;

    @GetMapping("/{guildId}")
    @Operation(summary = "길드 조회", description = "길드 ID로 길드 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminGuildResponse>> getGuild(
            @Parameter(description = "조회할 길드 ID") @PathVariable Long guildId
    ) {
        return HttpResponseUtil.ok(adminGuildService.getGuildById(guildId));
    }

    @PutMapping("/{guildId}/name")
    @Operation(summary = "길드명 변경", description = "길드명을 변경합니다.")
    public ResponseEntity<CommonResponse<AdminGuildResponse>> updateGuildName(
            @Parameter(description = "변경할 길드 ID") @PathVariable Long guildId,
            @Valid @RequestBody AdminUpdateGuildNameRequest request
    ) {
        return HttpResponseUtil.ok(adminGuildService.updateGuildName(guildId, request.name()));
    }

    @PutMapping("/{guildId}/description")
    @Operation(summary = "길드 설명 변경", description = "길드 설명(상태메시지)을 변경합니다.")
    public ResponseEntity<CommonResponse<AdminGuildResponse>> updateGuildDescription(
            @Parameter(description = "변경할 길드 ID") @PathVariable Long guildId,
            @Valid @RequestBody AdminUpdateGuildDescriptionRequest request
    ) {
        return HttpResponseUtil.ok(adminGuildService.updateGuildDescription(guildId, request.description()));
    }
}
