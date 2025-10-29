package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AppleTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("id_token")
    private String idToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("token_type")
    private String tokenType;
}
