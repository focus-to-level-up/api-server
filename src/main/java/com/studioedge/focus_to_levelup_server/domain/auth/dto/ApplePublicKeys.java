package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplePublicKeys {
    private List<ApplePublicKey> keys;
}
