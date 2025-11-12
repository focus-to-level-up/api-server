package com.studioedge.focus_to_levelup_server.domain.payment.service.history;

import com.studioedge.focus_to_levelup_server.domain.payment.dao.PaymentLogRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.history.PaymentHistoryListResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.history.PaymentHistoryResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentHistoryService {

    private final PaymentLogRepository paymentLogRepository;

    /**
     * 결제 내역 조회 (최신순)
     */
    public PaymentHistoryListResponse getPaymentHistory(Long memberId) {
        List<PaymentLog> paymentLogs = paymentLogRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);

        List<PaymentHistoryResponse> responses = paymentLogs.stream()
                .map(PaymentHistoryResponse::from)
                .toList();

        return PaymentHistoryListResponse.of(responses);
    }
}
