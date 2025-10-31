package com.studioedge.focus_to_levelup_server.domain.webhook.service;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.webhook.dto.KakaoUnlinkRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KakaoWebhookService {

    private final MemberRepository memberRepository;

    /**
     * 카카오 연결 해제 웹훅 처리
     * 사용자가 카카오톡 앱에서 직접 연결 해제 시 호출됨
     */
    public void handleUserUnlink(KakaoUnlinkRequest request) {
        String kakaoUserId = request.getUserId();

        log.info("Processing Kakao unlink for user ID: {}", kakaoUserId);

        // 카카오 소셜 ID로 회원 찾기
        Optional<Member> memberOpt = memberRepository.findBySocialTypeAndSocialId(
                SocialType.KAKAO,
                kakaoUserId
        );

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // 회원 탈퇴 처리 (soft delete)
            member.withdraw();
            memberRepository.save(member);

            log.info("Successfully withdrew member {} via Kakao webhook", member.getId());
        } else {
            log.warn("Member not found for Kakao user ID: {}", kakaoUserId);
        }
    }
}
