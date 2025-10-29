package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class NaverUserInfo {

    @SerializedName("resultcode")
    private String resultCode;

    private String message;

    private Response response;

    @Data
    public static class Response {
        private String id;
        private String nickname;

        @SerializedName("profile_image")
        private String profileImage;

        private String email;
        private String name;
    }
}
