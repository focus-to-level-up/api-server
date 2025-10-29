package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GooglePublicKey {
    private String kty;
    private String alg;
    private String use;
    private String kid;
    private String n;
    private String e;
}
