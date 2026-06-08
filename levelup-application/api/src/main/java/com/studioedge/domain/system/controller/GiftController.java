package com.studioedge.domain.system.controller;

import com.studioedge.member.entity.Member;
import com.studioedge.domain.system.dto.request.GiftBonusTicketRequest;
import com.studioedge.domain.system.dto.response.GiftResponse;
import com.studioedge.domain.system.service.GiftService;
import com.studioedge.response.CommonResponse;
import com.studioedge.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gift", description = "선물 발송 API (관리자 전용 또는 유저간 선물)")
@RestController
@RequestMapping("/api/v1/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @Operation(
            summary = "보너스 티켓 선물",
            description = "특정 유저에게 10% 다이아 보너스 티켓을 선물합니다. 우편함으로 전달됩니다."
    )
    @PostMapping("/bonus-ticket")
    public ResponseEntity<CommonResponse<GiftResponse>> giftBonusTicket(
            @AuthenticationPrincipal Member sender,
            @Valid @RequestBody GiftBonusTicketRequest request
    ) {
        GiftResponse response = giftService.giftBonusTicket(
                sender.getId(),
                request.receiverMemberId(),
                request.ticketCount(),
                request.message()
        );
        return HttpResponseUtil.ok(response);
    }
}
