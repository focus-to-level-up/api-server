package com.studioedge.domain.auth.dto;

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
