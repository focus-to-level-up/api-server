package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class KakaoTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;

    @SerializedName("scope")
    private String scope;
}
