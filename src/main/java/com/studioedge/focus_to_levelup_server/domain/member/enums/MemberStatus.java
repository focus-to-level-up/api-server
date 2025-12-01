package com.studioedge.focus_to_levelup_server.domain.member.enums;

public enum MemberStatus {
    ACTIVE,    // 활성
    BANNED,    // 정지
    RANKING_BANNED,
    WITHDRAWN,  // 탈퇴
    PENDING // 가입 진행중 (가입 완료되지 않은 유저)
}
