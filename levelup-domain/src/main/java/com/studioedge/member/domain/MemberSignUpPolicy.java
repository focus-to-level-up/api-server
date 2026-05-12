package com.studioedge.member.domain;

import java.util.List;

public final class MemberSignUpPolicy {
    private MemberSignUpPolicy() {}

    /** 회원가입 시 지급되는 기본 캐릭터의 이름 */
    public static final String DEFAULT_CHARACTER_NAME = "양동동";

    /** 회원가입 시 지급되는 기본 과목 3개 색상코드 */
    public static final List<String> INITIAL_SUBJECT_COLORS = List.of(
            "EE5D42",
            "FA846C",
            "FF9852"
    );

    /** 회원가입 시 지급되는 기본 프로필 에셋의 이름 목록 */
    public static final List<String> DEFAULT_ASSET_NAMES = List.of(
            "양동동 1단계 프로필 이미지",
            "양동동 프로필 테두리"
    );
}