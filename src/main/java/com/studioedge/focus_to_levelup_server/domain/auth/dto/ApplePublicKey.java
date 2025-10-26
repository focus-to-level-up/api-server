package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import lombok.Data;

@Data
public class ApplePublicKey {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
}
