package com.studioedge.focus_to_levelup_server.domain.payment.dao;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.WebhookEvent;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {

    /**
     * 특정 사용자의 웹훅 이벤트 목록 조회
     */
    List<WebhookEvent> findAllByAppUserId(String appUserId);

    /**
     * 상태별 웹훅 이벤트 조회
     */
    List<WebhookEvent> findAllByStatus(WebhookEventStatus status);

    /**
     * 실패한 이벤트 조회 (재처리용)
     */
    List<WebhookEvent> findAllByStatusOrderByCreatedAtAsc(WebhookEventStatus status);
}
