package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class KakaoUserInfo {

    private Long id;

    @SerializedName("connected_at")
    private String connectedAt;

    @SerializedName("kakao_account")
    private KakaoAccount kakaoAccount;

    @Data
    public static class KakaoAccount {
        private Profile profile;

        @SerializedName("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;

        @SerializedName("profile_image_needs_agreement")
        private Boolean profileImageNeedsAgreement;

        @Data
        public static class Profile {
            private String nickname;

            @SerializedName("profile_image_url")
            private String profileImageUrl;

            @SerializedName("thumbnail_image_url")
            private String thumbnailImageUrl;

            @SerializedName("is_default_image")
            private Boolean isDefaultImage;
        }
    }
}
