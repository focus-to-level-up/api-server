package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GoogleTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("scope")
    private String scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("id_token")
    private String idToken;
}
