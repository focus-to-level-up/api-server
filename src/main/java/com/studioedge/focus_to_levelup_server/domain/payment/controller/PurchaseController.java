package com.studioedge.focus_to_levelup_server.domain.payment.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.history.PaymentHistoryListResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.service.history.PaymentHistoryService;
import com.studioedge.focus_to_levelup_server.domain.payment.service.refund.RefundService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Purchase", description = "인앱결제 API")
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final RefundService refundService;
    private final PaymentHistoryService paymentHistoryService;

    @Operation(summary = "결제 환불", description = "결제 내역을 환불하고 획득한 재화를 회수합니다. (7일 이내, 재화 미사용 시에만 가능)")
    @PostMapping("/{paymentLogId}/refund")
    public ResponseEntity<CommonResponse<RefundResponse>> refund(
            @AuthenticationPrincipal Member member,
            @PathVariable Long paymentLogId,
            @RequestBody @Valid RefundRequest request
    ) {
        RefundResponse response = refundService.processRefund(member.getId(), paymentLogId, request);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "결제 내역 조회", description = "내 모든 결제 내역을 조회합니다 (최신순)")
    @GetMapping("/history")
    public ResponseEntity<CommonResponse<PaymentHistoryListResponse>> getPaymentHistory(
            @AuthenticationPrincipal Member member
    ) {
        PaymentHistoryListResponse response = paymentHistoryService.getPaymentHistory(member.getId());
        return HttpResponseUtil.ok(response);
    }
}
