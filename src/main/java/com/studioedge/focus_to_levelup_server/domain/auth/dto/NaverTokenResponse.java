package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class NaverTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private Long expiresIn;
}
