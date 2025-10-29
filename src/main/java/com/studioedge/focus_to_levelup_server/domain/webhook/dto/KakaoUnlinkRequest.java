package com.studioedge.focus_to_levelup_server.domain.webhook.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 연결 해제 웹훅 요청 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoUnlinkRequest {

    @SerializedName("app_id")
    private String appId;

    @SerializedName("user_id")
    private String userId;  // 카카오 회원번호 (socialId)

    private String referrer;
}
