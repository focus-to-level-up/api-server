package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.admin.guild.dto.UpdateGuildDescriptionRequest;
import com.studioedge.admin.guild.dto.UpdateGuildNameRequest;
import com.studioedge.response.CommonResponse;
import com.studioedge.admin.global.support.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Guild", description = "관리자 길드 관리 API")
@RestController
@RequestMapping("/api/v1/admin/guilds")
@RequiredArgsConstructor
public class GuildController {
    private final GuildService guildService;

    @GetMapping("/{guildId}")
    @Operation(summary = "길드 조회", description = "길드 ID로 길드 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<GuildResponse>> getGuild(
            @Parameter(description = "조회할 길드 ID") @PathVariable Long guildId
    ) {
        return HttpResponseUtil.ok(guildService.getGuildById(guildId));
    }

    @PutMapping("/{guildId}/name")
    @Operation(summary = "길드명 변경", description = "길드명을 변경합니다.")
    public ResponseEntity<CommonResponse<GuildResponse>> updateGuildName(
            @Parameter(description = "변경할 길드 ID") @PathVariable Long guildId,
            @Valid @RequestBody UpdateGuildNameRequest request
    ) {
        return HttpResponseUtil.ok(guildService.updateGuildName(guildId, request.name()));
    }

    @PutMapping("/{guildId}/description")
    @Operation(summary = "길드 설명 변경", description = "길드 설명(상태메시지)을 변경합니다.")
    public ResponseEntity<CommonResponse<GuildResponse>> updateGuildDescription(
            @Parameter(description = "변경할 길드 ID") @PathVariable Long guildId,
            @Valid @RequestBody UpdateGuildDescriptionRequest request
    ) {
        return HttpResponseUtil.ok(guildService.updateGuildDescription(guildId, request.description()));
    }
}
